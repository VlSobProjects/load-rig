package loadrig.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.EnumMap;
import java.util.Map;
import loadrig.model.SutSurface.Scope;
import loadrig.model.SutSurface.Sort;
import loadrig.model.SutSurface.Transition;
import org.junit.jupiter.api.Test;

/**
 * Proves what can be proven about the walk without a running stack: that it assembles, that it
 * refuses to assemble without an account for every role, and that the addresses it drives are the
 * ones the specification's appendix states.
 *
 * <p>Whether the walk actually drives the application is proven by a run, and a run is recorded in
 * the session note rather than in a test.
 */
class TransportWalkTest {

    private static final String A_STACK = "http://an.address.of.a.stack:8080";
    private static final String A_PASSWORD = "loadrig123!";
    private static final String A_RUN = "20260828-093000";

    @Test
    void theWalkAssemblesIntoATestPlan() {
        var plan = new TransportWalk(A_STACK, allRoles(), A_PASSWORD, A_RUN)
                .plan(2, 1, "results", "a-run.jtl");

        assertNotNull(plan);
    }

    @Test
    void aWalkWithoutAnAccountForEveryRoleDoesNotAssemble() {
        Map<Role, Account> incomplete = new EnumMap<>(Role.class);
        incomplete.put(Role.WORKER, new Account("worker", "worker123!"));

        assertThrows(IllegalArgumentException.class,
                () -> new TransportWalk(A_STACK, incomplete, A_PASSWORD, A_RUN));
    }

    @Test
    void theAddressesAreTheOnesTheSpecificationStates() {
        assertEquals("/tasks/7", SutSurface.task("7"));
        assertEquals("/tasks/7/notes", SutSurface.taskNotes("7"));
        assertEquals("/tasks/7/complete", SutSurface.taskTransition("7", Transition.FINISH));
        assertEquals("/tasks/7/acknowledge",
                SutSurface.taskTransition("7", Transition.ACCEPT_THE_REFUSAL));
        assertEquals("/users/7/delete", SutSurface.userDeletion("7"));
    }

    @Test
    void theListStateIsTheClosedListTheApplicationAccepts() {
        assertEquals("all", Scope.ALL.value());
        assertEquals("by-me", Scope.ASSIGNED_BY_ME.value());
        assertEquals("to-me", Scope.ASSIGNED_TO_ME.value());
        assertEquals(3, Scope.values().length);

        assertEquals("created", Sort.CREATED.value());
        assertEquals("due", Sort.DUE_DATE.value());
        assertEquals(4, Sort.values().length);
    }

    private static Map<Role, Account> allRoles() {
        Map<Role, Account> accounts = new EnumMap<>(Role.class);
        accounts.put(Role.WORKER, new Account("worker", "worker123!"));
        accounts.put(Role.MANAGER, new Account("manager", "manager123!"));
        accounts.put(Role.ADMINISTRATOR, new Account("admin", "admin123!"));
        return accounts;
    }
}
