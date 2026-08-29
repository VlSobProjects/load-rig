package loadrig.model.scenario;

import java.util.List;
import java.util.Map;
import loadrig.model.Role;
import loadrig.model.SutSurface.Scope;
import loadrig.model.SutSurface.Sort;
import loadrig.model.profile.ScenarioName;
import loadrig.model.step.Step;
import loadrig.model.step.StepKind;
import loadrig.model.step.StepKit;

/**
 * The discussion: the convergence of attention on the hot set. One virtual user orchestrates
 * many voices - each iteration takes up whichever manager session is free, looks at what is due
 * soon, opens a hot task, and joins the discussion of a hot task its manager neither created
 * nor works on - so over the window several distinct people write into the notes of the same
 * few tasks, which is the specification's picture of a hot task and the profile's one
 * deliberate concurrency.
 */
public final class DiscussionScenario implements Scenario {

    /**
     * Of every ten steps the discussion takes, how many are of each kind: mostly the notes
     * visits it exists for. The mix-survival check holds this allocation against the profile
     * before any load.
     */
    static final Map<StepKind, Integer> STEP_WEIGHTS = Map.of(
            StepKind.LOOKING_AT_A_LIST, 2,
            StepKind.OPENING_ONE_TASK, 2,
            StepKind.DISCUSSION, 6);

    private static final String ACTOR = "the discussion";

    /** The discussion browses what is due soon across everything: the hot set's own view. */
    private static final Scope LIST_SCOPE = Scope.ALL;
    private static final Sort LIST_SORT = Sort.DUE_DATE;

    private final List<Step> iteration;

    DiscussionScenario(StepKit kit, ScenarioWiring wiring) {
        this.iteration = Scenarios.conforming(name(), STEP_WEIGHTS, List.of(
                CommonSteps.lookingAtAList(kit, STEP_WEIGHTS.get(StepKind.LOOKING_AT_A_LIST),
                        ACTOR + " looks at what is due soon", LIST_SCOPE, LIST_SORT),
                CommonSteps.openingOneTask(kit, wiring,
                        STEP_WEIGHTS.get(StepKind.OPENING_ONE_TASK), sessionRole(),
                        ACTOR + " opens a task", LIST_SCOPE, LIST_SORT),
                CommonSteps.discussion(kit, wiring, STEP_WEIGHTS.get(StepKind.DISCUSSION),
                        ACTOR, LIST_SCOPE, LIST_SORT,
                        CommonSteps.pickHotTaskOutsideOwnArea(wiring,
                                ACTOR + " reads the notes"))));
    }

    @Override
    public ScenarioName name() {
        return ScenarioName.DISCUSSION;
    }

    @Override
    public Role sessionRole() {
        return Role.MANAGER;
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
