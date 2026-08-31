package loadrig.model.scenario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import loadrig.model.AccountPool;
import loadrig.model.Role;
import loadrig.model.profile.LoadProfile;
import loadrig.model.profile.ProfileLoader;
import loadrig.model.profile.ScenarioName;
import loadrig.model.profile.ServiceLevelBand;
import loadrig.model.profile.StepMix;
import loadrig.model.step.Step;
import loadrig.model.step.StepKit;
import loadrig.registry.SessionRegistry;
import loadrig.registry.TaskRegistry;
import loadrig.registry.UserDirectory;
import org.junit.jupiter.api.Test;

/**
 * Proves what can be proven about the profile-driven plan without a running stack: that the
 * repository's own day profile assembles into a plan over the account pool, that every closed
 * name has a scenario whose iteration states exactly its own weights, and that a profile the
 * invariants refuse - a broken mix, populations the pool cannot seat - never assembles at all.
 *
 * <p>Whether the plan actually drives the application is proven by a run, and a run is recorded
 * in the session note rather than in a test.
 */
class ProfilePlanTest {

    private static final String A_STACK = "http://an.address.of.a.stack:8080";
    private static final String A_RUN = "20260829-120000";
    private static final String A_PASSWORD = "loadrig123!";

    @Test
    void theDayProfileAssemblesIntoAPlanOverTheAccountPool() {
        LoadProfile profile = ProfileLoader.load(Path.of("profiles", "day.json"));

        var plan = new ProfilePlan(A_STACK, profile, poolWiring())
                .plan("results", "a-run.jtl");

        assertNotNull(plan);
    }

    @Test
    void everyClosedNameHasAScenarioStatingItsOwnWeights() {
        ScenarioWiring wiring = poolWiring();
        StepKit kit = new StepKit(A_STACK, new LoadProfile.ThinkTime(3, 7));

        Map<ScenarioName, Scenario> scenarios = Scenarios.all(kit, wiring);

        for (ScenarioName name : ScenarioName.values()) {
            Scenario scenario = scenarios.get(name);
            assertEquals(name, scenario.name());
            int stated = Scenarios.stepWeights().get(name).values().stream()
                    .mapToInt(Integer::intValue).sum();
            int built = scenario.iteration().stream().mapToInt(Step::weight).sum();
            assertEquals(stated, built,
                    "the " + name.key() + " iteration and its stated weights are one fact");
        }
    }

    @Test
    void theAssembledPlanLeavesEveryBandWithAnActToJudge() {
        LoadProfile profile = ProfileLoader.load(Path.of("profiles", "day.json"));
        ScenarioWiring wiring = poolWiring();

        new ProfilePlan(A_STACK, profile, wiring).plan("results", "a-run.jtl");

        Collection<ServiceLevelBand> judged = wiring.serviceLevels().bandsByLabel().values();
        for (ServiceLevelBand band : ServiceLevelBand.values()) {
            assertTrue(judged.contains(band),
                    "the day plan performs an act judged in " + band.key() + ": " + judged);
        }
    }

    @Test
    void aProfileWhoseMixTheScenariosCannotReproduceNeverAssembles() {
        LoadProfile broken = dayProfileWithPopulations(3, 5, 1, 1);

        assertThrows(IllegalArgumentException.class,
                () -> new ProfilePlan(A_STACK, broken, poolWiring())
                        .plan("results", "a-run.jtl"));
    }

    @Test
    void populationsThePoolCannotSeatAreRefusedBeforeAnyLoad() {
        LoadProfile profile = ProfileLoader.load(Path.of("profiles", "day.json"));
        List<AccountPool.Member> oneOfEachRole = List.of(
                new AccountPool.Member("worker-01", Role.WORKER),
                new AccountPool.Member("manager-01", Role.MANAGER),
                new AccountPool.Member("admin-01", Role.ADMINISTRATOR));

        IllegalArgumentException refusal = assertThrows(IllegalArgumentException.class,
                () -> SeatedAccounts.of(profile, oneOfEachRole));

        assertTrue(refusal.getMessage().contains("starve"),
                "the refusal must state the consequence: " + refusal.getMessage());
    }

    private static ScenarioWiring poolWiring() {
        LoadProfile profile = ProfileLoader.load(Path.of("profiles", "day.json"));
        return new ScenarioWiring(new SessionRegistry(AccountPool.members()),
                SeatedAccounts.of(profile), new TaskRegistry(), new UserDirectory(),
                new StarvationLedger(), new RefusalLedger(), new ServiceLevelLedger(),
                profile.hotSetSkew(), A_RUN, A_PASSWORD);
    }

    private static LoadProfile dayProfileWithPopulations(int workers, int managers,
            int administrators, int discussion) {
        return new LoadProfile("day", workers + managers + administrators + discussion, 30, 10,
                new LoadProfile.ThinkTime(3, 7), new StepMix(37, 22, 13, 12, 10, 5, 1),
                new LoadProfile.HotSetSkew(70, 80), 2,
                Map.of(ScenarioName.WORKER, workers, ScenarioName.MANAGER, managers,
                        ScenarioName.ADMINISTRATOR, administrators,
                        ScenarioName.DISCUSSION, discussion));
    }
}
