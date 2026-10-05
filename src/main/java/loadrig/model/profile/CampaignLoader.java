package loadrig.model.profile;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;

/**
 * Reads the campaign file into the vocabulary, as strictly as a profile is read: an unknown key, a
 * missing key, a band outside the closed list and a figure outside its range each refuse the run
 * loudly before any load is applied. Nothing has a default, because a value absorbed from a
 * default is a capture judged - or populated - by a figure nobody wrote down, and a capture nobody
 * can re-read later is the one thing the discipline behind these numbers exists to prevent.
 *
 * <p>One file serves a whole campaign and every profile in it. It is loaded once per run and
 * carried into the run's load-profile description as read.
 */
public final class CampaignLoader {

    /**
     * The document as the file spells it, with every figure boxed: a missing key arrives as null
     * and is refused by name, never absorbed as a primitive zero.
     */
    private record CampaignDocument(
            Integer playersPerSeat,
            ServiceLevelsDocument serviceLevels) {
    }

    private record ServiceLevelsDocument(
            Map<String, Integer> percentile95Millis,
            Integer hardCeilingMillis) {
    }

    public static Campaign load(Path campaignFile) {
        ObjectMapper mapper = new ObjectMapper()
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        CampaignDocument document;
        try {
            document = mapper.readValue(campaignFile.toFile(), CampaignDocument.class);
        } catch (IOException e) {
            if (e instanceof com.fasterxml.jackson.core.JacksonException) {
                throw new IllegalArgumentException("the campaign in " + campaignFile
                        + " is refused: " + e.getMessage(), e);
            }
            throw new UncheckedIOException(
                    "the campaign in " + campaignFile + " cannot be read", e);
        }
        return new Campaign(
                required(document.playersPerSeat(), "playersPerSeat"),
                serviceLevelsOf(required(document.serviceLevels(), "serviceLevels")));
    }

    private static ServiceLevels serviceLevelsOf(ServiceLevelsDocument document) {
        return new ServiceLevels(
                figuresOf(required(document.percentile95Millis(),
                        "serviceLevels.percentile95Millis")),
                required(document.hardCeilingMillis(), "serviceLevels.hardCeilingMillis"));
    }

    private static Map<ServiceLevelBand, Integer> figuresOf(Map<String, Integer> byKey) {
        EnumMap<ServiceLevelBand, Integer> figures = new EnumMap<>(ServiceLevelBand.class);
        byKey.forEach((key, millis) -> figures.put(
                ServiceLevelBand.ofKey(key),
                required(millis, "serviceLevels.percentile95Millis." + key)));
        return figures;
    }

    private static <T> T required(T value, String key) {
        if (value == null) {
            throw new IllegalArgumentException("the campaign states no " + key
                    + "; what a campaign fixes has no defaults, every key is stated");
        }
        return value;
    }

    private CampaignLoader() {
    }
}
