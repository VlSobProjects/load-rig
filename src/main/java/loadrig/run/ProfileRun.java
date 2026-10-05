package loadrig.run;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import loadrig.model.profile.Campaign;
import loadrig.model.profile.CampaignLoader;
import loadrig.model.profile.LoadProfile;
import loadrig.model.profile.ProfileLoader;
import loadrig.model.profile.ServiceLevels;
import loadrig.model.scenario.MixSurvivalCheck;
import loadrig.model.scenario.PlayingAccounts;
import loadrig.model.scenario.ProfilePlan;
import loadrig.model.scenario.RefusalLedger;
import loadrig.model.scenario.ScenarioDemand;
import loadrig.model.scenario.ScenarioWiring;
import loadrig.model.scenario.ServiceLevelLedger;
import loadrig.model.scenario.StarvationLedger;
import loadrig.model.scenario.WarmStartCensus;
import loadrig.registry.SessionRegistry;
import loadrig.registry.TaskRegistry;
import loadrig.registry.UserDirectory;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;
import us.abstracta.jmeter.javadsl.core.engines.EmbeddedJmeterEngine;

/**
 * The run harness: loads the stated profile, holds the invariants, assembles the plan over the
 * account pool, brings the stand to the population the profile needs and drives the stack with it,
 * laying the capture artifacts into a directory of this run's own - the load-profile description,
 * the JTL result log and the run report. One directory is one run; the capture consumes it whole
 * and never pairs files by guessing at names.
 *
 * <p>The warm start runs before the window and outside the test plan (DR-6), so the result log
 * holds the window alone and the mix holds from its first minute instead of building up while the
 * registry learns the stand.
 *
 * <p>The artifacts stay on disk whatever the run's outcome, because a spoiled run is evidence
 * too; a run with refused samples still says so loudly and exits non-zero, so that a stand
 * window is never trusted by mistake. A breached service level is not such a case: it is a
 * finding about the system, published in the description's levels and the report's verdict, and
 * the faulted variants this rig produces are meant to breach.
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
        Campaign campaign;
        ScenarioDemand demand;
        PlayingAccounts playing;
        WarmStartCensus census;
        StarvationLedger starvation = new StarvationLedger();
        RefusalLedger refusals = new RefusalLedger();
        ServiceLevelLedger waits = new ServiceLevelLedger();
        TaskRegistry tasks = new TaskRegistry();
        UserDirectory directory = new UserDirectory();
        DslTestPlan plan;
        Path runDirectory;
        try {
            profile = ProfileLoader.load(profileFile);
            campaign = CampaignLoader.load(configuration.campaignFile());
            demand = ScenarioDemand.of(profile);
            MixSurvivalCheck.check(profile);
            playing = PlayingAccounts.of(profile, campaign);
            census = WarmStartCensus.of(profile, playing);
            census.check();
            runDirectory = configuration.resultsDirectory().toAbsolutePath()
                    .resolve(profile.name() + "-" + runStamp);
            // The registry holds the accounts this run plays and no others: the choice rule that
            // decides who signs in next picks among them, so a seat is never taken by an account
            // the census left unstocked (DR-8).
            ScenarioWiring wiring = new ScenarioWiring(
                    new SessionRegistry(playing.members()), playing,
                    tasks, directory, starvation, refusals, waits,
                    profile.hotSetSkew(), runStamp, configuration.provisionedPassword());
            plan = new ProfilePlan(configuration.baseUrl(), profile, wiring)
                    .plan(runDirectory.toString(), SAMPLES_FILE);
        } catch (IllegalArgumentException e) {
            System.err.println("the run under " + profileFile + " is refused: " + e.getMessage());
            System.exit(1);
            return;
        }

        // The population the profile needs, established before the window and outside the test
        // plan: the registry starts the run knowing the stand, and no sample of the capture is
        // spent bringing the stand about (DR-6).
        System.out.println("the profile \"" + profile.name() + "\" is played by "
                + playing.members().size() + " account(s) of the pool, "
                + campaign.playersPerSeat() + " per seat; its census is "
                + census.tasks() + " task(s) over " + census.buckets().size() + " bucket(s)");
        WarmStart.Result warmStart = new WarmStart(configuration.baseUrl(),
                configuration.provisionedPassword(), runStamp, census, tasks, directory)
                .bringAbout();
        System.out.println("the stand holds " + warmStart.tasksOnTheStand() + " task(s); the warm"
                + " start read " + warmStart.read() + " of them and created "
                + warmStart.created());

        // Asked before the window opens and outside the test plan, so the description carries a
        // version the stand itself named and the capture pays no sample for the question.
        String sutVersion = configuration.statedSutVersion()
                .orElseGet(() -> SutVersion.askTheStand(configuration.baseUrl()));

        Files.createDirectories(runDirectory);
        LoadProfileDescription
                .of(profile, campaign, demand, playing, census, warmStart, runStamp, sutVersion)
                .writeTo(runDirectory.resolve(DESCRIPTION_FILE));

        System.out.println("driving " + configuration.baseUrl() + " under the profile \""
                + profile.name() + "\" (" + profileFile + "): " + profile.virtualUsers()
                + " virtual users, ramp " + profile.rampSeconds() + " s, steady window "
                + profile.steadyWindowMinutes() + " min");
        System.out.println("the run's artifacts land in " + runDirectory
                + "; it is judged by the service levels of the campaign in "
                + configuration.campaignFile());

        TestPlanStats stats = plan.runIn(new EmbeddedJmeterEngine());

        String report = report(profile, campaign, playing, census, warmStart, sutVersion, runStamp,
                stats, starvation, refusals, waits.verdict(campaign.serviceLevels()));
        System.out.println(report);
        Files.writeString(runDirectory.resolve(REPORT_FILE), report);

        long errors = stats.overall().errorsCount();
        if (errors > 0) {
            throw new IOException("the run failed " + errors + " sample(s); a clean profile"
                    + " asks only what the application allows, so the capture in " + runDirectory
                    + " holds the script's failures beside the system's answers - read "
                    + REPORT_FILE + " and the log before trusting it");
        }
        long vanished = refusals.tasksVanishedUnderASession();
        long intended = intendedDeletions(profile, demand);
        if (vanished > intended) {
            throw new IOException("the run lost " + vanished + " task(s) under a session while"
                    + " the profile intended to delete " + intended + " over the window; a task"
                    + " deleted between a session's pick and its request is a race the capture may"
                    + " carry, but more of them than the profile meant to remove is not a race -"
                    + " the capture in " + runDirectory + " is not a clean one");
        }
    }

    /**
     * How many tasks the profile means to remove over the steady window. It is the bound the
     * tolerated losses are held against: a session can only lose a task somebody deleted, so
     * losing more than were meant to be deleted says the cause is something else. Rounded up,
     * because a bound stated below the intention would spoil a run for behaving as asked.
     */
    static long intendedDeletions(LoadProfile profile, ScenarioDemand demand) {
        return (long) Math.ceil(demand.deletionsPerMinute() * profile.steadyWindowMinutes());
    }

    /**
     * What the run realized against what it intended: the counts, the refusals, the waits against
     * the service levels and the starvation ledger. A skipped gate is the run's timing, not an
     * error, but a drifted realized mix must name where it drifted instead of keeping the drift a
     * secret of the log.
     *
     * <p>A failed sample and a refusal are counted separately and named apart, because they are
     * not the same thing: a sample fails when its content assertion is not satisfied - which
     * includes the three screens that refuse under a successful status - while a refusal is a
     * status the application answered a request with.
     */
    private static String report(LoadProfile profile, Campaign campaign, PlayingAccounts playing,
            WarmStartCensus census, WarmStart.Result warmStart, String sutVersion, String runStamp,
            TestPlanStats stats, StarvationLedger starvation, RefusalLedger refusals,
            ServiceLevelLedger.Verdict waits) {
        StringBuilder report = new StringBuilder();
        report.append(String.format(Locale.ENGLISH,
                "the run %s-%s is complete: profile \"%s\", SUT version %s%n",
                profile.name(), runStamp, profile.name(), sutVersion));
        report.append(String.format(Locale.ENGLISH,
                "  %d virtual users, ramp %d s, steady window %d min%n",
                profile.virtualUsers(), profile.rampSeconds(), profile.steadyWindowMinutes()));
        report.append(String.format(Locale.ENGLISH,
                "  the rotation: %d account(s) play, %d per seat%n",
                playing.members().size(), campaign.playersPerSeat()));
        report.append(playing.statement());
        report.append(String.format(Locale.ENGLISH,
                "  warm start: a census of %d task(s) over %d bucket(s); the stand held %d task(s),"
                        + " %d were read and %d created%n",
                warmStart.census(), census.buckets().size(), warmStart.tasksOnTheStand(),
                warmStart.read(), warmStart.created()));
        report.append(String.format(Locale.ENGLISH, "  %d samples, %d of them failed%n",
                stats.overall().samplesCount(), stats.overall().errorsCount()));
        report.append(refusals(refusals));
        report.append(serviceLevels(waits));
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
     * What the people this run played actually waited, band by band, against the levels the
     * campaign fixed. A breach is reported as a finding and nothing else happens: the run is not
     * failed by it, because a faulted variant is meant to breach and a clean baseline that
     * breaches has measured a system worth the reading.
     *
     * <p>It is half a verdict and says so. The specification's viability criteria are these
     * levels together with the application's log staying quiet and the throttled-period counter
     * staying flat, and the last two are read on the stand: the injector drives no operational
     * endpoint and cannot see them.
     */
    private static String serviceLevels(ServiceLevelLedger.Verdict waits) {
        StringBuilder lines = new StringBuilder();
        lines.append(String.format(Locale.ENGLISH,
                "  service levels, %dth percentile by nearest rank, against a hard ceiling of"
                        + " %d ms:%n",
                ServiceLevels.PERCENTILE, waits.hardCeilingMillis()));
        for (ServiceLevelLedger.BandResult band : waits.bands()) {
            if (!band.exercised()) {
                lines.append(String.format(Locale.ENGLISH,
                        "    %s: not exercised, level %d ms%n",
                        band.band().key(), band.levelMillis()));
                continue;
            }
            lines.append(String.format(Locale.ENGLISH,
                    "    %s: %d ms against %d ms over %d sample(s), worst %d ms - %s%n",
                    band.band().key(), band.percentileMillis(), band.levelMillis(), band.samples(),
                    band.worstMillis(), band.breached() ? "BREACHED" : "met"));
        }
        lines.append(String.format(Locale.ENGLISH,
                "    the worst wait of the run was %d ms; %d sample(s) passed the ceiling%n",
                waits.worstMillis(), waits.samplesOverTheCeiling()));
        if (waits.samplesOutsideTheBands() > 0) {
            lines.append(String.format(Locale.ENGLISH,
                    "    %d sample(s) belong to no band and were held to the ceiling alone; a step"
                            + " the plan cannot name is the rig's own defect%n",
                    waits.samplesOutsideTheBands()));
        }
        lines.append("    the other half of the viability criteria - the application's log quiet"
                + " and the throttled-period counter flat - is read on the stand, not here")
                .append(System.lineSeparator());
        return lines.toString();
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
        if (code == RefusalLedger.NAMES_NOTHING) {
            return String.format(Locale.ENGLISH,
                    ", on %d task(s) deleted under a session between the pick and the request -"
                            + " carried by the run rather than failed over",
                    refusals.tasksVanishedUnderASession());
        }
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
