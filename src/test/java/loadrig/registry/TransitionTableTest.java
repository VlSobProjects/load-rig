package loadrig.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import loadrig.model.Role;
import loadrig.model.SutSurface.Transition;
import org.junit.jupiter.api.Test;

/**
 * Holds the mirrored transition table against the scenario specification: every transition of
 * the surface has a rule, the statuses and the acting parties are the specification's, and the
 * permission check follows relations rather than roles.
 */
class TransitionTableTest {

    private static final TaskRegistry.TaskFacts OPEN_TASK =
            new TaskRegistry.TaskFacts("7", TaskStatus.OPEN, "manager-01", "worker-01", false);

    @Test
    void everyTransitionOfTheSurfaceHasARule() {
        for (Transition transition : Transition.values()) {
            assertFalse(TransitionTable.movesFrom(transition).isEmpty(),
                    transition + " must move from somewhere");
            assertNotNull(TransitionTable.actingParty(transition),
                    transition + " must name who applies it");
            if (!TransitionTable.removes(transition)) {
                assertNotNull(TransitionTable.movesTo(transition),
                        transition + " must land somewhere");
            }
        }
    }

    @Test
    void theAssigneeMovesAnOpenTask() {
        for (Transition transition : Set.of(Transition.FINISH, Transition.RETURN_WITH_A_QUESTION,
                Transition.REFUSE)) {
            assertEquals(Set.of(TaskStatus.OPEN), TransitionTable.movesFrom(transition));
            assertEquals(TransitionTable.ActingParty.THE_ASSIGNEE,
                    TransitionTable.actingParty(transition));
        }
        assertEquals(TaskStatus.COMPLETED, TransitionTable.movesTo(Transition.FINISH));
        assertEquals(TaskStatus.RETURNED,
                TransitionTable.movesTo(Transition.RETURN_WITH_A_QUESTION));
        assertEquals(TaskStatus.REFUSED, TransitionTable.movesTo(Transition.REFUSE));
    }

    @Test
    void theCreatorSettlesWhatComesBack() {
        assertEquals(Set.of(TaskStatus.COMPLETED), TransitionTable.movesFrom(Transition.APPROVE));
        assertEquals(TaskStatus.APPROVED, TransitionTable.movesTo(Transition.APPROVE));

        assertEquals(Set.of(TaskStatus.REFUSED),
                TransitionTable.movesFrom(Transition.ACCEPT_THE_REFUSAL));
        assertEquals(TaskStatus.ACKNOWLEDGED,
                TransitionTable.movesTo(Transition.ACCEPT_THE_REFUSAL));

        assertEquals(Set.of(TaskStatus.RETURNED),
                TransitionTable.movesFrom(Transition.HAND_OUT_AGAIN));
        assertEquals(TaskStatus.OPEN, TransitionTable.movesTo(Transition.HAND_OUT_AGAIN));

        for (Transition transition : Set.of(Transition.APPROVE, Transition.ACCEPT_THE_REFUSAL,
                Transition.HAND_OUT_AGAIN, Transition.PUT_BACK_TO_WORK)) {
            assertEquals(TransitionTable.ActingParty.THE_CREATOR,
                    TransitionTable.actingParty(transition));
        }
    }

    @Test
    void onlySettledWorkGoesBackToWork() {
        assertEquals(Set.of(TaskStatus.APPROVED, TaskStatus.ACKNOWLEDGED),
                TransitionTable.movesFrom(Transition.PUT_BACK_TO_WORK));
        assertEquals(TaskStatus.OPEN, TransitionTable.movesTo(Transition.PUT_BACK_TO_WORK));
    }

    @Test
    void deletionTakesATaskFromAnyStatusAndLandsNowhere() {
        assertEquals(Set.of(TaskStatus.values()),
                TransitionTable.movesFrom(Transition.DELETE));
        assertTrue(TransitionTable.removes(Transition.DELETE));
        assertEquals(TransitionTable.ActingParty.THE_CREATOR_OR_AN_ADMINISTRATOR,
                TransitionTable.actingParty(Transition.DELETE));
        assertThrows(IllegalArgumentException.class,
                () -> TransitionTable.movesTo(Transition.DELETE),
                "a deletion that lands somewhere would keep a removed task in circulation");
    }

    @Test
    void relationsPermitAndStrangersAreRefused() {
        assertTrue(TransitionTable.permits(Transition.FINISH, OPEN_TASK, "worker-01",
                Role.WORKER));
        assertFalse(TransitionTable.permits(Transition.FINISH, OPEN_TASK, "worker-02",
                Role.WORKER), "only the assignee finishes");
        assertFalse(TransitionTable.permits(Transition.FINISH, OPEN_TASK, "manager-01",
                Role.MANAGER), "the creator hands out work but does not finish it");
    }

    @Test
    void aWrongStatusRefusesEvenTheRightParty() {
        TaskRegistry.TaskFacts completed =
                new TaskRegistry.TaskFacts("7", TaskStatus.COMPLETED, "manager-01", "worker-01",
                        false);

        assertFalse(TransitionTable.permits(Transition.FINISH, completed, "worker-01",
                Role.WORKER), "finished work cannot be finished again");
        assertTrue(TransitionTable.permits(Transition.APPROVE, completed, "manager-01",
                Role.MANAGER));
    }

    @Test
    void theAdministratorsOneRightIsDeletion() {
        assertTrue(TransitionTable.permits(Transition.DELETE, OPEN_TASK, "admin-01",
                Role.ADMINISTRATOR), "deletion is the one act the role owns");
        assertTrue(TransitionTable.permits(Transition.DELETE, OPEN_TASK, "manager-01",
                Role.MANAGER), "the creator deletes their own");
        assertFalse(TransitionTable.permits(Transition.DELETE, OPEN_TASK, "manager-02",
                Role.MANAGER), "another manager does not");
        assertFalse(TransitionTable.permits(Transition.APPROVE,
                new TaskRegistry.TaskFacts("7", TaskStatus.COMPLETED, "manager-01", "worker-01",
                        false),
                "admin-01", Role.ADMINISTRATOR),
                "an administrator moves nothing: relations decide, not the role");
    }
}
