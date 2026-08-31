package loadrig.run;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import loadrig.model.profile.LoadProfile;
import loadrig.model.profile.ProfileLoader;
import loadrig.model.profile.ServiceLevelBand;
import loadrig.model.profile.ServiceLevels;
import loadrig.model.profile.ServiceLevelsLoader;
import loadrig.model.scenario.ScenarioDemand;
import loadrig.model.scenario.SeatedAccounts;
import loadrig.model.scenario.WarmStartCensus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Proves the description artifact against the repository's own day profile: the written JSON
 * carries the profile's stated facts, the run's identity, the computed target rates - the same
 * numbers the plan is built from, so the description can never state targets the run did not obey
 * - the population the run established, measured rather than stated, and the service levels the
 * capture is judged by, which are chosen numbers and therefore have to travel with it.
 */
class LoadProfileDescriptionTest {

    private static final String A_RUN = "20260830-120000";
    private static final String A_SUT_VERSION = "unknown";

    /** A warm start that found a stand holding some of the census and created the rest. */
    private static final WarmStart.Result A_WARM_START = new WarmStart.Result(323, 60, 24, 66);

    @Test
    void theDescriptionStatesTheProfileTheRunAndTheComputedTargets(@TempDir Path directory)
            throws IOException {
        LoadProfile profile = day();
        Path file = directory.resolve("load-profile.json");

        descriptionOf(profile).writeTo(file);

        JsonNode description = new ObjectMapper().readTree(file.toFile());
        assertEquals(profile.name(), description.get("profile").asText());
        assertEquals(A_RUN, description.get("runStamp").asText());
        assertEquals(A_SUT_VERSION, description.get("sutVersion").asText());
        assertEquals(LoadProfileDescription.SAMPLES_ARE_STAMPED_AT_START,
                description.get("jtlTimestampSemantics").asText());
        assertEquals(profile.virtualUsers(), description.get("virtualUsers").asInt());
        assertEquals(profile.rampSeconds(), description.get("rampSeconds").asInt());
        assertEquals(profile.steadyWindowMinutes(),
                description.get("steadyWindowMinutes").asInt());
        assertEquals(profile.thinkTime().minSeconds(),
                description.get("thinkTime").get("minSeconds").asInt());
        assertEquals(profile.stepMix().lookingAtAListPercent(),
                description.get("stepMix").get("lookingAtAListPercent").asInt());
        assertEquals(profile.hotSetSkew().notesPercent(),
                description.get("hotSetSkew").get("notesPercent").asInt());
        assertEquals(profile.endingASessionPercent(),
                description.get("endingASessionPercent").asInt(),
                "the share of iterations that end their session is applied by the run, so the"
                        + " description states it");
    }

    @Test
    void theTargetRatesAreTheOnesTheCodedScenariosImply(@TempDir Path directory)
            throws IOException {
        LoadProfile profile = day();
        ScenarioDemand demand = ScenarioDemand.of(profile);
        Path file = directory.resolve("load-profile.json");

        descriptionOf(profile).writeTo(file);

        JsonNode targets = new ObjectMapper().readTree(file.toFile())
                .get("targetRatesPerMinute");
        assertEquals(demand.stepsPerMinute(), targets.get("steps").asDouble(), 1e-9);
        assertEquals(demand.creationsPerMinute(),
                targets.get("creatingATask").asDouble(), 1e-9);
        assertEquals(demand.deletionsPerMinute(),
                targets.get("deletingATask").asDouble(), 1e-9);
        assertEquals(demand.notesPerMinute(), targets.get("discussion").asDouble(), 1e-9);
        assertEquals(demand.settlementsPerMinute(),
                targets.get("settlements").asDouble(), 1e-9);
        assertEquals(demand.sessionsGivenUpPerMinute(),
                targets.get("sessionsGivenUp").asDouble(), 1e-9);
        double statedOperations = targets.get("lookingAtAList").asDouble()
                + targets.get("openingOneTask").asDouble()
                + targets.get("movingATask").asDouble()
                + targets.get("discussion").asDouble()
                + targets.get("runningAReport").asDouble()
                + targets.get("creatingATask").asDouble()
                + targets.get("deletingATask").asDouble();
        assertEquals(demand.stepsPerMinute(), statedOperations, 1e-9);
    }

    @Test
    void thePopulationIsTheMeasuredStandAndTheEstablishedCensus(@TempDir Path directory)
            throws IOException {
        LoadProfile profile = day();
        Path file = directory.resolve("load-profile.json");

        descriptionOf(profile).writeTo(file);

        JsonNode population = new ObjectMapper().readTree(file.toFile()).get("population");
        assertEquals(censusOf(profile).tasks(), population.get("census").asInt());
        assertEquals(A_WARM_START.tasksOnTheStand(),
                population.get("tasksOnTheStandAtStart").asInt(),
                "the volume of the stand is the one the warm start measured, never one stated");
        assertEquals(A_WARM_START.read(), population.get("readFromTheStand").asInt());
        assertEquals(A_WARM_START.created(), population.get("createdByTheWarmStart").asInt());
        assertEquals(ScenarioDemand.of(profile).tableGrowthOverWindow(),
                population.get("tableGrowthOverWindow").asDouble(), 1e-9);
    }

    @Test
    void theScenarioPopulationsAreKeyedByTheScenarioNames(@TempDir Path directory)
            throws IOException {
        LoadProfile profile = day();
        Path file = directory.resolve("load-profile.json");

        descriptionOf(profile).writeTo(file);

        JsonNode populations = new ObjectMapper().readTree(file.toFile())
                .get("scenarioPopulation");
        assertTrue(populations.has("worker"), "the keys are the profile file's own: " + populations);
        assertTrue(populations.has("discussion"),
                "the keys are the profile file's own: " + populations);
        int seated = 0;
        for (JsonNode population : populations) {
            seated += population.asInt();
        }
        assertEquals(profile.virtualUsers(), seated);
    }

    @Test
    void theServiceLevelsTheRunIsJudgedByTravelWithTheCapture(@TempDir Path directory)
            throws IOException {
        LoadProfile profile = day();
        ServiceLevels levels = campaignLevels();
        Path file = directory.resolve("load-profile.json");

        descriptionOf(profile).writeTo(file);

        JsonNode judged = new ObjectMapper().readTree(file.toFile()).get("serviceLevels");
        assertEquals(ServiceLevels.PERCENTILE, judged.get("percentile").asInt());
        assertEquals(levels.hardCeilingMillis(), judged.get("hardCeilingMillis").asInt());
        JsonNode byBand = judged.get("byBandMillis");
        for (ServiceLevelBand band : ServiceLevelBand.values()) {
            assertEquals(levels.levelOf(band), byBand.get(band.key()).asInt(),
                    "the levels are published in the file's own spelling: " + byBand);
        }
    }

    private static LoadProfile day() {
        return ProfileLoader.load(Path.of("profiles", "day.json"));
    }

    private static WarmStartCensus censusOf(LoadProfile profile) {
        return WarmStartCensus.of(profile, SeatedAccounts.of(profile));
    }

    private static ServiceLevels campaignLevels() {
        return ServiceLevelsLoader.load(Path.of("profiles", "service-levels.json"));
    }

    private static LoadProfileDescription descriptionOf(LoadProfile profile) {
        return LoadProfileDescription.of(profile, ScenarioDemand.of(profile),
                censusOf(profile), A_WARM_START, campaignLevels(), A_RUN, A_SUT_VERSION);
    }
}
