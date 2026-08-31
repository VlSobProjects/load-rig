package loadrig.model.scenario;

import static us.abstracta.jmeter.javadsl.JmeterDsl.httpCookies;
import static us.abstracta.jmeter.javadsl.JmeterDsl.jsr223PostProcessor;
import static us.abstracta.jmeter.javadsl.JmeterDsl.jtlWriter;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import loadrig.model.Role;
import loadrig.model.profile.LoadProfile;
import loadrig.model.profile.ScenarioName;
import loadrig.model.step.Step;
import loadrig.model.step.StepKit;
import loadrig.registry.TransportContext;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.DslTestPlan.TestPlanChild;
import us.abstracta.jmeter.javadsl.core.postprocessors.DslJsr223PostProcessor;
import us.abstracta.jmeter.javadsl.core.threadgroups.BaseThreadGroup.ThreadGroupChild;

/**
 * The test plan of one profile: every scheduling number - the populations, the ramp, the steady
 * window, the think-time bounds, the skew - comes from the loaded profile, and every step comes
 * from the scenarios behind the closed names. The invariants are held before the plan is even
 * assembled: the census the profile's population needs and the survival of the mix, each refusing
 * loudly before any load. The third - that the populations are seatable on the pool at all - was
 * held before the wiring existed, by {@link SeatedAccounts}, which is where the accounts of this
 * run come from.
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
        WarmStartCensus.of(profile, wiring.seated()).check();
        MixSurvivalCheck.check(profile);
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
        // The labels the plan just built, with the kind of act behind each: the service-level
        // ledger attributes every sample through this, and it is stated once the whole plan
        // exists so that no group's steps are judged before its own are known.
        wiring.serviceLevels().judgeByTheKindsOf(kit.stepKindsByLabel());
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
        children.add(perSampleTally());
        return threadGroup(scenario.name().key())
                .rampToAndHold(population, Duration.ofSeconds(profile.rampSeconds()),
                        Duration.ofMinutes(profile.steadyWindowMinutes()))
                .children(children.toArray(new ThreadGroupChild[0]));
    }

    /**
     * The element that fills the two ledgers a sample feeds. It sits in the group rather than on
     * a sampler, so that it runs after every request the group makes and no step can be added
     * that escapes it.
     *
     * <p>The refusal ledger reads the status the application answered with and whether the thread
     * held a token when it asked; the second is what splits the one refusal code that carries two
     * causes, and it is knowable only here, inside the injector. The service-level ledger reads
     * the label and the time the person waited, and takes only the samples the result log itself
     * carries: the injector's own bookkeeping elements are marked ignored and are no wait
     * anybody had.
     *
     * <p>It writes nothing into the sample and changes nothing about the run: the result log is
     * unaffected, and what these two ledgers know lives in the report the harness writes beside
     * it.
     */
    private DslJsr223PostProcessor perSampleTally() {
        return jsr223PostProcessor(s -> {
            wiring.refusals().answered(s.prev.getResponseCode(),
                    TransportContext.isAToken(s.vars.get(SessionSteps.SESSION_TOKEN_VARIABLE)));
            if (!s.prev.isIgnore()) {
                wiring.serviceLevels().took(s.prev.getSampleLabel(), s.prev.getTime());
            }
        });
    }

}
