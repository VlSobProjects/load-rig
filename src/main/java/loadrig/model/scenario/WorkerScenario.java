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
 * The worker's day: they look at the list of what is assigned to them, open tasks - due-soon
 * ones more often, by the profile's skew - take part in the discussion of their own tasks, and
 * move the work they hold: mostly finishing it, now and then returning it with a question or
 * refusing it.
 */
public final class WorkerScenario implements Scenario {

    /**
     * Of every hundred steps a worker takes, how many are of each kind. The weights are this
     * project's allocation of the specification's mix across the scenarios - derived together
     * with the other scenarios' weights and the day populations - and the mix-survival check
     * holds the allocation against the profile before any load.
     */
    static final Map<StepKind, Integer> STEP_WEIGHTS = Map.of(
            StepKind.LOOKING_AT_A_LIST, 48,
            StepKind.OPENING_ONE_TASK, 30,
            StepKind.MOVING_A_TASK, 14,
            StepKind.DISCUSSION, 8);

    /**
     * The split of the worker's moves, summing to the move weight: most held work is finished,
     * and a question and a refusal each take a small, equal share. Together with the manager's
     * split this is what realizes the specification's settlements, and the census the warm start
     * brings a stand to is computed from exactly these numbers.
     */
    static final Map<Transition, Integer> TRANSITION_WEIGHTS = Map.of(
            Transition.FINISH, 10,
            Transition.RETURN_WITH_A_QUESTION, 2,
            Transition.REFUSE, 2);

    /** The role a session of this scenario acts in; the census reads it without wiring a plan. */
    static final Role SESSION_ROLE = Role.WORKER;

    /** A question is required on a return, a reason on a refusal; a finish needs no words. */
    private static final String QUESTION = "what should be done about the open points?";
    private static final String REFUSAL_REASON = "cannot be done as assigned";

    private static final String ACTOR = "the worker";
    private static final Scope LIST_SCOPE = Scope.ASSIGNED_TO_ME;
    private static final Sort LIST_SORT = Sort.DUE_DATE;

    private final List<Step> iteration;

    WorkerScenario(StepKit kit, ScenarioWiring wiring) {
        this.iteration = Scenarios.conforming(name(), STEP_WEIGHTS, List.of(
                CommonSteps.lookingAtAList(kit, STEP_WEIGHTS.get(StepKind.LOOKING_AT_A_LIST),
                        ACTOR + " looks at their list", LIST_SCOPE, LIST_SORT),
                CommonSteps.openingOneTask(kit, wiring,
                        STEP_WEIGHTS.get(StepKind.OPENING_ONE_TASK), sessionRole(),
                        ACTOR + " opens a task", LIST_SCOPE, LIST_SORT),
                CommonSteps.discussion(kit, wiring, STEP_WEIGHTS.get(StepKind.DISCUSSION),
                        ACTOR, LIST_SCOPE, LIST_SORT,
                        CommonSteps.pickVisibleTask(wiring, sessionRole(),
                                wiring.skew().notesPercent(), ACTOR + " reads the notes")),
                CommonSteps.movingATask(kit, wiring,
                        TRANSITION_WEIGHTS.get(Transition.FINISH), Transition.FINISH,
                        sessionRole(), ACTOR + " finishes a task", LIST_SCOPE, LIST_SORT, null),
                CommonSteps.movingATask(kit, wiring,
                        TRANSITION_WEIGHTS.get(Transition.RETURN_WITH_A_QUESTION),
                        Transition.RETURN_WITH_A_QUESTION, sessionRole(),
                        ACTOR + " returns a task with a question", LIST_SCOPE, LIST_SORT,
                        QUESTION),
                CommonSteps.movingATask(kit, wiring,
                        TRANSITION_WEIGHTS.get(Transition.REFUSE), Transition.REFUSE,
                        sessionRole(), ACTOR + " refuses a task", LIST_SCOPE, LIST_SORT,
                        REFUSAL_REASON)));
    }

    @Override
    public ScenarioName name() {
        return ScenarioName.WORKER;
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
