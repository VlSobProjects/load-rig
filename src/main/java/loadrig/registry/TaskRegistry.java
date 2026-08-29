package loadrig.registry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import loadrig.model.Role;
import loadrig.model.SutSurface.Transition;

/**
 * The rig's own ledger of the tasks a run acts on: id, status, creator, assignee and hot-set
 * membership, kept in the injector JVM so a scenario step finds a task it may act on without
 * asking the system under test.
 *
 * <p>A step that moves a task leases it first, exclusively: two sessions never move one task,
 * so a refused transition in a capture is the system's answer and not the rig racing itself.
 * Reading needs no lease - several sessions opening one hot task at once is the discussion
 * scenario, the one deliberate concurrency of the profile.
 *
 * <p>A lease is handed out first fit; a read is drawn at random among the fitting tasks, so the
 * attention the skew sends to the hot set spreads over the set. How often a step asks for a hot
 * task rather than a cold one is the profile's skew and lives with the profile, not here.
 */
public final class TaskRegistry {

    /**
     * What the registry knows about one task. The hot flag marks membership of the set the
     * discussion converges on; the specification's hot task is an open one due soon, so the
     * flag counts only while the task is {@code OPEN}.
     */
    public record TaskFacts(String taskId, TaskStatus status, String creator, String assignee,
            boolean hot) {

        public TaskFacts {
            Objects.requireNonNull(taskId, "taskId");
            Objects.requireNonNull(status, "status");
            Objects.requireNonNull(creator, "creator");
            Objects.requireNonNull(assignee, "assignee");
            if (taskId.isBlank()) {
                throw new IllegalArgumentException("a task with a blank id cannot be acted on");
            }
        }
    }

    /** An exclusive hold on one task for one transition, until the outcome is recorded. */
    public record Lease(TaskFacts task, Transition transition) {
    }

    private final Map<String, TaskFacts> tasks = new LinkedHashMap<>();
    private final Set<String> leasedIds = new HashSet<>();

    /** Registers a task the run created, or one the seeded history is known to hold. */
    public synchronized void register(TaskFacts facts) {
        if (tasks.containsKey(facts.taskId())) {
            throw new IllegalArgumentException("the task " + facts.taskId()
                    + " is already registered; one row of the system is one entry here");
        }
        tasks.put(facts.taskId(), facts);
    }

    /**
     * A task the account may apply the transition to, held exclusively until
     * {@link #applied(Lease)} or {@link #released(Lease)}.
     */
    public synchronized Lease lease(Transition transition, String username, Role role) {
        for (TaskFacts facts : tasks.values()) {
            if (leasedIds.contains(facts.taskId())) {
                continue;
            }
            if (TransitionTable.permits(transition, facts, username, role)) {
                leasedIds.add(facts.taskId());
                return new Lease(facts, transition);
            }
        }
        throw new RegistryStarvedException("no free task " + username + " may apply " + transition
                + " to, among the " + tasks.size() + " registered");
    }

    /** Records the outcome of the leased transition and frees the task. */
    public synchronized void applied(Lease lease) {
        TaskFacts held = heldTask(lease);
        if (TransitionTable.removes(lease.transition())) {
            tasks.remove(held.taskId());
            leasedIds.remove(held.taskId());
            return;
        }
        moved(held, lease.transition(), held.assignee());
    }

    /** Records a hand-out, whose one degree of freedom is the next assignee. */
    public synchronized void applied(Lease lease, String nextAssignee) {
        if (lease.transition() != Transition.HAND_OUT_AGAIN) {
            throw new IllegalArgumentException(
                    "only a hand-out chooses an assignee; " + lease.transition() + " does not");
        }
        Objects.requireNonNull(nextAssignee, "nextAssignee");
        moved(heldTask(lease), lease.transition(), nextAssignee);
    }

    /** Returns the task unmoved, for a transition the walk did not get to apply. */
    public synchronized void released(Lease lease) {
        heldTask(lease);
        leasedIds.remove(lease.task().taskId());
    }

    /**
     * Forgets a task the system no longer holds: another session deleted it between this one's
     * pick and its request, and the answer named nothing. Neither a transition nor a release -
     * the row is gone, and a registry that kept it would hand out a ghost and spend the rest of
     * the run's steps learning the same thing again.
     *
     * <p>Deliberately tolerant of a task it does not know and of a lease it does not hold: two
     * sessions can meet the same deletion, and the second must not fail over the first having
     * already recorded it.
     */
    public synchronized void vanished(String taskId) {
        tasks.remove(taskId);
        leasedIds.remove(taskId);
    }

    /**
     * A task the account may open for reading, from the hot set or from the rest, drawn at
     * random among the fitting ones: the skew decides how much attention the hot set receives,
     * and the draw spreads that attention over the set instead of converging on its first
     * member alone.
     */
    public synchronized TaskFacts toOpen(String username, Role role, boolean hot) {
        List<TaskFacts> fitting = new ArrayList<>();
        for (TaskFacts facts : tasks.values()) {
            if (inHotSet(facts) == hot && visibleTo(facts, username, role)) {
                fitting.add(facts);
            }
        }
        if (fitting.isEmpty()) {
            throw new RegistryStarvedException("no " + (hot ? "hot" : "cold")
                    + " task is visible to " + username + " among the " + tasks.size()
                    + " registered");
        }
        return fitting.get(ThreadLocalRandom.current().nextInt(fitting.size()));
    }

    /**
     * A hot task the manager neither created nor works on: the discussion scenario's visit to a
     * task outside one's own area, which opens read-only and still offers the note form. Only a
     * manager makes it - an administrator is offered no note form, and a worker sees only the
     * tasks assigned to them.
     */
    public synchronized TaskFacts hotTaskOutsideOwnArea(String username, Role role) {
        if (role != Role.MANAGER) {
            throw new IllegalArgumentException(
                    "only a manager visits a task outside their own area; " + username + " is "
                            + role);
        }
        List<TaskFacts> fitting = new ArrayList<>();
        for (TaskFacts facts : tasks.values()) {
            if (inHotSet(facts) && !username.equals(facts.creator())
                    && !username.equals(facts.assignee())) {
                fitting.add(facts);
            }
        }
        if (fitting.isEmpty()) {
            throw new RegistryStarvedException("no hot task is outside the area of " + username
                    + " among the " + tasks.size() + " registered");
        }
        return fitting.get(ThreadLocalRandom.current().nextInt(fitting.size()));
    }

    private void moved(TaskFacts held, Transition transition, String assignee) {
        tasks.put(held.taskId(), new TaskFacts(held.taskId(), TransitionTable.movesTo(transition),
                held.creator(), assignee, held.hot()));
        leasedIds.remove(held.taskId());
    }

    private TaskFacts heldTask(Lease lease) {
        String taskId = lease.task().taskId();
        TaskFacts facts = tasks.get(taskId);
        if (facts == null || !leasedIds.contains(taskId)) {
            throw new IllegalStateException(
                    "the lease on task " + taskId + " is not held by this registry");
        }
        return facts;
    }

    private static boolean inHotSet(TaskFacts facts) {
        return facts.hot() && facts.status() == TaskStatus.OPEN;
    }

    private static boolean visibleTo(TaskFacts facts, String username, Role role) {
        return switch (role) {
            case MANAGER, ADMINISTRATOR -> true;
            // A returned task leaves the worker's sight: it sits with the creator awaiting the
            // answer, out of the assignee's queue, and the stand refuses the assignee's open of
            // it. Every other status stays visible to the assignee - proven against the stand
            // status by status.
            case WORKER -> username.equals(facts.assignee())
                    && facts.status() != TaskStatus.RETURNED;
        };
    }
}
