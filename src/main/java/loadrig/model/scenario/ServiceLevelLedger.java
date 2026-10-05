package loadrig.model.scenario;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import loadrig.model.profile.ServiceLevelBand;
import loadrig.model.profile.ServiceLevels;
import loadrig.model.step.StepKind;

/**
 * The run's record of what the people it plays actually waited. Every sample the result log
 * carries is timed into the band of the act that produced it, and at the end the run states the
 * percentile it realized per band against the level the campaign fixed.
 *
 * <p>A sample reaches a band through the label the plan gave it: the plan states the kind of
 * every request it builds, and the kind names the band. A label no kind claims is still timed -
 * against the hard ceiling, which is over everything - and counted apart, so a step that escapes
 * the bands shows in the report as a number instead of quietly going unjudged.
 *
 * <p>What the ledger does not do is decide anything about the run. A breached level is a finding
 * about the system, not a failure of the script: the faulted variants this rig exists to produce
 * are meant to breach, and a run that stopped itself over a breach could not capture them.
 */
public final class ServiceLevelLedger {

    private final Map<ServiceLevelBand, Times> times = new EnumMap<>(ServiceLevelBand.class);
    private final Times outsideTheBands = new Times();
    private final AtomicLong worstMillis = new AtomicLong();
    private volatile Map<String, ServiceLevelBand> bandsByLabel = Map.of();

    public ServiceLevelLedger() {
        for (ServiceLevelBand band : ServiceLevelBand.values()) {
            times.put(band, new Times());
        }
    }

    /**
     * The bands the labels of an assembled plan fall into, taken from the kinds the plan itself
     * declared. It is stated once, before the load starts, and every sample is attributed through
     * it.
     */
    public void judgeByTheKindsOf(Map<String, StepKind> kindsByLabel) {
        Map<String, ServiceLevelBand> bands = new LinkedHashMap<>();
        kindsByLabel.forEach((label, kind) -> bands.put(label, kind.band()));
        this.bandsByLabel = Map.copyOf(bands);
    }

    /**
     * The band each label of the assembled plan is judged in. Empty until a plan states its kinds,
     * which is why a run installs them before the load and never during it.
     */
    public Map<String, ServiceLevelBand> bandsByLabel() {
        return bandsByLabel;
    }

    /** One sample of the result log: what it was called, and how long the person waited. */
    public void took(String label, long elapsedMillis) {
        ServiceLevelBand band = bandsByLabel.get(label);
        (band == null ? outsideTheBands : times.get(band)).add(elapsedMillis);
        worstMillis.accumulateAndGet(elapsedMillis, Math::max);
    }

    /** What the run realized against the levels the campaign was fixed at. */
    public Verdict verdict(ServiceLevels levels) {
        List<BandResult> bands = new ArrayList<>();
        long overTheCeiling = 0;
        for (ServiceLevelBand band : ServiceLevelBand.values()) {
            long[] sorted = times.get(band).sorted();
            bands.add(new BandResult(band, sorted.length,
                    percentile(sorted, ServiceLevels.PERCENTILE), worst(sorted),
                    levels.levelOf(band)));
            overTheCeiling += over(sorted, levels.hardCeilingMillis());
        }
        // The ceiling is over everything the injector measured, the samples no band claims
        // included: it is the figure past which a run is not measuring a usable system at all.
        long[] unnamed = outsideTheBands.sorted();
        overTheCeiling += over(unnamed, levels.hardCeilingMillis());
        return new Verdict(List.copyOf(bands), unnamed.length, overTheCeiling,
                worstMillis.get(), levels.hardCeilingMillis());
    }

    /**
     * The percentile by nearest rank: the value at rank ceil(p / 100 * n) of the sorted times.
     * The method is stated rather than assumed, because two percentile definitions over the same
     * samples answer differently and a capture judged by an unnamed one cannot be re-read.
     */
    static long percentile(long[] sorted, int percentile) {
        if (sorted.length == 0) {
            return 0;
        }
        int rank = (int) Math.ceil(percentile / 100.0 * sorted.length);
        return sorted[Math.min(sorted.length, Math.max(1, rank)) - 1];
    }

    private static long worst(long[] sorted) {
        return sorted.length == 0 ? 0 : sorted[sorted.length - 1];
    }

    private static long over(long[] sorted, int ceilingMillis) {
        int first = 0;
        while (first < sorted.length && sorted[first] <= ceilingMillis) {
            first++;
        }
        return sorted.length - (long) first;
    }

    /**
     * What one band realized. A band no sample landed in is not a met level and not a breached
     * one: it was not exercised, and the report says so rather than reporting a zero.
     */
    public record BandResult(ServiceLevelBand band, long samples, long percentileMillis,
            long worstMillis, int levelMillis) {

        public boolean exercised() {
            return samples > 0;
        }

        public boolean breached() {
            return exercised() && percentileMillis > levelMillis;
        }
    }

    /**
     * The run's own half of the viability criteria. The other half - the application's log staying
     * quiet and the throttled-period counter staying flat - is read on the stand and not by the
     * injector, which drives no operational endpoint; a verdict that claimed it would be claiming
     * a measurement nobody took.
     */
    public record Verdict(List<BandResult> bands, long samplesOutsideTheBands,
            long samplesOverTheCeiling, long worstMillis, int hardCeilingMillis) {

        public boolean breached() {
            return samplesOverTheCeiling > 0 || bands.stream().anyMatch(BandResult::breached);
        }
    }

    /**
     * The times of one band, held as primitives and sorted only when the verdict is drawn. The
     * lock is the band's own: the threads of a run write here as often as they sample, and a
     * single lock over every band would put the injector's own contention into the measurement.
     */
    private static final class Times {

        private long[] values = new long[1024];
        private int count;

        synchronized void add(long millis) {
            if (count == values.length) {
                values = Arrays.copyOf(values, values.length * 2);
            }
            values[count++] = millis;
        }

        synchronized long[] sorted() {
            long[] copy = Arrays.copyOf(values, count);
            Arrays.sort(copy);
            return copy;
        }
    }
}
