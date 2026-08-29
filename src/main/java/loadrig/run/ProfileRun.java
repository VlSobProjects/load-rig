package loadrig.run;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import loadrig.model.AccountPool;
import loadrig.model.profile.EquilibriumCheck;
import loadrig.model.profile.LoadProfile;
import loadrig.model.profile.ProfileLoader;
import loadrig.model.scenario.MixSurvivalCheck;
import loadrig.model.scenario.ProfilePlan;
import loadrig.model.scenario.RefusalLedger;
import loadrig.model.scenario.ScenarioWiring;
import loadrig.model.scenario.StarvationLedger;
import loadrig.registry.SessionRegistry;
import loadrig.registry.TaskRegistry;
import loadrig.registry.UserDirectory;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;
import us.abstracta.jmeter.javadsl.core.engines.EmbeddedJmeterEngine;

/**
 * The run harness: loads the stated profile, holds the invariants, assembles the plan over the
 * account pool and drives the stack with it, laying the capture artifacts into a directory of
 * this run's own - the load-profile description, the JTL result log and the run report. One
 * directory is one run; the capture consumes it whole and never pairs files by guessing at
 * names.
 *
 * <p>The artifacts stay on disk whatever the run's outcome, because a spoiled run is evidence
 * too; a run with refused samples still says so loudly and exits non-zero, so that a stand
 * window is never trusted by mistake.
 */
public final class ProfileRun {

    /** The names a capture finds inside a run's directory, fixed as the artifact contract. */
    static final String DESCRIPTION_FILE = "load-profile.json";
    static final String SAMPLES_FILE = "samples.jtl";
    static final String REPORT_FILE = "run-report.txt";

    private static final DateTimeFormatter RUN_STAMP =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("state the profile file: ProfileRun <path>");
            System.exit(2);
            return;
        }
        InjectorSettings.apply();
        RigConfiguration configuration = RigConfiguration.fromSystemProperties();
        Path profileFile = Path.of(args[0]);
        String runStamp = ZonedDateTime.now(ZoneOffset.UTC).format(RUN_STAMP);

        LoadProfile profile;
        EquilibriumCheck.Intensities intensities;
        StarvationLedger starvation = new StarvationLedger();
        ProfilePlan profilePlan;
        DslTestPlan plan;
        Path runDirectory;
        try {
            profile = ProfileLoader.load(profileFile);
            intensities = EquilibriumCheck.check(profile);
            MixSurvivalCheck.check(profile);
            runDirectory = configuration.resultsDirectory().toAbsolutePath()
                    .resolve(profile.name() + "-" + runStamp);
            ScenarioWiring wiring = new ScenarioWiring(
                    new SessionRegistry(AccountPool.members()),
                    new TaskRegistry(), new UserDirectory(), starvation,
                    profile.hotSetSkew(), runStamp, configuration.provisionedPassword());
            profilePlan = new ProfilePlan(configuration.baseUrl(), profile, wiring);
            plan = profilePlan.plan(runDirectory.toString(), SAMPLES_FILE);
        } catch (IllegalArgumentException e) {
            System.err.println("the run under " + profileFile + " is refused: " + e.getMessage());
            System.exit(1);
            return;
        }

        // Asked before the window opens and outside the test plan, so the description carries a
        // version the stand itself named and the capture pays no sample for the question.
        String sutVersion = configuration.statedSutVersion()
                .orElseGet(() -> SutVersion.askTheStand(configuration.baseUrl()));

        Files.createDirectories(runDirectory);
        LoadProfileDescription.of(profile, intensities, runStamp, sutVersion)
                .writeTo(runDirectory.resolve(DESCRIPTION_FILE));

        System.out.println("driving " + configuration.baseUrl() + " under the profile \""
                + profile.name() + "\" (" + profileFile + "): " + profile.virtualUsers()
                + " virtual users, ramp " + profile.rampSeconds() + " s, steady window "
                + profile.steadyWindowMinutes() + " min");
        System.out.println("the run's artifacts land in " + runDirectory);

        TestPlanStats stats = plan.runIn(new EmbeddedJmeterEngine());

        String report = report(profile, sutVersion, runStamp, stats, starvation,
                profilePlan.refusals());
        System.out.println(report);
        Files.writeString(runDirectory.resolve(REPORT_FILE), report);

        long errors = stats.overall().errorsCount();
        if (errors > 0) {
            throw new IOException("the run failed " + errors + " sample(s); a clean profile"
                    + " asks only what the application allows, so the capture in " + runDirectory
                    + " holds the script's failures beside the system's answers - read "
                    + REPORT_FILE + " and the log before trusting it");
        }
    }

    /**
     * What the run realized against what it intended: the counts, the refusals and the starvation
     * ledger. A skipped gate is the run's timing, not an error, but a drifted realized mix must
     * name where it drifted instead of keeping the drift a secret of the log.
     *
     * <p>A failed sample and a refusal are counted separately and named apart, because they are
     * not the same thing: a sample fails when its content assertion is not satisfied - which
     * includes the three screens that refuse under a successful status - while a refusal is a
     * status the application answered a request with.
     */
    private static String report(LoadProfile profile, String sutVersion, String runStamp,
            TestPlanStats stats, StarvationLedger starvation, RefusalLedger refusals) {
        StringBuilder report = new StringBuilder();
        report.append(String.format(Locale.ENGLISH,
                "the run %s-%s is complete: profile \"%s\", SUT version %s%n",
                profile.name(), runStamp, profile.name(), sutVersion));
        report.append(String.format(Locale.ENGLISH,
                "  %d virtual users, ramp %d s, steady window %d min%n",
                profile.virtualUsers(), profile.rampSeconds(), profile.steadyWindowMinutes()));
        report.append(String.format(Locale.ENGLISH, "  %d samples, %d of them failed%n",
                stats.overall().samplesCount(), stats.overall().errorsCount()));
        report.append(refusals(refusals));
        Map<String, Long> skips = starvation.skips();
        if (skips.isEmpty()) {
            report.append("  starvation ledger: every gate found its pick").append(System.lineSeparator());
        } else {
            report.append("  starvation ledger:").append(System.lineSeparator());
            skips.forEach((step, count) -> report.append(String.format(Locale.ENGLISH,
                    "    %s skipped %d time(s)%n", step, count)));
        }
        report.append("  artifacts: ").append(DESCRIPTION_FILE).append(", ")
                .append(SAMPLES_FILE).append(", ").append(REPORT_FILE)
                .append(System.lineSeparator());
        return report.toString();
    }

    /**
     * The refusals by the code the application answered with. The code that carries two causes is
     * broken into them here and nowhere else: the result log cannot hold the split, because
     * nothing in a sample says whether the request held a token, and only the injector knows.
     *
     * <p>The code no screen of the application produces gets a sentence of its own when it
     * appears. The scenarios follow the screens, so no person could have produced it: in a run
     * meant to be clean it is the rig's own defect, and a reader who takes it for user behaviour
     * reads the capture wrong.
     */
    private static String refusals(RefusalLedger refusals) {
        Map<Integer, Long> counts = refusals.counts();
        if (counts.isEmpty()) {
            return "  refusals: the application refused nothing" + System.lineSeparator();
        }
        StringBuilder lines = new StringBuilder();
        lines.append(String.format(Locale.ENGLISH, "  refusals: %d, by code%n", refusals.total()));
        counts.forEach((code, count) -> lines.append(
                String.format(Locale.ENGLISH, "    %d answered %d time(s)%s%n", code, count,
                        note(code, refusals))));
        return lines.toString();
    }

    private static String note(int code, RefusalLedger refusals) {
        if (code == RefusalLedger.FORBIDDEN) {
            return String.format(Locale.ENGLISH,
                    " - %d asked holding a token, an action the actor does not own; %d asked"
                            + " without one, a session that lost its token",
                    refusals.forbiddenCarryingAToken(), refusals.forbiddenCarryingNone());
        }
        if (code == RefusalLedger.NEVER_PRODUCED_BY_A_SCREEN) {
            return " - no screen of the application produces this request, so no person made it:"
                    + " in a run meant to be clean it is the rig's own defect";
        }
        return "";
    }

    private ProfileRun() {
    }
}
