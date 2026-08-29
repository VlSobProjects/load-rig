package loadrig.run;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import loadrig.model.profile.LoadProfile;
import loadrig.model.profile.ScenarioName;
import loadrig.model.profile.StepMix;
import loadrig.model.scenario.ScenarioDemand;
import loadrig.model.scenario.WarmStartCensus;
import loadrig.model.step.StepKind;

/**
 * The load-profile description a run lays beside its result log: what the run was meant to apply,
 * so that the capture's reader compares the achieved intensity against a stated target instead of
 * guessing one. It carries the profile's own facts, the target rates the scenarios imply -
 * computed by the same code the plan is built from, never restated by hand - the population the
 * run established before the window, the run's identity and the version of the SUT it drove.
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
        Map<String, Integer> scenarioPopulation,
        TargetRates targetRatesPerMinute,
        Population population) {

    /**
     * What the JTL stamps mean, stated for the capture's reader: a sample carries the moment it
     * started. The setting itself is applied by {@link InjectorSettings}; this is its statement
     * in the artifact, so a log is never read under the wrong semantics.
     */
    static final String SAMPLES_ARE_STAMPED_AT_START = "start";

    /**
     * The per-operation targets the profile implies, in steps per minute. The seven operations are
     * the mix's rows; the settlements come beside them because a reader judging the population
     * should not have to re-derive the transition split to do it.
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
            double settlements) {
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

    public static LoadProfileDescription of(LoadProfile profile, ScenarioDemand demand,
            WarmStartCensus census, WarmStart.Result warmStart, String runStamp,
            String sutVersion) {
        TargetRates targets = new TargetRates(
                demand.stepsPerMinute(),
                demand.rateOf(StepKind.LOOKING_AT_A_LIST),
                demand.rateOf(StepKind.OPENING_ONE_TASK),
                demand.rateOf(StepKind.MOVING_A_TASK),
                demand.rateOf(StepKind.DISCUSSION),
                demand.rateOf(StepKind.RUNNING_A_REPORT),
                demand.rateOf(StepKind.CREATING_A_TASK),
                demand.rateOf(StepKind.DELETING_A_TASK),
                demand.settlementsPerMinute());
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
                populations,
                targets,
                new Population(census.tasks(), census.buckets().size(),
                        warmStart.tasksOnTheStand(), warmStart.read(), warmStart.created(),
                        demand.tableGrowthOverWindow()));
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
