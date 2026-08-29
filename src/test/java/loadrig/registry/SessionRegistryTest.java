package loadrig.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import loadrig.model.AccountPool;
import loadrig.model.Role;
import org.junit.jupiter.api.Test;

/**
 * Holds the session registry's obligations: sessions are decoupled from virtual users and
 * acquired by state, one holder at a time, and a registry with no fitting session says so
 * loudly instead of retrying.
 */
class SessionRegistryTest {

    private static final List<AccountPool.Member> MEMBERS = List.of(
            new AccountPool.Member("manager-01", Role.MANAGER),
            new AccountPool.Member("manager-02", Role.MANAGER),
            new AccountPool.Member("worker-01", Role.WORKER));

    private final SessionRegistry registry = new SessionRegistry(MEMBERS);

    @Test
    void aSignInTakesAnAccountWithoutASession() {
        SessionRegistry.Lease lease = registry.acquireToSignIn(Role.MANAGER);

        assertEquals("manager-01", lease.username());
        assertThrows(RegistryStarvedException.class, () -> registry.acquireOf("manager-01"),
                "an account being signed in is not free until its holder releases it");

        registry.release(lease);
        assertEquals("manager-01", registry.acquireOf("manager-01").username());
    }

    @Test
    void acquiringByRoleFindsOnlyAFreeSignedInSession() {
        assertThrows(RegistryStarvedException.class, () -> registry.acquire(Role.MANAGER),
                "no session exists before a sign-in");

        registry.release(registry.acquireToSignIn(Role.MANAGER));

        assertEquals("manager-01", registry.acquire(Role.MANAGER).username());
    }

    @Test
    void aSessionIsHeldByOneStepAtATime() {
        registry.release(registry.acquireToSignIn(Role.MANAGER));
        registry.acquire(Role.MANAGER);

        assertThrows(RegistryStarvedException.class, () -> registry.acquire(Role.MANAGER));
    }

    @Test
    void theDiscussionAcquiresAManagerOtherThanTheNamedOnes() {
        registry.release(registry.acquireToSignIn(Role.MANAGER));
        registry.release(registry.acquireToSignIn(Role.MANAGER));

        SessionRegistry.Lease other = registry.acquireManagerOtherThan(Set.of("manager-01"));

        assertEquals("manager-02", other.username(),
                "the discussion needs a manager who is not the creator of the task");
    }

    @Test
    void aSignOutEndsTheSession() {
        registry.release(registry.acquireToSignIn(Role.WORKER));
        registry.signedOut(registry.acquireOf("worker-01"));

        assertThrows(RegistryStarvedException.class, () -> registry.acquireOf("worker-01"),
                "a signed-out account has no session to acquire");
        assertEquals("worker-01", registry.acquireToSignIn(Role.WORKER).username(),
                "a signed-out account is again an account to sign in with");
    }

    @Test
    void starvationStatesTheReason() {
        RegistryStarvedException starved = assertThrows(RegistryStarvedException.class,
                () -> registry.acquire(Role.WORKER));

        assertTrue(starved.getMessage().contains(Role.WORKER.toString()),
                "the reason names what was asked for");
    }

    @Test
    void anAccountOutsideThePoolIsRefusedOutright() {
        assertThrows(IllegalArgumentException.class, () -> registry.acquireOf("stranger"),
                "a stranger is a wiring mistake, not starvation");
    }

    @Test
    void aStaleLeaseIsRefused() {
        SessionRegistry.Lease lease = registry.acquireToSignIn(Role.MANAGER);
        registry.release(lease);

        assertThrows(IllegalStateException.class, () -> registry.release(lease),
                "a released session is not the releaser's to move again");
    }

    @Test
    void aRegistryOverNoAccountsIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> new SessionRegistry(List.of()));
    }
}
