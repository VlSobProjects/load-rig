package loadrig.run;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import loadrig.model.profile.EquilibriumCheck;
import loadrig.model.profile.LoadProfile;
import loadrig.model.profile.ScenarioName;
import loadrig.model.profile.StepMix;

/**
 * The load-profile description a run lays beside its result log: what the run was meant to
 * apply, so that the capture's reader compares the achieved intensity against a stated target
 * instead of guessing one. It carries the profile's own facts, the target rates the profile
 * implies - computed here exactly as the validation computes them, never restated by hand -
 * the run's identity and the version of the SUT the run drove.
 *
 * <p>It is written before the load starts: a run that dies mid-way still leaves the statement
 * of what it was trying to do, and the dying injector is one of the very variants this rig
 * exists to produce.
 */
public record LoadProfileDescription(
        String profile,
        String runStamp,
        String sutVersion,
        String jtlTimestampSemantics,
        int virtualUsers,
        int rampSeconds,
        int steadyWindowMinutes,
        LoadProfile.ThinkTime thinkTime,
        StepMix stepMix,
        LoadProfile.HotSetSkew hotSetSkew,
        int seededVolume,
        Map<String, Integer> scenarioPopulation,
        TargetRates targetRatesPerMinute) {

    /**
     * What the JTL stamps mean, stated for the capture's reader: a sample carries the moment it
     * started. The setting itself is applied by {@link InjectorSettings}; this is its statement
     * in the artifact, so a log is never read under the wrong semantics.
     */
    static final String SAMPLES_ARE_STAMPED_AT_START = "start";

    /**
     * The per-operation targets the profile implies, in steps per minute. The seven operations
     * are the mix's rows; the settlements and the net drift come beside them because they are
     * what the population equilibrium is judged by, and a reader of the capture should not have
     * to re-derive the transition split to judge it.
     */
    public record TargetRates(
            double steps,
            double lookingAtAList,
            double openingOneTask,
            double movingATask,
            double discussion,
            double runningAReport,
            double creatingATask,
            double deletingATask,
            double settlements,
            double netDriftOverWindow) {
    }

    public static LoadProfileDescription of(LoadProfile profile,
            EquilibriumCheck.Intensities intensities, String runStamp, String sutVersion) {
        StepMix mix = profile.stepMix();
        double steps = intensities.stepsPerMinute();
        TargetRates targets = new TargetRates(
                steps,
                rateOf(steps, mix.lookingAtAListPercent()),
                rateOf(steps, mix.openingOneTaskPercent()),
                rateOf(steps, mix.movingATaskPercent()),
                rateOf(steps, mix.discussionPercent()),
                rateOf(steps, mix.runningAReportPercent()),
                rateOf(steps, mix.creatingATaskPercent()),
                rateOf(steps, mix.deletingATaskPercent()),
                intensities.settlementsPerMinute(),
                intensities.netDriftOverWindow());
        Map<String, Integer> populations = new LinkedHashMap<>();
        for (ScenarioName name : ScenarioName.values()) {
            populations.put(name.key(), profile.scenarioPopulation().get(name));
        }
        return new LoadProfileDescription(
                profile.name(),
                runStamp,
                sutVersion,
                SAMPLES_ARE_STAMPED_AT_START,
                profile.virtualUsers(),
                profile.rampSeconds(),
                profile.steadyWindowMinutes(),
                profile.thinkTime(),
                mix,
                profile.hotSetSkew(),
                profile.seededVolume(),
                populations,
                targets);
    }

    public void writeTo(Path file) {
        try {
            new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(file.toFile(), this);
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "the load-profile description could not be written to " + file, e);
        }
    }

    private static double rateOf(double stepsPerMinute, int mixPercent) {
        return stepsPerMinute * mixPercent / 100.0;
    }
}
