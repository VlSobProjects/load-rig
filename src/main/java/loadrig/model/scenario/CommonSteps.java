package loadrig.model.scenario;

import static us.abstracta.jmeter.javadsl.JmeterDsl.jsr223PostProcessor;

import java.util.concurrent.ThreadLocalRandom;
import loadrig.model.Role;
import loadrig.model.SutSurface;
import loadrig.model.SutSurface.Scope;
import loadrig.model.SutSurface.Sort;
import loadrig.model.SutSurface.Transition;
import loadrig.model.step.ContentExpectation;
import loadrig.model.step.Step;
import loadrig.model.step.StepGate;
import loadrig.model.step.StepKind;
import loadrig.model.step.StepKit;
import loadrig.registry.RegistryStarvedException;
import loadrig.registry.TaskRegistry;
import us.abstracta.jmeter.javadsl.core.postprocessors.DslJsr223PostProcessor;
import us.abstracta.jmeter.javadsl.http.DslHttpSampler;

/**
 * The steps every scenario states the same way: looking at a list, opening a task, one visit of
 * the discussion, one transition. What differs between scenarios - the actor in the step's
 * name, the scope of the list, the weight, which tasks the gate may pick - comes in as
 * arguments; the shape of the act is stated once.
 */
final class CommonSteps {

    private CommonSteps() {
    }

    static boolean draw(int percent) {
        return ThreadLocalRandom.current().nextInt(100) < percent;
    }

    static Step lookingAtAList(StepKit kit, int weight, String name, Scope scope, Sort sort) {
        return kit.step(weight, StepKind.LOOKING_AT_A_LIST,
                listState(kit.fragmentRequest(name, SutSurface.TASKS,
                        ContentExpectation.rendersNoRefusalMark()), scope, sort));
    }

    static Step openingOneTask(StepKit kit, ScenarioWiring wiring, int weight, Role role,
            String name, Scope scope, Sort sort) {
        StepGate gate = pickVisibleTask(wiring, role, wiring.skew().taskOpensPercent(), name);
        DslHttpSampler request = listState(kit.fragmentRequest(name,
                SutSurface.task(StepKit.variable(IterationSlots.TASK)),
                ContentExpectation.rendersNoRefusalMark()), scope, sort);
        request.children(overATaskThatMayBeGone(wiring));
        return kit.step(weight, StepKind.OPENING_ONE_TASK, gate, request);
    }

    /** One visit to the notes tab that leaves one note: the discussion step of the mix. */
    static Step discussion(StepKit kit, ScenarioWiring wiring, int weight, String actorLabel,
            Scope scope, Sort sort, StepGate gate) {
        DslHttpSampler readsTheNotes = listState(kit.fragmentRequest(
                actorLabel + " reads the notes",
                SutSurface.taskNotes(StepKit.variable(IterationSlots.TASK)),
                ContentExpectation.rendersNoRefusalMark()), scope, sort);
        DslHttpSampler writesANote = listState(kit.fragmentRequest(
                actorLabel + " writes a note",
                SutSurface.taskNotes(StepKit.variable(IterationSlots.TASK)),
                ContentExpectation.rendersNoRefusalMark()), scope, sort)
                .method("POST")
                .param(SutSurface.NOTE_TEXT_FIELD, "a remark of " + wiring.runTag())
                .param(SutSurface.CSRF_FIELD, StepKit.variable(SessionSteps.SESSION_TOKEN_VARIABLE));
        readsTheNotes.children(overATaskThatMayBeGone(wiring));
        writesANote.children(overATaskThatMayBeGone(wiring));
        return kit.step(weight, StepKind.DISCUSSION, gate, readsTheNotes, writesANote);
    }

    /**
     * A transition whose task the gate leases from the registry, so two sessions never move one
     * task and a refused transition in the capture is the system's answer. The outcome the
     * answer reports is recorded back: an accepted transition moves the registry's fact, a
     * refused one returns the task unmoved.
     */
    static Step movingATask(StepKit kit, ScenarioWiring wiring, int weight,
            Transition transition, Role role, String name, Scope scope, Sort sort,
            String message) {
        StepGate gate = vars -> {
            String username = vars.get(SessionSteps.SESSION_USER_VARIABLE);
            TaskRegistry.Lease lease;
            try {
                lease = wiring.tasks().lease(transition, username, role);
            } catch (RegistryStarvedException starved) {
                wiring.starvation().skipped(name);
                return false;
            }
            vars.putObject(IterationSlots.MOVE_LEASE, lease);
            vars.put(IterationSlots.TASK, lease.task().taskId());
            return true;
        };
        DslHttpSampler request =
                transitionRequest(kit, wiring, name, transition, scope, sort, message);
        request.children(recordedOutcome(wiring));
        StepKind kind = transition == Transition.DELETE
                ? StepKind.DELETING_A_TASK
                : StepKind.MOVING_A_TASK;
        return kit.step(weight, kind, gate, request);
    }

    /** The transition request alone, for the steps that record their outcome differently. */
    static DslHttpSampler transitionRequest(StepKit kit, ScenarioWiring wiring, String name,
            Transition transition, Scope scope, Sort sort, String message) {
        DslHttpSampler request = listState(kit.fragmentRequest(name,
                SutSurface.taskTransition(StepKit.variable(IterationSlots.TASK), transition),
                ContentExpectation.rendersNoRefusalMark()), scope, sort)
                .method("POST")
                .param(SutSurface.CSRF_FIELD, StepKit.variable(SessionSteps.SESSION_TOKEN_VARIABLE));
        if (message != null) {
            request.param(SutSurface.TRANSITION_MESSAGE_FIELD, message);
        }
        request.children(overATaskThatMayBeGone(wiring));
        return request;
    }

    /**
     * The answer a step acting on a task the registry handed it is allowed to meet besides the
     * task itself: the task is gone, because another session deleted it in the seconds between
     * the pick and the request. The SUT confirms this as real user behaviour - a stale list row
     * clicked after a delete - and that a clean baseline may carry a handful of them, so the
     * sample stops counting as a failure of the script.
     *
     * <p>Three things happen and nothing else. The registry forgets the task, or it would hand
     * out the same ghost for the rest of the run. The ledger counts it, and the harness holds
     * that count against the deletions the profile intended, so a flood is still a spoiled
     * capture. And the sample is marked successful, which changes the flag alone: the result log
     * keeps the status the application really answered with.
     *
     * <p>Deliberately narrow. It is attached only to the requests that name a task the registry
     * picked; a list, a report or a sign-in meeting the same answer still fails the run, because
     * nothing could have deleted what they asked for.
     */
    static DslJsr223PostProcessor overATaskThatMayBeGone(ScenarioWiring wiring) {
        String namesNothing = String.valueOf(RefusalLedger.NAMES_NOTHING);
        return jsr223PostProcessor(s -> {
            if (!namesNothing.equals(s.prev.getResponseCode())) {
                return;
            }
            String taskId = s.vars.get(IterationSlots.TASK);
            if (taskId != null) {
                wiring.tasks().vanished(taskId);
            }
            wiring.refusals().aTaskVanishedUnderASession(taskId);
            s.prev.setSuccessful(true);
        });
    }

    /** Whether the answer says the task is gone, which no outcome may be recorded against. */
    static boolean theTaskIsGone(org.apache.jmeter.samplers.SampleResult result) {
        return String.valueOf(RefusalLedger.NAMES_NOTHING).equals(result.getResponseCode());
    }

    /**
     * A task the acting account may open, preferring the hot or the cold set by the profile's
     * draw and falling back to the other, because the skew states attention, not blindness: a
     * person finding nothing due soon reads something else.
     */
    static StepGate pickVisibleTask(ScenarioWiring wiring, Role role, int hotPercent,
            String stepName) {
        return vars -> {
            String username = vars.get(SessionSteps.SESSION_USER_VARIABLE);
            boolean hot = draw(hotPercent);
            TaskRegistry.TaskFacts task = visibleTask(wiring, username, role, hot);
            if (task == null) {
                task = visibleTask(wiring, username, role, !hot);
            }
            if (task == null) {
                wiring.starvation().skipped(stepName);
                return false;
            }
            vars.put(IterationSlots.TASK, task.taskId());
            return true;
        };
    }

    /** A hot task the acting manager neither created nor works on: the discussion's visit. */
    static StepGate pickHotTaskOutsideOwnArea(ScenarioWiring wiring, String stepName) {
        return vars -> {
            String username = vars.get(SessionSteps.SESSION_USER_VARIABLE);
            try {
                TaskRegistry.TaskFacts task =
                        wiring.tasks().hotTaskOutsideOwnArea(username, Role.MANAGER);
                vars.put(IterationSlots.TASK, task.taskId());
                return true;
            } catch (RegistryStarvedException starved) {
                wiring.starvation().skipped(stepName);
                return false;
            }
        };
    }

    /** Whether the answer accepted the act: the status held and no refusal was rendered. */
    static boolean accepted(org.apache.jmeter.samplers.SampleResult result, String answer) {
        return result.isSuccessful() && !answer.contains(SutSurface.REFUSED_MARK);
    }

    private static DslJsr223PostProcessor recordedOutcome(ScenarioWiring wiring) {
        return jsr223PostProcessor(s -> {
            TaskRegistry.Lease lease =
                    (TaskRegistry.Lease) s.vars.getObject(IterationSlots.MOVE_LEASE);
            if (lease == null) {
                return;
            }
            s.vars.putObject(IterationSlots.MOVE_LEASE, null);
            // The task was deleted under this session: there is no outcome to record and no lease
            // to return, because the tolerance above already forgot the task and its hold.
            if (theTaskIsGone(s.prev)) {
                return;
            }
            if (accepted(s.prev, s.prevResponse())) {
                wiring.tasks().applied(lease);
            } else {
                wiring.tasks().released(lease);
            }
        });
    }

    private static TaskRegistry.TaskFacts visibleTask(ScenarioWiring wiring, String username,
            Role role, boolean hot) {
        try {
            return wiring.tasks().toOpen(username, role, hot);
        } catch (RegistryStarvedException starved) {
            return null;
        }
    }

    private static DslHttpSampler listState(DslHttpSampler request, Scope scope, Sort sort) {
        return request
                .param(SutSurface.SCOPE_FIELD, scope.value())
                .param(SutSurface.SORT_FIELD, sort.value())
                .param(SutSurface.PAGE_FIELD, IterationSlots.FIRST_PAGE);
    }
}
