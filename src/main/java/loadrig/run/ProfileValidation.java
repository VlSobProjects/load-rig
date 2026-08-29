package loadrig.run;

import java.nio.file.Path;
import java.util.Locale;
import loadrig.model.profile.EquilibriumCheck;
import loadrig.model.profile.LoadProfile;
import loadrig.model.profile.ProfileLoader;

/**
 * Parses and checks a profile file without applying any load, so that a broken profile is found
 * before a stand window is spent on it. Running it is part of preparing a campaign.
 *
 * <p>It prints the intensities the profile implies - the numbers the profile deliberately does
 * not state, because they are computed from the mix, the users and the think time - and holds
 * the equilibrium invariant against them. A refused profile is reported with the reason and a
 * non-zero exit.
 */
public final class ProfileValidation {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("state the profile file: ProfileValidation <path>");
            System.exit(2);
            return;
        }
        Path profileFile = Path.of(args[0]);
        try {
            LoadProfile profile = ProfileLoader.load(profileFile);
            EquilibriumCheck.Intensities intensities = EquilibriumCheck.check(profile);
            System.out.println("the profile \"" + profile.name() + "\" (" + profileFile
                    + ") holds:");
            System.out.printf(Locale.ENGLISH, "  %d virtual users, ramp %d s, steady window %d min,"
                            + " think time %d..%d s%n",
                    profile.virtualUsers(), profile.rampSeconds(),
                    profile.steadyWindowMinutes(), profile.thinkTime().minSeconds(),
                    profile.thinkTime().maxSeconds());
            System.out.printf(Locale.ENGLISH, "  %.1f steps per minute, of them %.1f creations,"
                            + " %.1f settlements, %.1f deletions, %.1f notes%n",
                    intensities.stepsPerMinute(), intensities.creationsPerMinute(),
                    intensities.settlementsPerMinute(), intensities.deletionsPerMinute(),
                    intensities.notesPerMinute());
            System.out.printf(Locale.ENGLISH, "  net drift over the window: %.1f rows against a seeded volume"
                            + " of %d - the population holds%n",
                    intensities.netDriftOverWindow(), profile.seededVolume());
        } catch (IllegalArgumentException e) {
            System.err.println("the profile " + profileFile + " is refused: " + e.getMessage());
            System.exit(1);
        }
    }

    private ProfileValidation() {
    }
}
