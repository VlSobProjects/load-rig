package loadrig.registry;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import loadrig.model.Role;
import loadrig.model.SutSurface.Transition;

/**
 * The task transition table of the system under test, mirrored as the rig's own fact: which
 * statuses each transition moves a task from, where it lands, and whose relation to the task it
 * demands.
 *
 * <p>Derived from the scenario specification: the assignee finishes, returns or refuses an open
 * task; the creator approves finished work, accepts a refusal, answers a returned question by
 * handing the task out again, and puts settled work back into circulation; deletion belongs to
 * the creator and to the administrator and takes the task with it, whatever its status. Notes
 * are absent on purpose: a note follows visibility, not this table, and moves nothing.
 *
 * <p>Relations decide, not roles - a manager assigned a task by another manager finishes it as
 * its assignee. The one right a role owns is the administrator's right to delete.
 */
public final class TransitionTable {

    /** Whose relation to the task a transition demands. */
    public enum ActingParty {

        THE_ASSIGNEE,
        THE_CREATOR,
        THE_CREATOR_OR_AN_ADMINISTRATOR
    }

    private record Rule(Set<TaskStatus> from, TaskStatus to, ActingParty party) {
    }

    private static final Map<Transition, Rule> RULES = new EnumMap<>(Map.of(
            Transition.FINISH,
            new Rule(EnumSet.of(TaskStatus.OPEN), TaskStatus.COMPLETED, ActingParty.THE_ASSIGNEE),
            Transition.RETURN_WITH_A_QUESTION,
            new Rule(EnumSet.of(TaskStatus.OPEN), TaskStatus.RETURNED, ActingParty.THE_ASSIGNEE),
            Transition.REFUSE,
            new Rule(EnumSet.of(TaskStatus.OPEN), TaskStatus.REFUSED, ActingParty.THE_ASSIGNEE),
            Transition.HAND_OUT_AGAIN,
            new Rule(EnumSet.of(TaskStatus.RETURNED), TaskStatus.OPEN, ActingParty.THE_CREATOR),
            Transition.APPROVE,
            new Rule(EnumSet.of(TaskStatus.COMPLETED), TaskStatus.APPROVED, ActingParty.THE_CREATOR),
            Transition.ACCEPT_THE_REFUSAL,
            new Rule(EnumSet.of(TaskStatus.REFUSED), TaskStatus.ACKNOWLEDGED, ActingParty.THE_CREATOR),
            Transition.PUT_BACK_TO_WORK,
            new Rule(EnumSet.of(TaskStatus.APPROVED, TaskStatus.ACKNOWLEDGED), TaskStatus.OPEN,
                    ActingParty.THE_CREATOR),
            Transition.DELETE,
            new Rule(EnumSet.allOf(TaskStatus.class), null,
                    ActingParty.THE_CREATOR_OR_AN_ADMINISTRATOR)));

    /** The statuses the transition may be applied from. */
    public static Set<TaskStatus> movesFrom(Transition transition) {
        return Set.copyOf(ruleOf(transition).from());
    }

    /** Where the transition lands a task. Deletion lands nowhere: ask {@link #removes} first. */
    public static TaskStatus movesTo(Transition transition) {
        Rule rule = ruleOf(transition);
        if (rule.to() == null) {
            throw new IllegalArgumentException(
                    transition + " removes the task instead of moving it");
        }
        return rule.to();
    }

    /** Whether the transition removes the task from the system rather than moving it. */
    public static boolean removes(Transition transition) {
        return ruleOf(transition).to() == null;
    }

    public static ActingParty actingParty(Transition transition) {
        return ruleOf(transition).party();
    }

    /** Whether the account may apply the transition to the task as the registry knows it. */
    public static boolean permits(Transition transition, TaskRegistry.TaskFacts task,
            String username, Role role) {
        Rule rule = ruleOf(transition);
        if (!rule.from().contains(task.status())) {
            return false;
        }
        return switch (rule.party()) {
            case THE_ASSIGNEE -> username.equals(task.assignee());
            case THE_CREATOR -> username.equals(task.creator());
            case THE_CREATOR_OR_AN_ADMINISTRATOR ->
                    username.equals(task.creator()) || role == Role.ADMINISTRATOR;
        };
    }

    private static Rule ruleOf(Transition transition) {
        Rule rule = RULES.get(transition);
        if (rule == null) {
            throw new IllegalArgumentException(
                    "the transition table holds no rule for " + transition);
        }
        return rule;
    }

    private TransitionTable() {
    }
}
