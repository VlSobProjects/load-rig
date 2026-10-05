package loadrig.run;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import loadrig.model.Role;
import loadrig.model.profile.Campaign;
import loadrig.model.profile.LoadProfile;
import loadrig.model.profile.ScenarioName;
import loadrig.model.profile.ServiceLevelBand;
import loadrig.model.profile.ServiceLevels;
import loadrig.model.profile.StepMix;
import loadrig.model.scenario.PlayingAccounts;
import loadrig.model.scenario.ScenarioDemand;
import loadrig.model.scenario.WarmStartCensus;
import loadrig.model.step.StepKind;

/**
 * The load-profile description a run lays beside its result log: what the run was meant to apply,
 * so that the capture's reader compares the achieved intensity against a stated target instead of
 * guessing one. It carries the profile's own facts, the target rates the scenarios imply -
 * computed by the same code the plan is built from, never restated by hand - the population the
 * run established before the window, the service levels the run is judged by, the run's identity
 * and the version of the SUT it drove.
 *
 * <p>The service levels are here because they are chosen rather than measured: a capture judged
 * against figures nobody wrote down cannot be re-read later, and re-reading old captures is what
 * keeping them is for. The rotation is here for the same reason: how many people played a window
 * decides how wide the working set was, so two captures of one intensity over rotations of
 * different depths are not comparable, and nothing in the result log says which was which.
 *
 * <p>It is written before the load starts: a run that dies mid-way still leaves the statement of
 * what it was trying to do, and the dying injector is one of the very variants this rig exists to
 * produce.
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
        int endingASessionPercent,
        Map<String, Integer> scenarioPopulation,
        Rotation rotation,
        TargetRates targetRatesPerMinute,
        Population population,
        ServiceLevelsApplied serviceLevels) {

    /**
     * What the JTL stamps mean, stated for the capture's reader: a sample carries the moment it
     * started. The setting itself is applied by {@link InjectorSettings}; this is its statement
     * in the artifact, so a log is never read under the wrong semantics.
     */
    static final String SAMPLES_ARE_STAMPED_AT_START = "start";

    /**
     * How many decimals a computed figure is published with. The rates are computed in binary
     * fractions, and written as computed they carry the arithmetic's noise into the artifact - a
     * rate of 15.6 stated as 15.600000000000001. A thousandth of a step per minute is far below
     * anything an achieved intensity is compared at, so the reader loses nothing. Only the
     * statement is rounded: the census and the plan are computed from the demand itself.
     */
    static final int PUBLISHED_DECIMALS = 3;

    /**
     * The per-operation targets the profile implies, in steps per minute. The seven operations are
     * the mix's rows; the settlements come beside them because a reader judging the population
     * should not have to re-derive the transition split to do it, and the sessions given up
     * because they are an intensity the run applies which no row of the mix accounts for - and
     * the rate at which the log's sign-ins should arrive.
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
            double sessionsGivenUp) {
    }

    /**
     * The people the run played, as against the seats its populations held at once: the depth the
     * campaign states, the accounts that depth produces and how they fall by role. An account
     * between sessions is waiting its turn rather than absent, so all of them are part of the
     * working set the stand had to hold (DR-8).
     */
    public record Rotation(int playersPerSeat, int accountsPlaying,
            Map<String, Integer> playersByRole) {

        static Rotation of(Campaign campaign, PlayingAccounts playing) {
            Map<String, Integer> byRole = new LinkedHashMap<>();
            for (Role role : Role.values()) {
                byRole.put(role.name().toLowerCase(Locale.ENGLISH),
                        playing.names(role).size());
            }
            return new Rotation(campaign.playersPerSeat(), playing.members().size(), byRole);
        }
    }

    /**
     * The population under the load: what the profile needed, what the stand was measured to hold
     * before the window, and what the warm start had to bring about (DR-6). The growth is the rows
     * the window adds to the table - creations less deletions - and it is stated as the fact it is:
     * noise against a seeded history, the whole table on a stand that held none, and the reader's
     * to judge against the measured volume.
     */
    public record Population(
            int census,
            int censusBuckets,
            int tasksOnTheStandAtStart,
            int readFromTheStand,
            int createdByTheWarmStart,
            double tableGrowthOverWindow) {
    }

    /**
     * The levels this capture is judged by, in the file's own spelling, with the percentile they
     * are stated at: the criterion travels with the evidence.
     */
    public record ServiceLevelsApplied(int percentile, Map<String, Integer> byBandMillis,
            int hardCeilingMillis) {

        static ServiceLevelsApplied of(ServiceLevels levels) {
            Map<String, Integer> byBand = new LinkedHashMap<>();
            for (ServiceLevelBand band : ServiceLevelBand.values()) {
                byBand.put(band.key(), levels.levelOf(band));
            }
            return new ServiceLevelsApplied(ServiceLevels.PERCENTILE, byBand,
                    levels.hardCeilingMillis());
        }
    }

    public static LoadProfileDescription of(LoadProfile profile, Campaign campaign,
            ScenarioDemand demand, PlayingAccounts playing, WarmStartCensus census,
            WarmStart.Result warmStart, String runStamp, String sutVersion) {
        TargetRates targets = new TargetRates(
                published(demand.stepsPerMinute()),
                published(demand.rateOf(StepKind.LOOKING_AT_A_LIST)),
                published(demand.rateOf(StepKind.OPENING_ONE_TASK)),
                published(demand.rateOf(StepKind.MOVING_A_TASK)),
                published(demand.rateOf(StepKind.DISCUSSION)),
                published(demand.rateOf(StepKind.RUNNING_A_REPORT)),
                published(demand.rateOf(StepKind.CREATING_A_TASK)),
                published(demand.rateOf(StepKind.DELETING_A_TASK)),
                published(demand.settlementsPerMinute()),
                published(demand.sessionsGivenUpPerMinute()));
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
                profile.stepMix(),
                profile.hotSetSkew(),
                profile.endingASessionPercent(),
                populations,
                Rotation.of(campaign, playing),
                targets,
                new Population(census.tasks(), census.buckets().size(),
                        warmStart.tasksOnTheStand(), warmStart.read(), warmStart.created(),
                        published(demand.tableGrowthOverWindow())),
                ServiceLevelsApplied.of(campaign.serviceLevels()));
    }

    /** A computed figure as the artifact states it: to {@link #PUBLISHED_DECIMALS}, half up. */
    static double published(double computed) {
        return BigDecimal.valueOf(computed)
                .setScale(PUBLISHED_DECIMALS, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public void writeTo(Path file) {
        try {
            new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(file.toFile(), this);
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "the load-profile description could not be written to " + file, e);
        }
    }
}
