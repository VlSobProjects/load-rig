package loadrig.run;

import java.nio.file.Path;
import java.util.Locale;
import loadrig.model.profile.LoadProfile;
import loadrig.model.profile.ProfileLoader;
import loadrig.model.scenario.MixSurvivalCheck;
import loadrig.model.scenario.ScenarioDemand;
import loadrig.model.scenario.SeatedAccounts;
import loadrig.model.scenario.WarmStartCensus;

/**
 * Parses and checks a profile file without applying any load, so that a broken profile is found
 * before a stand window is spent on it. Running it is part of preparing a campaign.
 *
 * <p>It prints the rates the profile implies - the numbers the profile deliberately does not
 * state, because the scenarios' own weights, the populations and the think time give them exactly
 * - and the census a warm start would have to establish before the window, bucket by bucket. It
 * holds the mix against the scenarios as they are coded and the census against what a warm start
 * may bring about. A refused profile is reported with the reason and a non-zero exit.
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
            ScenarioDemand demand = ScenarioDemand.of(profile);
            MixSurvivalCheck.check(profile);
            WarmStartCensus census = WarmStartCensus.of(profile, SeatedAccounts.of(profile));
            census.check();
            System.out.println("the profile \"" + profile.name() + "\" (" + profileFile
                    + ") holds:");
            System.out.printf(Locale.ENGLISH, "  %d virtual users, ramp %d s, steady window %d min,"
                            + " think time %d..%d s%n",
                    profile.virtualUsers(), profile.rampSeconds(),
                    profile.steadyWindowMinutes(), profile.thinkTime().minSeconds(),
                    profile.thinkTime().maxSeconds());
            System.out.printf(Locale.ENGLISH, "  %.1f steps per minute, of them %.1f creations,"
                            + " %.1f settlements, %.1f deletions, %.1f notes%n",
                    demand.stepsPerMinute(), demand.creationsPerMinute(),
                    demand.settlementsPerMinute(), demand.deletionsPerMinute(),
                    demand.notesPerMinute());
            System.out.printf(Locale.ENGLISH,
                    "  the window adds %.0f row(s) to the task table%n",
                    demand.tableGrowthOverWindow());
            System.out.printf(Locale.ENGLISH,
                    "  the census a warm start must establish is %d task(s) over %d bucket(s):%n",
                    census.tasks(), census.buckets().size());
            System.out.print(census.statement());
            System.out.println("  the mix survives the scenario populations");
        } catch (IllegalArgumentException e) {
            System.err.println("the profile " + profileFile + " is refused: " + e.getMessage());
            System.exit(1);
        }
    }

    private ProfileValidation() {
    }
}
