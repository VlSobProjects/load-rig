package loadrig.model.profile;

import java.util.EnumMap;
import java.util.Map;

/**
 * One load profile: everything a run varies without a rebuild, parsed from the external JSON
 * file that is also the run's load-profile description artifact. The types fix what a profile
 * may state; the numbers are the file's.
 *
 * <p>Every field is validated here, so that a profile object that exists is a profile the run
 * may trust. What is deliberately absent: intensities. The creations, settlements, deletions
 * and notes per minute are computed from the mix, the users and the think time by
 * {@link EquilibriumCheck}, never stated, because two sources of one fact drift apart and no
 * reader of the capture can tell which one the run obeyed.
 */
public record LoadProfile(
        String name,
        int virtualUsers,
        int rampSeconds,
        int steadyWindowMinutes,
        ThinkTime thinkTime,
        StepMix stepMix,
        HotSetSkew hotSetSkew,
        int seededVolume,
        Map<ScenarioName, Integer> scenarioPopulation) {

    /** The pause a virtual user thinks between steps, drawn per step from these bounds. */
    public record ThinkTime(int minSeconds, int maxSeconds) {

        public ThinkTime {
            if (minSeconds <= 0) {
                throw new IllegalArgumentException("a think time of " + minSeconds
                        + " seconds is no think time; dropping it deletes the saturated-pool"
                        + " fault surface, the specification's third false finding");
            }
            if (maxSeconds < minSeconds) {
                throw new IllegalArgumentException("the think time bounds are reversed: "
                        + minSeconds + ".." + maxSeconds + " seconds");
            }
        }

        public double meanSeconds() {
            return (minSeconds + maxSeconds) / 2.0;
        }
    }

    /**
     * How much attention lands on the hot set - the open tasks due soon that several sessions
     * work at once. The skew, not the volume alone, decides how much disk work the wide reads
     * cause, and a uniform pick is the specification's first false finding.
     */
    public record HotSetSkew(int taskOpensPercent, int notesPercent) {

        public HotSetSkew {
            requirePercent("taskOpens", taskOpensPercent);
            requirePercent("notes", notesPercent);
        }

        private static void requirePercent(String share, int percent) {
            if (percent < 0 || percent > 100) {
                throw new IllegalArgumentException("the hot-set share of " + share
                        + " must be a percentage, and it is " + percent);
            }
        }
    }

    public LoadProfile {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("a profile must carry a name; the name goes into"
                    + " the capture and two captures under different profiles must say so");
        }
        requirePositive("virtualUsers", virtualUsers);
        if (rampSeconds < 0) {
            throw new IllegalArgumentException("the ramp cannot be negative, and it is "
                    + rampSeconds + " seconds");
        }
        requirePositive("steadyWindowMinutes", steadyWindowMinutes);
        requirePositive("seededVolume", seededVolume);
        scenarioPopulation = populationOfEveryScenario(scenarioPopulation, virtualUsers);
    }

    private static Map<ScenarioName, Integer> populationOfEveryScenario(
            Map<ScenarioName, Integer> population, int virtualUsers) {
        int sum = 0;
        EnumMap<ScenarioName, Integer> copy = new EnumMap<>(ScenarioName.class);
        for (ScenarioName scenario : ScenarioName.values()) {
            Integer users = population.get(scenario);
            if (users == null) {
                throw new IllegalArgumentException("the profile states no population for the "
                        + scenario.key() + " scenario; a scenario a profile leaves out is stated"
                        + " with a population of zero, never omitted");
            }
            if (users < 0) {
                throw new IllegalArgumentException("the population of the " + scenario.key()
                        + " scenario cannot be negative, and it is " + users);
            }
            copy.put(scenario, users);
            sum += users;
        }
        if (sum != virtualUsers) {
            throw new IllegalArgumentException("the scenario populations sum to " + sum
                    + " users while the profile states " + virtualUsers
                    + " virtual users; every user runs exactly one scenario");
        }
        return Map.copyOf(copy);
    }

    private static void requirePositive(String key, int value) {
        if (value <= 0) {
            throw new IllegalArgumentException(key + " must be positive, and it is " + value);
        }
    }
}
