package loadrig.run;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import loadrig.model.TransportWalk;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;
import us.abstracta.jmeter.javadsl.core.engines.EmbeddedJmeterEngine;

/**
 * The smoke run of the transport skeleton: a few virtual users walking every element of the
 * transport once against a running stack, leaving a JTL log behind.
 *
 * <p>It is not a capture and it carries no load profile. Its purpose is to prove that the script
 * drives the application rather than measuring the application, and it is the run the session
 * note of this work item records.
 */
public final class TransportSmokeRun {

    private static final String ARTIFACT_PREFIX = "transport-smoke-";
    private static final String ARTIFACT_SUFFIX = ".jtl";

    private static final DateTimeFormatter ARTIFACT_STAMP =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    public static void main(String[] args) throws Exception {
        InjectorSettings.apply();
        RigConfiguration configuration = RigConfiguration.fromSystemProperties();
        Path results = configuration.resultsDirectory().toAbsolutePath();
        Files.createDirectories(results);
        String runStamp = ZonedDateTime.now(ZoneOffset.UTC).format(ARTIFACT_STAMP);
        String artifact = ARTIFACT_PREFIX + runStamp + ARTIFACT_SUFFIX;

        System.out.println("driving " + configuration.baseUrl()
                + " with " + configuration.virtualUsers() + " virtual users, "
                + configuration.iterations() + " iteration(s)");

        TestPlanStats stats = new TransportWalk(
                configuration.baseUrl(),
                configuration.accounts(),
                configuration.provisionedPassword(),
                runStamp)
                .plan(configuration.virtualUsers(), configuration.iterations(),
                        results.toString(), artifact)
                .runIn(new EmbeddedJmeterEngine());

        report(stats, results.resolve(artifact));
    }

    /**
     * A walk that could not be completed is a failure of the script, not a result: every step of
     * it is legal by the transition table, so a refused request means the rig asked the
     * application for something it does not allow.
     */
    private static void report(TestPlanStats stats, Path artifact) throws IOException {
        long samples = stats.overall().samplesCount();
        long errors = stats.overall().errorsCount();
        System.out.println(samples + " samples, " + errors + " of them refused");
        System.out.println("the result log is " + artifact);
        if (errors > 0) {
            throw new IOException("the transport walk was refused " + errors + " time(s); "
                    + "the walk measured the script and not the system, see " + artifact);
        }
    }

    private TransportSmokeRun() {
    }
}
