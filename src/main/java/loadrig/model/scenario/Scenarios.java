package loadrig.model.scenario;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import loadrig.model.Role;
import loadrig.model.SutSurface.Transition;
import loadrig.model.profile.ScenarioName;
import loadrig.model.step.Step;
import loadrig.model.step.StepKind;
import loadrig.model.step.StepKit;

/**
 * The closed scenario registry of DR-3: one scenario class behind each name of the closed list
 * the profile vocabulary already validates against, and nothing else. The registry is
 * materialized here, so a name without a scenario - or a scenario answering to the wrong name -
 * refuses the wiring instead of starving a run.
 */
public final class Scenarios {

    /** The scenario behind every name of the closed list, wired for one run. */
    public static Map<ScenarioName, Scenario> all(StepKit kit, ScenarioWiring wiring) {
        EnumMap<ScenarioName, Scenario> all = new EnumMap<>(ScenarioName.class);
        register(all, new WorkerScenario(kit, wiring));
        register(all, new ManagerScenario(kit, wiring));
        register(all, new AdministratorScenario(kit, wiring));
        register(all, new DiscussionScenario(kit, wiring));
        for (ScenarioName name : ScenarioName.values()) {
            if (!all.containsKey(name)) {
                throw new IllegalStateException("no scenario is registered behind the name "
                        + name.key() + "; the closed list and this registry must state the same"
                        + " scenarios");
            }
        }
        return Map.copyOf(all);
    }

    /**
     * The step weights every scenario states, without wiring anything: what the mix-survival
     * check reads, and by construction the same numbers the wired iterations are built from.
     */
    public static Map<ScenarioName, Map<StepKind, Integer>> stepWeights() {
        EnumMap<ScenarioName, Map<StepKind, Integer>> weights =
                new EnumMap<>(ScenarioName.class);
        weights.put(ScenarioName.WORKER, WorkerScenario.STEP_WEIGHTS);
        weights.put(ScenarioName.MANAGER, ManagerScenario.STEP_WEIGHTS);
        weights.put(ScenarioName.ADMINISTRATOR, AdministratorScenario.STEP_WEIGHTS);
        weights.put(ScenarioName.DISCUSSION, DiscussionScenario.STEP_WEIGHTS);
        return Map.copyOf(weights);
    }

    /**
     * The transition weights every scenario states, held against its own step weights: the moves
     * of a scenario must sum to the move share of its day, and its deletions to its deletion
     * share. What reads them is the census the warm start brings a stand to, so a scenario that
     * moved tasks the mix does not account for would have the census stock for a demand no
     * capture describes.
     */
    public static Map<ScenarioName, Map<Transition, Integer>> transitionWeights() {
        EnumMap<ScenarioName, Map<Transition, Integer>> weights =
                new EnumMap<>(ScenarioName.class);
        weights.put(ScenarioName.WORKER, WorkerScenario.TRANSITION_WEIGHTS);
        weights.put(ScenarioName.MANAGER, ManagerScenario.TRANSITION_WEIGHTS);
        weights.put(ScenarioName.ADMINISTRATOR, AdministratorScenario.TRANSITION_WEIGHTS);
        weights.put(ScenarioName.DISCUSSION, DiscussionScenario.TRANSITION_WEIGHTS);
        Map<ScenarioName, Map<StepKind, Integer>> steps = stepWeights();
        weights.forEach((name, table) -> holdingTheMoveShares(name, table, steps.get(name)));
        return Map.copyOf(weights);
    }

    /**
     * The role each scenario's sessions act in, read without wiring a plan: the census is computed
     * before anything is built, and it must know whose accounts a transition's demand lands on.
     */
    public static Map<ScenarioName, Role> sessionRoles() {
        EnumMap<ScenarioName, Role> roles = new EnumMap<>(ScenarioName.class);
        roles.put(ScenarioName.WORKER, WorkerScenario.SESSION_ROLE);
        roles.put(ScenarioName.MANAGER, ManagerScenario.SESSION_ROLE);
        roles.put(ScenarioName.ADMINISTRATOR, AdministratorScenario.SESSION_ROLE);
        roles.put(ScenarioName.DISCUSSION, DiscussionScenario.SESSION_ROLE);
        return Map.copyOf(roles);
    }

    private static void holdingTheMoveShares(ScenarioName name,
            Map<Transition, Integer> transitions, Map<StepKind, Integer> steps) {
        int moves = 0;
        int deletions = 0;
        for (Map.Entry<Transition, Integer> weighted : transitions.entrySet()) {
            if (weighted.getKey() == Transition.DELETE) {
                deletions += weighted.getValue();
            } else {
                moves += weighted.getValue();
            }
        }
        requireShare(name, StepKind.MOVING_A_TASK, moves, steps);
        requireShare(name, StepKind.DELETING_A_TASK, deletions, steps);
    }

    private static void requireShare(ScenarioName name, StepKind kind, int stated,
            Map<StepKind, Integer> steps) {
        int share = steps.getOrDefault(kind, 0);
        if (stated != share) {
            throw new IllegalStateException("the " + name.key() + " scenario weights " + stated
                    + " against " + kind + " through its transitions while its step table states "
                    + share + "; the two tables state one fact and must state it once");
        }
    }

    /**
     * Holds a scenario's built iteration against its own stated weights: every kind the table
     * states is present with exactly that weight, and no step carries a kind the table does not
     * state. The table is what the mix-survival check reads, so a drift between the two would
     * make that check a check of nothing.
     */
    static List<Step> conforming(ScenarioName name, Map<StepKind, Integer> statedWeights,
            List<Step> steps) {
        EnumMap<StepKind, Integer> built = new EnumMap<>(StepKind.class);
        for (Step step : steps) {
            built.merge(step.kind(), step.weight(), Integer::sum);
        }
        if (!built.equals(new EnumMap<>(statedWeights))) {
            throw new IllegalStateException("the " + name.key() + " scenario states the weights "
                    + statedWeights + " but its iteration is built with " + built
                    + "; the stated table is what the mix-survival check reads, and the two must"
                    + " be one fact");
        }
        return steps;
    }

    private static void register(EnumMap<ScenarioName, Scenario> all, Scenario scenario) {
        all.put(scenario.name(), scenario);
    }

    private Scenarios() {
    }
}
