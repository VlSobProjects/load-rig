package loadrig.model.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Holds the criterion to the same strictness as a profile: the campaign's own file loads, and
 * every way of stating levels that would leave a capture judged by a figure nobody wrote down is
 * refused before a stand window is spent.
 */
class ServiceLevelsLoaderTest {

    @Test
    void theCampaignsOwnFileStatesAFigureForEveryBand() {
        ServiceLevels levels = ServiceLevelsLoader.load(Path.of("profiles", "service-levels.json"));

        for (ServiceLevelBand band : ServiceLevelBand.values()) {
            assertTrue(levels.levelOf(band) > 0, band.key() + " is stated");
        }
        assertEquals(95, ServiceLevels.PERCENTILE);
        assertTrue(levels.levelOf(ServiceLevelBand.RUNNING_A_REPORT)
                        > levels.levelOf(ServiceLevelBand.OPENING_A_LIST),
                "the one wait a person accepts is looser than the screen they live on");
    }

    @Test
    void aBandLeftOutIsRefusedByName(@TempDir Path directory) throws IOException {
        Path file = write(directory, """
                {
                  "percentile95Millis": {
                    "openingAList": 1000,
                    "openingOneTask": 1000,
                    "performingAnAction": 1000,
                    "writingANote": 1000,
                    "signingIn": 2000,
                    "signingOut": 1000
                  },
                  "hardCeilingMillis": 10000
                }
                """);

        IllegalArgumentException refusal =
                assertThrows(IllegalArgumentException.class, () -> ServiceLevelsLoader.load(file));

        assertTrue(refusal.getMessage().contains("runningAReport"),
                "the refusal names the band: " + refusal.getMessage());
    }

    @Test
    void aBandOutsideTheClosedListIsRefused(@TempDir Path directory) throws IOException {
        Path file = write(directory, """
                {
                  "percentile95Millis": {
                    "openingAList": 1000,
                    "openingOneTask": 1000,
                    "performingAnAction": 1000,
                    "writingANote": 1000,
                    "signingIn": 2000,
                    "signingOut": 1000,
                    "runningAReport": 3000,
                    "deletingATask": 1000
                  },
                  "hardCeilingMillis": 10000
                }
                """);

        assertThrows(IllegalArgumentException.class, () -> ServiceLevelsLoader.load(file));
    }

    @Test
    void anUnknownKeyIsRefusedRatherThanIgnored(@TempDir Path directory) throws IOException {
        Path file = write(directory, """
                {
                  "percentile": 99,
                  "percentile95Millis": {
                    "openingAList": 1000,
                    "openingOneTask": 1000,
                    "performingAnAction": 1000,
                    "writingANote": 1000,
                    "signingIn": 2000,
                    "signingOut": 1000,
                    "runningAReport": 3000
                  },
                  "hardCeilingMillis": 10000
                }
                """);

        assertThrows(IllegalArgumentException.class, () -> ServiceLevelsLoader.load(file),
                "the percentile is the criterion's shape and not a key a campaign may state");
    }

    @Test
    void aFigureAboveTheHardCeilingIsRefused(@TempDir Path directory) throws IOException {
        Path file = write(directory, """
                {
                  "percentile95Millis": {
                    "openingAList": 1000,
                    "openingOneTask": 1000,
                    "performingAnAction": 1000,
                    "writingANote": 1000,
                    "signingIn": 2000,
                    "signingOut": 1000,
                    "runningAReport": 12000
                  },
                  "hardCeilingMillis": 10000
                }
                """);

        IllegalArgumentException refusal =
                assertThrows(IllegalArgumentException.class, () -> ServiceLevelsLoader.load(file));

        assertTrue(refusal.getMessage().contains("ceiling"),
                "the refusal states the contradiction: " + refusal.getMessage());
    }

    private static Path write(Path directory, String json) throws IOException {
        Path file = directory.resolve("service-levels.json");
        Files.writeString(file, json);
        return file;
    }
}
