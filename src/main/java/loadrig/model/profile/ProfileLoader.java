package loadrig.model.profile;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;

/**
 * Reads a profile file into the vocabulary, strictly. An unknown key, a missing key, a scenario
 * name outside the closed list and a value outside its range each refuse the profile loudly with
 * the key by name; no key has a default on the capture path, because a typo absorbed by a
 * default produces a false capture that nothing announces - the failure mode DR-2 names.
 *
 * <p>The file handed to this loader is the same file the run carries into the capture as its
 * load-profile description, as received: one source, so the description and the applied profile
 * cannot drift apart.
 */
public final class ProfileLoader {

    /**
     * The document as the file spells it, with every field boxed: a missing key arrives as null
     * and is refused by name, never absorbed as a primitive zero.
     */
    private record ProfileDocument(
            String name,
            Integer virtualUsers,
            Integer rampSeconds,
            Integer steadyWindowMinutes,
            ThinkTimeDocument thinkTime,
            StepMixDocument stepMix,
            HotSetSkewDocument hotSetSkew,
            Integer seededVolume,
            Map<String, Integer> scenarioPopulation) {
    }

    private record ThinkTimeDocument(Integer minSeconds, Integer maxSeconds) {
    }

    private record StepMixDocument(
            Integer lookingAtAListPercent,
            Integer openingOneTaskPercent,
            Integer movingATaskPercent,
            Integer discussionPercent,
            Integer runningAReportPercent,
            Integer creatingATaskPercent,
            Integer deletingATaskPercent) {
    }

    private record HotSetSkewDocument(Integer taskOpensPercent, Integer notesPercent) {
    }

    public static LoadProfile load(Path profileFile) {
        ObjectMapper mapper = new ObjectMapper()
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        ProfileDocument document;
        try {
            document = mapper.readValue(profileFile.toFile(), ProfileDocument.class);
        } catch (IOException e) {
            if (e instanceof com.fasterxml.jackson.core.JacksonException) {
                throw new IllegalArgumentException(
                        "the profile " + profileFile + " is refused: " + e.getMessage(), e);
            }
            throw new UncheckedIOException(
                    "the profile " + profileFile + " cannot be read", e);
        }
        return profileOf(document);
    }

    private static LoadProfile profileOf(ProfileDocument document) {
        ThinkTimeDocument thinkTime = required(document.thinkTime(), "thinkTime");
        StepMixDocument mix = required(document.stepMix(), "stepMix");
        HotSetSkewDocument skew = required(document.hotSetSkew(), "hotSetSkew");
        return new LoadProfile(
                required(document.name(), "name"),
                required(document.virtualUsers(), "virtualUsers"),
                required(document.rampSeconds(), "rampSeconds"),
                required(document.steadyWindowMinutes(), "steadyWindowMinutes"),
                new LoadProfile.ThinkTime(
                        required(thinkTime.minSeconds(), "thinkTime.minSeconds"),
                        required(thinkTime.maxSeconds(), "thinkTime.maxSeconds")),
                new StepMix(
                        required(mix.lookingAtAListPercent(), "stepMix.lookingAtAListPercent"),
                        required(mix.openingOneTaskPercent(), "stepMix.openingOneTaskPercent"),
                        required(mix.movingATaskPercent(), "stepMix.movingATaskPercent"),
                        required(mix.discussionPercent(), "stepMix.discussionPercent"),
                        required(mix.runningAReportPercent(), "stepMix.runningAReportPercent"),
                        required(mix.creatingATaskPercent(), "stepMix.creatingATaskPercent"),
                        required(mix.deletingATaskPercent(), "stepMix.deletingATaskPercent")),
                new LoadProfile.HotSetSkew(
                        required(skew.taskOpensPercent(), "hotSetSkew.taskOpensPercent"),
                        required(skew.notesPercent(), "hotSetSkew.notesPercent")),
                required(document.seededVolume(), "seededVolume"),
                populationOf(required(document.scenarioPopulation(), "scenarioPopulation")));
    }

    private static Map<ScenarioName, Integer> populationOf(Map<String, Integer> byKey) {
        EnumMap<ScenarioName, Integer> population = new EnumMap<>(ScenarioName.class);
        byKey.forEach((key, users) -> population.put(
                ScenarioName.ofKey(key),
                required(users, "scenarioPopulation." + key)));
        return population;
    }

    private static <T> T required(T value, String key) {
        if (value == null) {
            throw new IllegalArgumentException("the profile states no " + key
                    + "; a capture profile has no defaults, every key is stated");
        }
        return value;
    }

    private ProfileLoader() {
    }
}
