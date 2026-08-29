package loadrig.model.profile;

import java.util.Locale;

/**
 * The population-equilibrium invariant of the scenario specification, checked against a parsed
 * profile rather than trusted: creations minus settlements minus deletions, over the steady
 * window, must be small against the seeded volume. A profile that fails it would grow or drain
 * the table inside the window, and the capture would hold two regimes - a drift whose rising
 * latency is indistinguishable from the degrading index it was supposed to isolate.
 *
 * <p>The intensities are computed here from the mix, the users and the think time, never stated
 * by the profile, so the check and the run read the same numbers by construction.
 */
public final class EquilibriumCheck {

    /**
     * How many transitions a task takes to reach a settled status, on the transition table's
     * paths: complete then approve is two, refuse then acknowledge is two, a return with its
     * answering reassignment adds two, a reopen adds one and sends the task round again. The
     * specification weighs these into about two and a half; the exact weight is the scenarios'
     * behaviour, which is code, so the figure lives here beside the check and not in a profile.
     */
    static final double TRANSITIONS_PER_SETTLEMENT = 2.5;

    /**
     * The specification's own yardstick: the net drift over a window must stay under a tenth of
     * a percent of the seeded volume.
     */
    static final double MAX_DRIFT_SHARE_OF_VOLUME = 0.001;

    /**
     * The intensities a profile implies. What the profile description artifact and the LR-4
     * answer state as numbers per minute is this record, printed by the validation command.
     */
    public record Intensities(
            double stepsPerMinute,
            double creationsPerMinute,
            double settlementsPerMinute,
            double deletionsPerMinute,
            double notesPerMinute,
            double netDriftOverWindow) {
    }

    /**
     * Computes the intensities the profile implies and holds the invariant against them,
     * refusing loudly - with the numbers - before any load is applied.
     */
    public static Intensities check(LoadProfile profile) {
        Intensities intensities = intensitiesOf(profile);
        double driftShare = Math.abs(intensities.netDriftOverWindow()) / profile.seededVolume();
        if (driftShare > MAX_DRIFT_SHARE_OF_VOLUME) {
            throw new IllegalArgumentException(String.format(Locale.ENGLISH,
                    "the population does not hold: %.1f tasks created, %.1f settled and %.1f"
                            + " deleted per minute drift the table by %.0f rows over the"
                            + " %d-minute window, %.2f%% of the seeded volume of %d - the mix is"
                            + " wrong, and a capture under it would hold two regimes",
                    intensities.creationsPerMinute(), intensities.settlementsPerMinute(),
                    intensities.deletionsPerMinute(), intensities.netDriftOverWindow(),
                    profile.steadyWindowMinutes(), driftShare * 100, profile.seededVolume()));
        }
        return intensities;
    }

    /** The intensities alone, for a reader that reports rather than refuses. */
    public static Intensities intensitiesOf(LoadProfile profile) {
        double stepsPerMinute =
                profile.virtualUsers() * 60.0 / profile.thinkTime().meanSeconds();
        StepMix mix = profile.stepMix();
        double creations = stepsPerMinute * mix.creatingATaskPercent() / 100.0;
        double transitions = stepsPerMinute * mix.movingATaskPercent() / 100.0;
        double settlements = transitions / TRANSITIONS_PER_SETTLEMENT;
        double deletions = stepsPerMinute * mix.deletingATaskPercent() / 100.0;
        double notes = stepsPerMinute * mix.discussionPercent() / 100.0;
        double driftOverWindow =
                (creations - settlements - deletions) * profile.steadyWindowMinutes();
        return new Intensities(
                stepsPerMinute, creations, settlements, deletions, notes, driftOverWindow);
    }

    private EquilibriumCheck() {
    }
}
