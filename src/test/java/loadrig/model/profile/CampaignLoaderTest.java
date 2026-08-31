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
 * Holds the campaign to the same strictness as a profile: the repository's own campaign file
 * loads, and every way of stating it that would leave a capture judged - or played - by a figure
 * nobody wrote down is refused before a stand window is spent.
 */
class CampaignLoaderTest {

    @Test
    void theCampaignsOwnFileStatesAFigureForEveryBand() {
        ServiceLevels levels = theRepositorysCampaign().serviceLevels();

        for (ServiceLevelBand band : ServiceLevelBand.values()) {
            assertTrue(levels.levelOf(band) > 0, band.key() + " is stated");
        }
        assertEquals(95, ServiceLevels.PERCENTILE);
        assertTrue(levels.levelOf(ServiceLevelBand.RUNNING_A_REPORT)
                        > levels.levelOf(ServiceLevelBand.OPENING_A_LIST),
                "the one wait a person accepts is looser than the screen they live on");
    }

    @Test
    void theCampaignStatesHowManyAccountsEachSeatRotatesThrough() {
        assertTrue(theRepositorysCampaign().playersPerSeat() > 1,
                "the working set of a run is the people who play it, and a rotation as narrow as"
                        + " the seats is the population the calibration probe measured (DR-8)");
    }

    @Test
    void aRotationLeftOutIsRefusedByName(@TempDir Path directory) throws IOException {
        Path file = write(directory, campaign("", theSevenBands()));

        IllegalArgumentException refusal =
                assertThrows(IllegalArgumentException.class, () -> CampaignLoader.load(file));

        assertTrue(refusal.getMessage().contains("playersPerSeat"),
                "the refusal names what was left out: " + refusal.getMessage());
    }

    @Test
    void aRotationOfLessThanOneAccountASeatIsRefused(@TempDir Path directory) throws IOException {
        Path file = write(directory, campaign("\"playersPerSeat\": 0,", theSevenBands()));

        assertThrows(IllegalArgumentException.class, () -> CampaignLoader.load(file),
                "a campaign whose seats rotate through no account plays nobody");
    }

    @Test
    void aBandLeftOutIsRefusedByName(@TempDir Path directory) throws IOException {
        Path file = write(directory, campaign("\"playersPerSeat\": 3,", """
                "openingAList": 1000,
                      "openingOneTask": 1000,
                      "performingAnAction": 1000,
                      "writingANote": 1000,
                      "signingIn": 2000,
                      "signingOut": 1000"""));

        IllegalArgumentException refusal =
                assertThrows(IllegalArgumentException.class, () -> CampaignLoader.load(file));

        assertTrue(refusal.getMessage().contains("runningAReport"),
                "the refusal names the band: " + refusal.getMessage());
    }

    @Test
    void aBandOutsideTheClosedListIsRefused(@TempDir Path directory) throws IOException {
        Path file = write(directory, campaign("\"playersPerSeat\": 3,",
                theSevenBands() + ",\n                      \"deletingATask\": 1000"));

        assertThrows(IllegalArgumentException.class, () -> CampaignLoader.load(file));
    }

    @Test
    void anUnknownKeyIsRefusedRatherThanIgnored(@TempDir Path directory) throws IOException {
        Path file = write(directory,
                campaign("\"playersPerSeat\": 3,\n                  \"percentile\": 99,",
                        theSevenBands()));

        assertThrows(IllegalArgumentException.class, () -> CampaignLoader.load(file),
                "the percentile is the criterion's shape and not a key a campaign may state");
    }

    @Test
    void aFigureAboveTheHardCeilingIsRefused(@TempDir Path directory) throws IOException {
        Path file = write(directory, campaign("\"playersPerSeat\": 3,",
                theSevenBands().replace("\"runningAReport\": 3000", "\"runningAReport\": 12000")));

        IllegalArgumentException refusal =
                assertThrows(IllegalArgumentException.class, () -> CampaignLoader.load(file));

        assertTrue(refusal.getMessage().contains("ceiling"),
                "the refusal states the contradiction: " + refusal.getMessage());
    }

    private static Campaign theRepositorysCampaign() {
        return CampaignLoader.load(Path.of("profiles", "campaign.json"));
    }

    private static String theSevenBands() {
        return """
                "openingAList": 1000,
                      "openingOneTask": 1000,
                      "performingAnAction": 1000,
                      "writingANote": 1000,
                      "signingIn": 2000,
                      "signingOut": 1000,
                      "runningAReport": 3000""";
    }

    /** A campaign document as a file spells it, with the rotation and the bands stated apart. */
    private static String campaign(String rotation, String bands) {
        return """
                {
                  %s
                  "serviceLevels": {
                    "percentile95Millis": {
                      %s
                    },
                    "hardCeilingMillis": 10000
                  }
                }
                """.formatted(rotation, bands);
    }

    private static Path write(Path directory, String json) throws IOException {
        Path file = directory.resolve("campaign.json");
        Files.writeString(file, json);
        return file;
    }
}
