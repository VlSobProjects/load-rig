package loadrig.model.scenario;

import static us.abstracta.jmeter.javadsl.JmeterDsl.jsr223PostProcessor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import loadrig.model.Correlation;
import loadrig.model.Role;
import loadrig.model.SutSurface;
import loadrig.model.SutSurface.Scope;
import loadrig.model.SutSurface.Sort;
import loadrig.model.SutSurface.Transition;
import loadrig.model.profile.ScenarioName;
import loadrig.model.step.ContentExpectation;
import loadrig.model.step.Step;
import loadrig.model.step.StepGate;
import loadrig.model.step.StepKind;
import loadrig.model.step.StepKit;
import loadrig.registry.RegistryStarvedException;
import loadrig.registry.TaskRegistry;
import loadrig.registry.TaskStatus;
import us.abstracta.jmeter.javadsl.http.DslHttpSampler;

/**
 * The manager's day: they look at the work they handed out, open tasks, settle what came back -
 * approving finished work, acknowledging refusals, answering a returned question by handing the
 * task out again, occasionally putting settled work back into circulation - create new tasks,
 * run the report on one worker's week, and take part in the discussion.
 */
public final class ManagerScenario implements Scenario {

    /**
     * Of every sixty steps a manager takes, how many are of each kind: the report, the creation
     * and the settling are the manager's alone, which is why their shares of a manager's day
     * are large while the profile's global shares stay the specification's. The mix-survival
     * check holds this allocation against the profile before any load.
     */
    static final Map<StepKind, Integer> STEP_WEIGHTS = Map.of(
            StepKind.LOOKING_AT_A_LIST, 8,
            StepKind.OPENING_ONE_TASK, 6,
            StepKind.MOVING_A_TASK, 12,
            StepKind.DISCUSSION, 4,
            StepKind.RUNNING_A_REPORT, 20,
            StepKind.CREATING_A_TASK, 10);

    /**
     * The split of the manager's moves, summing to the move weight: settling dominates, the
     * answer to a returned question and the reopening of settled work are rare. Together with
     * the worker's split this realizes about the specification's two and a half transitions per
     * settlement.
     */
    private static final int APPROVES = 8;
    private static final int ACKNOWLEDGES = 2;
    private static final int HANDS_OUT_AGAIN = 1;
    private static final int PUTS_BACK_TO_WORK = 1;

    /** The reopen demands a reason the way a return and a refusal do; the stand refuses one without. */
    private static final String REOPEN_REASON = "the settled work is needed again";

    /**
     * The hand-out-again is the creator's answer to the returned question, and the answer is the
     * message: the stand demands it beside the assignee, refusing the bare reassignment.
     */
    private static final String ANSWER_TO_THE_QUESTION = "answered - proceed as discussed";

    /**
     * Every second created task is due inside the hot window, the rest later: the run must feed
     * both the hot set the skew converges on and the cold rest the skew occasionally reads, or
     * one of the two starves and the skew has nothing to choose between.
     */
    private static final int CREATED_DUE_SOON_PERCENT = 50;

    /** The hot window of the specification: open tasks due within three days are the hot set. */
    private static final String DUE_SOON = "P3D";

    private static final String DUE_LATER = "P14D";

    /**
     * The window a manager asks a report about: last week, about one worker. A range drawn at
     * random over the whole history is the most expensive question the application offers and
     * would dominate every capture, the clean baseline included.
     */
    private static final String REPORT_WINDOW_START = "-P7D";

    private static final String ACTOR = "the manager";
    private static final Scope LIST_SCOPE = Scope.ASSIGNED_BY_ME;
    private static final Sort LIST_SORT = Sort.CREATED;

    private final List<Step> iteration;

    ManagerScenario(StepKit kit, ScenarioWiring wiring) {
        this.iteration = Scenarios.conforming(name(), STEP_WEIGHTS, List.of(
                CommonSteps.lookingAtAList(kit, STEP_WEIGHTS.get(StepKind.LOOKING_AT_A_LIST),
                        ACTOR + " looks at the work they handed out", LIST_SCOPE, LIST_SORT),
                CommonSteps.openingOneTask(kit, wiring,
                        STEP_WEIGHTS.get(StepKind.OPENING_ONE_TASK), sessionRole(),
                        ACTOR + " opens a task", LIST_SCOPE, LIST_SORT),
                CommonSteps.movingATask(kit, wiring, APPROVES, Transition.APPROVE, sessionRole(),
                        ACTOR + " approves finished work", LIST_SCOPE, LIST_SORT, null),
                CommonSteps.movingATask(kit, wiring, ACKNOWLEDGES, Transition.ACCEPT_THE_REFUSAL,
                        sessionRole(), ACTOR + " accepts a refusal", LIST_SCOPE, LIST_SORT, null),
                handsATaskOutAgain(kit, wiring),
                CommonSteps.movingATask(kit, wiring, PUTS_BACK_TO_WORK,
                        Transition.PUT_BACK_TO_WORK, sessionRole(),
                        ACTOR + " puts settled work back into circulation", LIST_SCOPE, LIST_SORT,
                        REOPEN_REASON),
                createsATask(kit, wiring),
                runsAReport(kit, wiring),
                CommonSteps.discussion(kit, wiring, STEP_WEIGHTS.get(StepKind.DISCUSSION),
                        ACTOR, LIST_SCOPE, LIST_SORT,
                        CommonSteps.pickVisibleTask(wiring, sessionRole(),
                                wiring.skew().notesPercent(), ACTOR + " reads the notes"))));
    }

    /**
     * The answer to a returned question: the task goes out again, to a worker the directory
     * already knows the identity of. The registry records who it went to, because the next
     * finish must come from exactly that assignee.
     */
    private Step handsATaskOutAgain(StepKit kit, ScenarioWiring wiring) {
        String name = ACTOR + " hands a task out again";
        StepGate gate = vars -> {
            String username = vars.get(SessionSteps.SESSION_USER_VARIABLE);
            TaskRegistry.Lease lease;
            try {
                lease = wiring.tasks().lease(Transition.HAND_OUT_AGAIN, username, sessionRole());
            } catch (RegistryStarvedException starved) {
                wiring.starvation().skipped(name);
                return false;
            }
            String next = knownWorker(wiring);
            if (next == null) {
                wiring.tasks().released(lease);
                wiring.starvation().skipped(name);
                return false;
            }
            vars.putObject(IterationSlots.MOVE_LEASE, lease);
            vars.put(IterationSlots.TASK, lease.task().taskId());
            vars.put(IterationSlots.ASSIGNEE_NAME, next);
            vars.put(IterationSlots.ASSIGNEE_ID, wiring.directory().idOf(next).orElseThrow());
            return true;
        };
        DslHttpSampler request = CommonSteps.transitionRequest(kit, name,
                Transition.HAND_OUT_AGAIN, LIST_SCOPE, LIST_SORT, ANSWER_TO_THE_QUESTION)
                .param(SutSurface.TASK_ASSIGNEE_FIELD,
                        StepKit.variable(IterationSlots.ASSIGNEE_ID));
        request.children(jsr223PostProcessor(s -> {
            TaskRegistry.Lease lease =
                    (TaskRegistry.Lease) s.vars.getObject(IterationSlots.MOVE_LEASE);
            if (lease == null) {
                return;
            }
            s.vars.putObject(IterationSlots.MOVE_LEASE, null);
            if (CommonSteps.accepted(s.prev, s.prevResponse())) {
                wiring.tasks().applied(lease, s.vars.get(IterationSlots.ASSIGNEE_NAME));
            } else {
                wiring.tasks().released(lease);
            }
        }));
        return kit.step(HANDS_OUT_AGAIN, StepKind.MOVING_A_TASK, gate, request);
    }

    /**
     * A creation: a unique run-marked title, an assignee the directory knows, a due date drawn
     * inside or beyond the hot window. The identity of the created task is read from the row
     * carrying exactly this title, and the registry learns the task so later steps may act on
     * it.
     */
    private Step createsATask(StepKit kit, ScenarioWiring wiring) {
        String name = ACTOR + " creates a task";
        StepGate gate = vars -> {
            String assignee = knownWorker(wiring);
            if (assignee == null) {
                wiring.starvation().skipped(name);
                return false;
            }
            boolean dueSoon = CommonSteps.draw(CREATED_DUE_SOON_PERCENT);
            vars.put(IterationSlots.TASK_TITLE, wiring.nextTaskTitle());
            vars.put(IterationSlots.DUE_SHIFT, dueSoon ? DUE_SOON : DUE_LATER);
            vars.put(IterationSlots.CREATED_HOT, String.valueOf(dueSoon));
            vars.put(IterationSlots.ASSIGNEE_NAME, assignee);
            vars.put(IterationSlots.ASSIGNEE_ID, wiring.directory().idOf(assignee).orElseThrow());
            return true;
        };
        DslHttpSampler request = kit.fragmentRequest(name, SutSurface.TASKS,
                ContentExpectation.renders("the created task is in the answer",
                        StepKit.variable(IterationSlots.TASK_TITLE)),
                Correlation.taskIdentity(IterationSlots.CREATED_TASK,
                        StepKit.variable(IterationSlots.TASK_TITLE)))
                .method("POST")
                .param(SutSurface.TASK_TITLE_FIELD, StepKit.variable(IterationSlots.TASK_TITLE))
                .param(SutSurface.TASK_DESCRIPTION_FIELD, "a task of the day profile")
                .param(SutSurface.TASK_ASSIGNEE_FIELD,
                        StepKit.variable(IterationSlots.ASSIGNEE_ID))
                .param(SutSurface.TASK_DUE_DATE_FIELD,
                        StepKit.dateShiftedBy(StepKit.variable(IterationSlots.DUE_SHIFT)))
                .param(SutSurface.SCOPE_FIELD, LIST_SCOPE.value())
                .param(SutSurface.SORT_FIELD, LIST_SORT.value())
                .param(SutSurface.PAGE_FIELD, IterationSlots.FIRST_PAGE)
                .param(SutSurface.CSRF_FIELD,
                        StepKit.variable(SessionSteps.SESSION_TOKEN_VARIABLE));
        request.children(jsr223PostProcessor(s -> {
            String taskId = s.vars.get(IterationSlots.CREATED_TASK);
            if (taskId == null || Correlation.EXTRACTION_FAILED.equals(taskId)) {
                return;
            }
            wiring.tasks().register(new TaskRegistry.TaskFacts(taskId, TaskStatus.OPEN,
                    s.vars.get(SessionSteps.SESSION_USER_VARIABLE),
                    s.vars.get(IterationSlots.ASSIGNEE_NAME),
                    Boolean.parseBoolean(s.vars.get(IterationSlots.CREATED_HOT))));
        }));
        return kit.step(STEP_WEIGHTS.get(StepKind.CREATING_A_TASK), StepKind.CREATING_A_TASK,
                gate, request);
    }

    /** The report on one worker's last week: the form, then the question. */
    private Step runsAReport(StepKit kit, ScenarioWiring wiring) {
        String name = ACTOR + " runs a report on one worker";
        StepGate gate = vars -> {
            String worker = knownWorker(wiring);
            if (worker == null) {
                wiring.starvation().skipped(name);
                return false;
            }
            vars.put(IterationSlots.REPORT_WORKER_ID,
                    wiring.directory().idOf(worker).orElseThrow());
            return true;
        };
        DslHttpSampler opensTheForm = kit.pageRequest(ACTOR + " opens the report form",
                SutSurface.REPORTS, ContentExpectation.rendersNoRefusalMark());
        DslHttpSampler runsIt = kit.pageRequest(name, SutSurface.REPORTS,
                ContentExpectation.rendersNoRefusalMark())
                .method("POST")
                .param(SutSurface.REPORT_WORKER_FIELD,
                        StepKit.variable(IterationSlots.REPORT_WORKER_ID))
                .param(SutSurface.REPORT_FROM_DATE_FIELD,
                        StepKit.dateShiftedBy(REPORT_WINDOW_START))
                .param(SutSurface.REPORT_TO_DATE_FIELD, StepKit.today())
                .param(SutSurface.CSRF_FIELD,
                        StepKit.variable(SessionSteps.SESSION_TOKEN_VARIABLE));
        return kit.step(STEP_WEIGHTS.get(StepKind.RUNNING_A_REPORT), StepKind.RUNNING_A_REPORT,
                gate, opensTheForm, runsIt);
    }

    /** A worker whose identity an answer has already offered, drawn at random among them. */
    private static String knownWorker(ScenarioWiring wiring) {
        List<String> known = wiring.directory()
                .knownAmong(wiring.sessions().namesOf(Role.WORKER));
        if (known.isEmpty()) {
            return null;
        }
        return known.get(ThreadLocalRandom.current().nextInt(known.size()));
    }

    @Override
    public ScenarioName name() {
        return ScenarioName.MANAGER;
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
