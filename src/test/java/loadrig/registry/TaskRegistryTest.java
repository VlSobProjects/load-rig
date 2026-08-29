package loadrig.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import loadrig.model.Role;
import loadrig.model.SutSurface.Transition;
import org.junit.jupiter.api.Test;

/**
 * Holds the task registry's obligations: a step gets a task it may act on and no other, a task
 * being moved is held by one step at a time, the recorded outcome follows the transition table,
 * and a registry with nothing that fits says so loudly instead of retrying.
 */
class TaskRegistryTest {

    private static final String CREATOR = "manager-01";
    private static final String ASSIGNEE = "worker-01";

    private final TaskRegistry registry = new TaskRegistry();

    @Test
    void aLeaseFindsTheTaskTheAccountMayMove() {
        registry.register(openTask("1", CREATOR, "worker-02", false));
        registry.register(openTask("2", CREATOR, ASSIGNEE, false));

        TaskRegistry.Lease lease = registry.lease(Transition.FINISH, ASSIGNEE, Role.WORKER);

        assertEquals("2", lease.task().taskId(),
                "the task of another assignee is not " + ASSIGNEE + "'s to finish");
    }

    @Test
    void aLeasedTaskIsNotHandedOutTwice() {
        registry.register(openTask("1", CREATOR, ASSIGNEE, false));
        registry.lease(Transition.FINISH, ASSIGNEE, Role.WORKER);

        assertThrows(RegistryStarvedException.class,
                () -> registry.lease(Transition.DELETE, CREATOR, Role.MANAGER),
                "a task being moved is held by one step at a time, whatever the second step is");
    }

    @Test
    void theOutcomeFollowsTheTable() {
        registry.register(openTask("1", CREATOR, ASSIGNEE, false));

        registry.applied(registry.lease(Transition.FINISH, ASSIGNEE, Role.WORKER));

        TaskRegistry.Lease approval = registry.lease(Transition.APPROVE, CREATOR, Role.MANAGER);
        assertEquals(TaskStatus.COMPLETED, approval.task().status());
    }

    @Test
    void aHandOutChoosesTheNextAssignee() {
        registry.register(new TaskRegistry.TaskFacts("1", TaskStatus.RETURNED, CREATOR, ASSIGNEE,
                false));

        registry.applied(registry.lease(Transition.HAND_OUT_AGAIN, CREATOR, Role.MANAGER),
                "worker-02");

        TaskRegistry.Lease next = registry.lease(Transition.FINISH, "worker-02", Role.WORKER);
        assertEquals(TaskStatus.OPEN, next.task().status());
        assertEquals("worker-02", next.task().assignee());
    }

    @Test
    void onlyAHandOutChoosesAnAssignee() {
        registry.register(openTask("1", CREATOR, ASSIGNEE, false));
        TaskRegistry.Lease lease = registry.lease(Transition.FINISH, ASSIGNEE, Role.WORKER);

        assertThrows(IllegalArgumentException.class, () -> registry.applied(lease, "worker-02"),
                "no other transition moves a task between people");
    }

    @Test
    void deletionRemovesTheTask() {
        registry.register(openTask("1", CREATOR, ASSIGNEE, false));

        registry.applied(registry.lease(Transition.DELETE, "admin-01", Role.ADMINISTRATOR));

        assertThrows(RegistryStarvedException.class,
                () -> registry.toOpen(CREATOR, Role.MANAGER, false),
                "a deleted task is gone with its history, not settled");
    }

    @Test
    void aReleasedTaskComesBackUnmoved() {
        registry.register(openTask("1", CREATOR, ASSIGNEE, false));
        registry.released(registry.lease(Transition.FINISH, ASSIGNEE, Role.WORKER));

        TaskRegistry.Lease again = registry.lease(Transition.FINISH, ASSIGNEE, Role.WORKER);
        assertEquals(TaskStatus.OPEN, again.task().status());
    }

    @Test
    void aStaleLeaseIsRefused() {
        registry.register(openTask("1", CREATOR, ASSIGNEE, false));
        TaskRegistry.Lease lease = registry.lease(Transition.FINISH, ASSIGNEE, Role.WORKER);
        registry.applied(lease);

        assertThrows(IllegalStateException.class, () -> registry.applied(lease),
                "an outcome is recorded once; a second recording would move the task twice");
    }

    @Test
    void starvationStatesTheReason() {
        registry.register(openTask("1", CREATOR, "worker-02", false));

        RegistryStarvedException starved = assertThrows(RegistryStarvedException.class,
                () -> registry.lease(Transition.FINISH, ASSIGNEE, Role.WORKER));

        assertTrue(starved.getMessage().contains(ASSIGNEE),
                "the reason names who asked");
        assertTrue(starved.getMessage().contains(Transition.FINISH.toString()),
                "the reason names what was asked for");
    }

    @Test
    void aWorkerSeesOnlyTheTasksAssignedToThem() {
        registry.register(openTask("1", CREATOR, "worker-02", false));

        assertThrows(RegistryStarvedException.class,
                () -> registry.toOpen(ASSIGNEE, Role.WORKER, false));
        assertEquals("1", registry.toOpen("worker-02", Role.WORKER, false).taskId());
        assertEquals("1", registry.toOpen(CREATOR, Role.MANAGER, false).taskId(),
                "a manager sees every task, not only the ones they handed out");
    }

    @Test
    void theHotSetIsTheHotFlagWhileTheTaskIsOpen() {
        registry.register(openTask("1", CREATOR, ASSIGNEE, true));

        assertEquals("1", registry.toOpen(ASSIGNEE, Role.WORKER, true).taskId());
        assertThrows(RegistryStarvedException.class,
                () -> registry.toOpen(ASSIGNEE, Role.WORKER, false),
                "a task in the hot set is not offered as a cold one");

        registry.applied(registry.lease(Transition.FINISH, ASSIGNEE, Role.WORKER));

        assertThrows(RegistryStarvedException.class,
                () -> registry.toOpen(ASSIGNEE, Role.WORKER, true),
                "the specification's hot task is an open one, so settling leaves the hot set");
        assertEquals("1", registry.toOpen(ASSIGNEE, Role.WORKER, false).taskId());
    }

    @Test
    void theDiscussionVisitIsAHotTaskOfSomebodyElse() {
        registry.register(openTask("1", "manager-02", "worker-02", true));
        registry.register(openTask("2", CREATOR, ASSIGNEE, true));

        assertEquals("1", registry.hotTaskOutsideOwnArea(CREATOR, Role.MANAGER).taskId(),
                "the manager's own task is not a visit outside their area");
        assertThrows(IllegalArgumentException.class,
                () -> registry.hotTaskOutsideOwnArea("admin-01", Role.ADMINISTRATOR),
                "an administrator is offered no note form");
    }

    @Test
    void theWholeTableIsWalkableThroughTheRegistry() {
        registry.register(openTask("1", CREATOR, ASSIGNEE, false));

        registry.applied(registry.lease(Transition.RETURN_WITH_A_QUESTION, ASSIGNEE,
                Role.WORKER));
        registry.applied(registry.lease(Transition.HAND_OUT_AGAIN, CREATOR, Role.MANAGER),
                "worker-02");
        registry.applied(registry.lease(Transition.FINISH, "worker-02", Role.WORKER));
        registry.applied(registry.lease(Transition.APPROVE, CREATOR, Role.MANAGER));
        registry.applied(registry.lease(Transition.PUT_BACK_TO_WORK, CREATOR, Role.MANAGER));
        registry.applied(registry.lease(Transition.REFUSE, "worker-02", Role.WORKER));
        registry.applied(registry.lease(Transition.ACCEPT_THE_REFUSAL, CREATOR, Role.MANAGER));
        registry.applied(registry.lease(Transition.DELETE, CREATOR, Role.MANAGER));

        assertThrows(RegistryStarvedException.class,
                () -> registry.toOpen(CREATOR, Role.MANAGER, false),
                "the walk ends with the table empty: every transition and every status was real");
    }

    private static TaskRegistry.TaskFacts openTask(String taskId, String creator, String assignee,
            boolean hot) {
        return new TaskRegistry.TaskFacts(taskId, TaskStatus.OPEN, creator, assignee, hot);
    }
}
