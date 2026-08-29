package loadrig.model.scenario;

import static us.abstracta.jmeter.javadsl.JmeterDsl.httpCookies;
import static us.abstracta.jmeter.javadsl.JmeterDsl.jtlWriter;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import loadrig.model.Role;
import loadrig.model.profile.EquilibriumCheck;
import loadrig.model.profile.LoadProfile;
import loadrig.model.profile.ScenarioName;
import loadrig.model.step.Step;
import loadrig.model.step.StepKit;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.DslTestPlan.TestPlanChild;
import us.abstracta.jmeter.javadsl.core.threadgroups.BaseThreadGroup.ThreadGroupChild;

/**
 * The test plan of one profile: every scheduling number - the populations, the ramp, the steady
 * window, the think-time bounds, the skew - comes from the loaded profile, and every step comes
 * from the scenarios behind the closed names. The invariants are held before the plan is even
 * assembled: the population equilibrium, the survival of the mix, and the seating of the
 * populations on the account pool, each refusing loudly before any load.
 *
 * <p>One thread group per populated scenario, named with the scenario's own key, so the thread
 * name of every sample in the result log states which scenario produced it.
 */
public final class ProfilePlan {

    private final String baseUrl;
    private final LoadProfile profile;
    private final ScenarioWiring wiring;

    public ProfilePlan(String baseUrl, LoadProfile profile, ScenarioWiring wiring) {
        this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl");
        this.profile = Objects.requireNonNull(profile, "profile");
        this.wiring = Objects.requireNonNull(wiring, "wiring");
    }

    public DslTestPlan plan(String jtlDirectory, String jtlFileName) {
        EquilibriumCheck.check(profile);
        MixSurvivalCheck.check(profile);
        seatThePopulations();
        StepKit kit = new StepKit(baseUrl, profile.thinkTime());
        Map<ScenarioName, Scenario> scenarios = Scenarios.all(kit, wiring);
        List<TestPlanChild> children = new ArrayList<>();
        children.add(httpCookies());
        for (ScenarioName name : ScenarioName.values()) {
            int population = profile.scenarioPopulation().get(name);
            if (population > 0) {
                children.add(scenarioGroup(scenarios.get(name), population, kit));
            }
        }
        // One record per user-visible step: a redirect the application answers with is part of
        // the step a person performed, and an own record beside it would double every rate.
        children.add(jtlWriter(jtlDirectory, jtlFileName).withSubResults(false));
        return testPlan(children.toArray(new TestPlanChild[0]));
    }

    private TestPlanChild scenarioGroup(Scenario scenario, int population, StepKit kit) {
        SessionSteps session =
                new SessionSteps(kit, wiring, scenario.sessionRole(), scenario.actorLabel());
        int totalWeight = scenario.iteration().stream().mapToInt(Step::weight).sum();
        List<ThreadGroupChild> children = new ArrayList<>();
        children.add(session.contextIntoThread());
        children.add(session.cookiesOutOfThread());
        children.add(session.takeUp());
        children.add(session.signInWhenNeeded());
        for (Step step : scenario.iteration()) {
            children.add(step.scheduled(100f * step.weight() / totalWeight));
        }
        children.add(session.setDown());
        return threadGroup(scenario.name().key())
                .rampToAndHold(population, Duration.ofSeconds(profile.rampSeconds()),
                        Duration.ofMinutes(profile.steadyWindowMinutes()))
                .children(children.toArray(new ThreadGroupChild[0]));
    }

    /**
     * Every population must be seatable on the pool: the discussion takes up manager sessions,
     * so it counts against the managers. Refusing here is what keeps a misconfigured run from
     * spending a stand window to learn that its threads starve.
     */
    private void seatThePopulations() {
        Map<ScenarioName, Integer> population = profile.scenarioPopulation();
        requireSeats(Role.WORKER, population.get(ScenarioName.WORKER),
                ScenarioName.WORKER.key());
        requireSeats(Role.MANAGER,
                population.get(ScenarioName.MANAGER) + population.get(ScenarioName.DISCUSSION),
                ScenarioName.MANAGER.key() + " and " + ScenarioName.DISCUSSION.key());
        requireSeats(Role.ADMINISTRATOR, population.get(ScenarioName.ADMINISTRATOR),
                ScenarioName.ADMINISTRATOR.key());
    }

    private void requireSeats(Role role, int needed, String population) {
        int seats = wiring.sessions().namesOf(role).size();
        if (needed > seats) {
            throw new IllegalArgumentException("the " + population + " population needs "
                    + needed + " " + role + " session(s) at once while the pool holds " + seats
                    + "; the profile and the account pool disagree, and a run under them would"
                    + " starve instead of loading");
        }
    }
}
