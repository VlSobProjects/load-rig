package loadrig.run;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import loadrig.model.profile.EquilibriumCheck;
import loadrig.model.profile.LoadProfile;
import loadrig.model.profile.ProfileLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Proves the description artifact against the repository's own day profile: the written JSON
 * carries the profile's stated facts, the run's identity and the computed target rates - the
 * same numbers the equilibrium check computes, so the description can never state targets the
 * run did not obey.
 */
class LoadProfileDescriptionTest {

    private static final String A_RUN = "20260829-120000";
    private static final String A_SUT_VERSION = "unknown";

    @Test
    void theDescriptionStatesTheProfileTheRunAndTheComputedTargets(@TempDir Path directory)
            throws IOException {
        LoadProfile profile = ProfileLoader.load(Path.of("profiles", "day.json"));
        EquilibriumCheck.Intensities intensities = EquilibriumCheck.intensitiesOf(profile);
        Path file = directory.resolve("load-profile.json");

        LoadProfileDescription.of(profile, intensities, A_RUN, A_SUT_VERSION).writeTo(file);

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
        assertEquals(profile.seededVolume(), description.get("seededVolume").asInt());
        assertEquals(profile.thinkTime().minSeconds(),
                description.get("thinkTime").get("minSeconds").asInt());
        assertEquals(profile.stepMix().lookingAtAListPercent(),
                description.get("stepMix").get("lookingAtAListPercent").asInt());
        assertEquals(profile.hotSetSkew().notesPercent(),
                description.get("hotSetSkew").get("notesPercent").asInt());
    }

    @Test
    void theTargetRatesAreTheIntensitiesTheProfileImplies(@TempDir Path directory)
            throws IOException {
        LoadProfile profile = ProfileLoader.load(Path.of("profiles", "day.json"));
        EquilibriumCheck.Intensities intensities = EquilibriumCheck.intensitiesOf(profile);
        Path file = directory.resolve("load-profile.json");

        LoadProfileDescription.of(profile, intensities, A_RUN, A_SUT_VERSION).writeTo(file);

        JsonNode targets = new ObjectMapper().readTree(file.toFile())
                .get("targetRatesPerMinute");
        assertEquals(intensities.stepsPerMinute(), targets.get("steps").asDouble(), 1e-9);
        assertEquals(intensities.creationsPerMinute(),
                targets.get("creatingATask").asDouble(), 1e-9);
        assertEquals(intensities.deletionsPerMinute(),
                targets.get("deletingATask").asDouble(), 1e-9);
        assertEquals(intensities.notesPerMinute(), targets.get("discussion").asDouble(), 1e-9);
        assertEquals(intensities.settlementsPerMinute(),
                targets.get("settlements").asDouble(), 1e-9);
        assertEquals(intensities.netDriftOverWindow(),
                targets.get("netDriftOverWindow").asDouble(), 1e-9);
        double statedOperations = targets.get("lookingAtAList").asDouble()
                + targets.get("openingOneTask").asDouble()
                + targets.get("movingATask").asDouble()
                + targets.get("discussion").asDouble()
                + targets.get("runningAReport").asDouble()
                + targets.get("creatingATask").asDouble()
                + targets.get("deletingATask").asDouble();
        assertEquals(intensities.stepsPerMinute(), statedOperations, 1e-9);
    }

    @Test
    void theScenarioPopulationsAreKeyedByTheScenarioNames(@TempDir Path directory)
            throws IOException {
        LoadProfile profile = ProfileLoader.load(Path.of("profiles", "day.json"));
        Path file = directory.resolve("load-profile.json");

        LoadProfileDescription
                .of(profile, EquilibriumCheck.intensitiesOf(profile), A_RUN, A_SUT_VERSION)
                .writeTo(file);

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
}
