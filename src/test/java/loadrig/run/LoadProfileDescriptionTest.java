package loadrig.run;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import loadrig.model.Role;
import loadrig.model.profile.Campaign;
import loadrig.model.profile.CampaignLoader;
import loadrig.model.profile.LoadProfile;
import loadrig.model.profile.ProfileLoader;
import loadrig.model.profile.ServiceLevelBand;
import loadrig.model.profile.ServiceLevels;
import loadrig.model.scenario.PlayingAccounts;
import loadrig.model.scenario.ScenarioDemand;
import loadrig.model.scenario.WarmStartCensus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Proves the description artifact against the repository's own day profile: the written JSON
 * carries the profile's stated facts, the run's identity, the computed target rates - the same
 * numbers the plan is built from, so the description can never state targets the run did not obey
 * - the population the run established, measured rather than stated, the rotation it played, and
 * the service levels the capture is judged by: the last two are the campaign's chosen numbers and
 * therefore have to travel with the evidence.
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
    void noComputedFigureIsWrittenBeyondThePublishedDecimals(@TempDir Path directory)
            throws IOException {
        LoadProfile profile = day();
        Path file = directory.resolve("load-profile.json");

        descriptionOf(profile).writeTo(file);

        JsonNode description = new ObjectMapper().readTree(file.toFile());
        JsonNode targets = description.get("targetRatesPerMinute");
        targets.fieldNames().forEachRemaining(name ->
                assertTrue(decimalsOf(targets.get(name))
                                <= LoadProfileDescription.PUBLISHED_DECIMALS,
                        "the rate of " + name + " is written with the noise of the arithmetic"
                                + " that computed it: " + targets.get(name).asText()));
        JsonNode growth = description.get("population").get("tableGrowthOverWindow");
        assertTrue(decimalsOf(growth) <= LoadProfileDescription.PUBLISHED_DECIMALS,
                "the table's growth is written with the noise of the arithmetic that computed"
                        + " it: " + growth.asText());
    }

    @Test
    void aPublishedFigureIsTheComputedOneToTheStatedDecimalsHalfUp() {
        assertEquals(15.6, LoadProfileDescription.published(15.600000000000001));
        assertEquals(14.4, LoadProfileDescription.published(14.399999999999999));
        assertEquals(13.333, LoadProfileDescription.published(40.0 / 3));
        assertEquals(0.001, LoadProfileDescription.published(0.0005));
        assertEquals(120.0, LoadProfileDescription.published(120.0));
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
    void theRotationThatPlayedTheWindowTravelsWithTheCapture(@TempDir Path directory)
            throws IOException {
        LoadProfile profile = day();
        Path file = directory.resolve("load-profile.json");

        descriptionOf(profile).writeTo(file);

        JsonNode rotation = new ObjectMapper().readTree(file.toFile()).get("rotation");
        assertEquals(theCampaign().playersPerSeat(), rotation.get("playersPerSeat").asInt());
        assertEquals(playersOf(profile).members().size(),
                rotation.get("accountsPlaying").asInt(),
                "how many people played decides how wide the working set was, and nothing in the"
                        + " result log says it");
        assertEquals(playersOf(profile).names(Role.WORKER).size(),
                rotation.get("playersByRole").get("worker").asInt());
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

    /** The decimals a number of the written file carries, as the file spells it. */
    private static int decimalsOf(JsonNode number) {
        return Math.max(0, new BigDecimal(number.asText()).stripTrailingZeros().scale());
    }

    private static LoadProfile day() {
        return ProfileLoader.load(Path.of("profiles", "day.json"));
    }

    private static WarmStartCensus censusOf(LoadProfile profile) {
        return WarmStartCensus.of(profile, playersOf(profile));
    }

    private static PlayingAccounts playersOf(LoadProfile profile) {
        return PlayingAccounts.of(profile, theCampaign());
    }

    private static Campaign theCampaign() {
        return CampaignLoader.load(Path.of("profiles", "campaign.json"));
    }

    private static ServiceLevels campaignLevels() {
        return theCampaign().serviceLevels();
    }

    private static LoadProfileDescription descriptionOf(LoadProfile profile) {
        return LoadProfileDescription.of(profile, theCampaign(), ScenarioDemand.of(profile),
                playersOf(profile), censusOf(profile), A_WARM_START, A_RUN, A_SUT_VERSION);
    }
}
