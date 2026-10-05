package loadrig.model.scenario;

import java.util.Locale;
import java.util.Map;
import loadrig.model.profile.LoadProfile;
import loadrig.model.profile.ScenarioName;
import loadrig.model.profile.StepMix;
import loadrig.model.step.StepKind;

/**
 * Holds the profile's step mix against the scenarios as they are actually coded, before any
 * load. The scenarios state their own step weights - business shape is code, by DR-2's boundary
 * - and the profile states the populations; whether the populations over these scenarios
 * reproduce the profile's mix is a computable fact, so it is computed and refused on drift
 * rather than trusted. This is the mix-survival question of the specification, answered with
 * numbers.
 *
 * <p>The computation reads every virtual user as producing steps at the same rate - one step
 * per think time, which is exactly the equilibrium check's reading - so a scenario's share of
 * the global mix is its population times its per-kind step shares.
 */
public final class MixSurvivalCheck {

    /**
     * How far a computed share may sit from the stated one, in percentage points: the mix is
     * stated in whole percent, so half a point is the reading error of the statement itself.
     */
    static final double TOLERANCE_PERCENT_POINTS = 0.5;

    /** Refuses the profile loudly when the populations do not reproduce its mix. */
    public static void check(LoadProfile profile) {
        Map<ScenarioName, Map<StepKind, Integer>> weights = Scenarios.stepWeights();
        for (StepKind kind : StepKind.values()) {
            // The acts by which a session begins and ends are no rows of the mix and are held
            // against nothing here: they take no share from the business steps, and the profile
            // states their intensity separately.
            if (kind == StepKind.SESSION_KEEPING || kind == StepKind.GIVING_UP_A_SESSION) {
                continue;
            }
            double computed = computedShare(profile, weights, kind);
            int stated = statedShare(profile.stepMix(), kind);
            if (Math.abs(computed - stated) > TOLERANCE_PERCENT_POINTS) {
                throw new IllegalArgumentException(String.format(Locale.ENGLISH,
                        "the mix does not survive the populations: the scenarios as coded give"
                                + " %.1f%% of all steps to %s while the profile states %d%%"
                                + " - either the populations or the scenarios' own weights"
                                + " changed, and a capture under this profile would describe a"
                                + " mix the run did not apply",
                        computed, kind, stated));
            }
        }
    }

    /** The share of all steps the scenarios give the kind, in percent. */
    static double computedShare(LoadProfile profile,
            Map<ScenarioName, Map<StepKind, Integer>> weights, StepKind kind) {
        double share = 0;
        for (ScenarioName name : ScenarioName.values()) {
            Map<StepKind, Integer> table = weights.get(name);
            int total = table.values().stream().mapToInt(Integer::intValue).sum();
            share += profile.scenarioPopulation().get(name)
                    * 100.0 * table.getOrDefault(kind, 0) / total;
        }
        return share / profile.virtualUsers();
    }

    private static int statedShare(StepMix mix, StepKind kind) {
        return switch (kind) {
            case LOOKING_AT_A_LIST -> mix.lookingAtAListPercent();
            case OPENING_ONE_TASK -> mix.openingOneTaskPercent();
            case MOVING_A_TASK -> mix.movingATaskPercent();
            case DISCUSSION -> mix.discussionPercent();
            case RUNNING_A_REPORT -> mix.runningAReportPercent();
            case CREATING_A_TASK -> mix.creatingATaskPercent();
            case DELETING_A_TASK -> mix.deletingATaskPercent();
            case SESSION_KEEPING, GIVING_UP_A_SESSION -> throw new IllegalArgumentException(
                    kind + " is no row of the mix: a session begins and ends beside the business"
                            + " steps and takes no share from them");
        };
    }

    private MixSurvivalCheck() {
    }
}
