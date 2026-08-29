package loadrig.model.scenario;

import java.util.List;
import java.util.Map;
import loadrig.model.Role;
import loadrig.model.SutSurface.Scope;
import loadrig.model.SutSurface.Sort;
import loadrig.model.SutSurface.Transition;
import loadrig.model.profile.ScenarioName;
import loadrig.model.step.Step;
import loadrig.model.step.StepKind;
import loadrig.model.step.StepKit;

/**
 * The administrator's day: they browse everything - the unscoped list is theirs alone - open a
 * task here and there, and now and then delete one, the one right their role owns. Keeping the
 * accounts is provisioning work (LR-9) and no part of the day's load.
 */
public final class AdministratorScenario implements Scenario {

    /**
     * Of every ten steps the administrator takes, how many are of each kind. All the deletions
     * of the mix are theirs. The mix-survival check holds this allocation against the profile
     * before any load.
     */
    static final Map<StepKind, Integer> STEP_WEIGHTS = Map.of(
            StepKind.LOOKING_AT_A_LIST, 7,
            StepKind.OPENING_ONE_TASK, 2,
            StepKind.DELETING_A_TASK, 1);

    /**
     * The administrator's one move is the deletion, and it is the whole deletion share of the
     * mix. It stands outside the census: the deletion draws from every status of every task, so
     * the only stand it starves on is one with no tasks at all.
     */
    static final Map<Transition, Integer> TRANSITION_WEIGHTS =
            Map.of(Transition.DELETE, 1);

    /** The role a session of this scenario acts in; the census reads it without wiring a plan. */
    static final Role SESSION_ROLE = Role.ADMINISTRATOR;

    private static final String ACTOR = "the administrator";
    private static final Scope LIST_SCOPE = Scope.ALL;
    private static final Sort LIST_SORT = Sort.CREATED;

    private final List<Step> iteration;

    AdministratorScenario(StepKit kit, ScenarioWiring wiring) {
        this.iteration = Scenarios.conforming(name(), STEP_WEIGHTS, List.of(
                CommonSteps.lookingAtAList(kit, STEP_WEIGHTS.get(StepKind.LOOKING_AT_A_LIST),
                        ACTOR + " looks at every task", LIST_SCOPE, LIST_SORT),
                CommonSteps.openingOneTask(kit, wiring,
                        STEP_WEIGHTS.get(StepKind.OPENING_ONE_TASK), sessionRole(),
                        ACTOR + " opens a task", LIST_SCOPE, LIST_SORT),
                CommonSteps.movingATask(kit, wiring,
                        TRANSITION_WEIGHTS.get(Transition.DELETE), Transition.DELETE,
                        sessionRole(), ACTOR + " deletes a task", LIST_SCOPE, LIST_SORT, null)));
    }

    @Override
    public ScenarioName name() {
        return ScenarioName.ADMINISTRATOR;
    }

    @Override
    public Role sessionRole() {
        return SESSION_ROLE;
    }

    @Override
    public String actorLabel() {
        return ACTOR;
    }

    @Override
    public List<Step> iteration() {
        return iteration;
    }
}
