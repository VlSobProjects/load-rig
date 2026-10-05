package loadrig.model.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Holds the loader's strictness: the repository's day profile loads with the specification's
 * numbers, and an unknown key, a missing key, a scenario name outside the closed list and a
 * value outside its range each refuse the profile with the reason.
 */
class ProfileLoaderTest {

    /** The repository's own day profile; the tests run from the project directory. */
    private static final Path DAY_PROFILE = Path.of("profiles", "day.json");

    @TempDir
    Path temp;

    @Test
    void theDayProfileCarriesTheSpecificationsNumbers() {
        LoadProfile profile = ProfileLoader.load(DAY_PROFILE);

        assertEquals("day", profile.name());
        assertEquals(10, profile.virtualUsers());
        assertEquals(30, profile.rampSeconds());
        assertEquals(10, profile.steadyWindowMinutes());
        assertEquals(3, profile.thinkTime().minSeconds());
        assertEquals(7, profile.thinkTime().maxSeconds());
        assertEquals(new StepMix(37, 22, 13, 12, 10, 5, 1), profile.stepMix());
        assertEquals(70, profile.hotSetSkew().taskOpensPercent());
        assertEquals(80, profile.hotSetSkew().notesPercent());
        assertEquals(2, profile.endingASessionPercent());
        assertEquals(Map.of(
                        ScenarioName.WORKER, 5,
                        ScenarioName.MANAGER, 3,
                        ScenarioName.ADMINISTRATOR, 1,
                        ScenarioName.DISCUSSION, 1),
                profile.scenarioPopulation());
    }

    @Test
    void anUnknownKeyIsRefused() throws IOException {
        Path profile = dayProfileWith("\"virtualUsers\": 10,",
                "\"virtualUsers\": 10, \"intensity\": 42,");
        IllegalArgumentException refusal =
                assertThrows(IllegalArgumentException.class, () -> ProfileLoader.load(profile));
        assertTrue(refusal.getMessage().contains("intensity"),
                "the refusal must name the unknown key: " + refusal.getMessage());
    }

    @Test
    void aProfileStillStatingASeededVolumeIsRefused() throws IOException {
        Path profile = dayProfileWith("\"virtualUsers\": 10,",
                "\"virtualUsers\": 10, \"seededVolume\": 50000,");
        IllegalArgumentException refusal =
                assertThrows(IllegalArgumentException.class, () -> ProfileLoader.load(profile));
        assertTrue(refusal.getMessage().contains("seededVolume"),
                "a profile that still states the stand's volume is refused by name, so no file"
                        + " carries the untrue figure past the loader: " + refusal.getMessage());
    }

    @Test
    void aMissingKeyIsRefusedByName() throws IOException {
        Path profile = dayProfileWith("\"virtualUsers\": 10,", "");
        IllegalArgumentException refusal =
                assertThrows(IllegalArgumentException.class, () -> ProfileLoader.load(profile));
        assertTrue(refusal.getMessage().contains("virtualUsers"),
                "the refusal must name the missing key: " + refusal.getMessage());
    }

    @Test
    void aMissingNestedKeyIsRefusedByItsPath() throws IOException {
        Path profile = dayProfileWith("\"minSeconds\": 3,", "");
        IllegalArgumentException refusal =
                assertThrows(IllegalArgumentException.class, () -> ProfileLoader.load(profile));
        assertTrue(refusal.getMessage().contains("thinkTime.minSeconds"),
                "the refusal must name the missing key: " + refusal.getMessage());
    }

    @Test
    void aScenarioNameOutsideTheClosedListIsRefused() throws IOException {
        Path profile = dayProfileWith("\"worker\": 5,", "\"stranger\": 5,");
        IllegalArgumentException refusal =
                assertThrows(IllegalArgumentException.class, () -> ProfileLoader.load(profile));
        assertTrue(refusal.getMessage().contains("stranger"),
                "the refusal must name the stranger scenario: " + refusal.getMessage());
    }

    @Test
    void anOmittedScenarioPopulationIsRefused() throws IOException {
        Path profile = dayProfileWith("\"administrator\": 1,", "");
        IllegalArgumentException refusal =
                assertThrows(IllegalArgumentException.class, () -> ProfileLoader.load(profile));
        assertTrue(refusal.getMessage().contains("administrator"),
                "the refusal must name the omitted scenario: " + refusal.getMessage());
    }

    @Test
    void aMixThatDoesNotAccountForEveryStepIsRefused() throws IOException {
        Path profile = dayProfileWith(
                "\"lookingAtAListPercent\": 37,", "\"lookingAtAListPercent\": 38,");
        IllegalArgumentException refusal =
                assertThrows(IllegalArgumentException.class, () -> ProfileLoader.load(profile));
        assertTrue(refusal.getMessage().contains("101"),
                "the refusal must state the wrong sum: " + refusal.getMessage());
    }

    @Test
    void aProfileWhoseSessionsNeverEndIsRefused() throws IOException {
        Path profile = dayProfileWith(
                "\"endingASessionPercent\": 2,", "\"endingASessionPercent\": 0,");
        IllegalArgumentException refusal =
                assertThrows(IllegalArgumentException.class, () -> ProfileLoader.load(profile));
        assertTrue(refusal.getMessage().contains("cold-start"),
                "the refusal must name what a model without sign-ins measures: "
                        + refusal.getMessage());
    }

    @Test
    void reversedThinkTimeBoundsAreRefused() throws IOException {
        Path profile = dayProfileWith("\"minSeconds\": 3,", "\"minSeconds\": 9,");
        assertThrows(IllegalArgumentException.class, () -> ProfileLoader.load(profile));
    }

    @Test
    void populationsThatDoNotSumToTheUsersAreRefused() throws IOException {
        Path profile = dayProfileWith("\"worker\": 5,", "\"worker\": 7,");
        IllegalArgumentException refusal =
                assertThrows(IllegalArgumentException.class, () -> ProfileLoader.load(profile));
        assertTrue(refusal.getMessage().contains("12"),
                "the refusal must state the wrong sum: " + refusal.getMessage());
    }

    /** The day profile with one edit, written where the test may scribble. */
    private Path dayProfileWith(String fragment, String replacement) throws IOException {
        String document = Files.readString(DAY_PROFILE);
        assertTrue(document.contains(fragment),
                "the day profile no longer contains " + fragment);
        Path edited = temp.resolve("edited.json");
        Files.writeString(edited, document.replace(fragment, replacement));
        return edited;
    }
}
