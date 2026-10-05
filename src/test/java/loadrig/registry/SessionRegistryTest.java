package loadrig.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.function.ToIntFunction;
import loadrig.model.AccountPool;
import loadrig.model.Role;
import org.junit.jupiter.api.Test;

/**
 * Holds the session registry's obligations: sessions are decoupled from virtual users and
 * acquired by state, one holder at a time, the transport context travels with the session, and
 * a registry with no fitting session says so loudly instead of retrying.
 */
class SessionRegistryTest {

    private static final List<AccountPool.Member> MEMBERS = List.of(
            new AccountPool.Member("manager-01", Role.MANAGER),
            new AccountPool.Member("manager-02", Role.MANAGER),
            new AccountPool.Member("worker-01", Role.WORKER));

    /** A stand where nothing is waiting for anybody: the choice falls to the other two facts. */
    private static final ToIntFunction<String> NOTHING_WAITING = username -> 0;

    private final SessionRegistry registry = new SessionRegistry(MEMBERS);

    @Test
    void aSignInTakesAnAccountWithoutASession() {
        SessionRegistry.Lease lease = registry.acquireToSignIn(Role.MANAGER, NOTHING_WAITING);

        assertEquals("manager-01", lease.username(),
                "with nothing to tell two idle accounts apart, the pool's order decides");
        assertThrows(RegistryStarvedException.class, () -> registry.acquireOf("manager-01"),
                "an account being signed in is not free until its holder releases it");

        registry.attach(lease, aContext("a-token"));
        registry.release(lease);
        assertEquals("manager-01", registry.acquireOf("manager-01").username());
    }

    @Test
    void theAccountWithWorkWaitingSignsInFirst() {
        SessionRegistry.Lease lease = registry.acquireToSignIn(Role.MANAGER,
                username -> username.equals("manager-02") ? 4 : 0);

        assertEquals("manager-02", lease.username(),
                "an account with nothing it may act on takes a seat and starves the gates;"
                        + " the depth of the backlog decides before anything else");
    }

    @Test
    void amongEquallySuppliedAccountsTheOneThatHasDoneLeastComesBack() {
        signIn(Role.MANAGER);
        registry.signedOut(registry.acquireOf("manager-01"));

        SessionRegistry.Lease lease = registry.acquireToSignIn(Role.MANAGER, NOTHING_WAITING);

        assertEquals("manager-02", lease.username(),
                "with the backlogs equal, the account that has taken fewer acts comes back");
    }

    @Test
    void theLongestIdleBreaksWhatIsStillTied() {
        signIn(Role.MANAGER);
        registry.signedOut(registry.acquireOf("manager-01"));
        signIn(Role.MANAGER);
        registry.signedOut(registry.acquireOf("manager-02"));

        SessionRegistry.Lease lease = registry.acquireToSignIn(Role.MANAGER, NOTHING_WAITING);

        assertEquals("manager-01", lease.username(),
                "the two have done the same; the one whose last act is older comes back");
    }

    @Test
    void acquiringByRoleFindsOnlyAFreeSignedInSession() {
        assertThrows(RegistryStarvedException.class, () -> registry.acquire(Role.MANAGER),
                "no session exists before a sign-in");

        signIn(Role.MANAGER);

        assertEquals("manager-01", registry.acquire(Role.MANAGER).username());
    }

    @Test
    void aSessionIsHeldByOneStepAtATime() {
        signIn(Role.MANAGER);
        registry.acquire(Role.MANAGER);

        assertThrows(RegistryStarvedException.class, () -> registry.acquire(Role.MANAGER));
    }

    @Test
    void theDiscussionAcquiresAManagerOtherThanTheNamedOnes() {
        signIn(Role.MANAGER);
        signIn(Role.MANAGER);

        SessionRegistry.Lease other = registry.acquireManagerOtherThan(Set.of("manager-01"));

        assertEquals("manager-02", other.username(),
                "the discussion needs a manager who is not the creator of the task");
    }

    @Test
    void theContextTravelsWithTheSession() {
        SessionRegistry.Lease signedIn = registry.acquireToSignIn(Role.WORKER, NOTHING_WAITING);
        registry.attach(signedIn, aContext("the-token-of-the-sign-in"));
        registry.release(signedIn);

        SessionRegistry.Lease held = registry.acquire(Role.WORKER);
        assertEquals("the-token-of-the-sign-in", registry.contextOf(held).pageToken(),
                "the next holder reads the context the sign-in attached");

        registry.attach(held, aContext("the-token-after-more-steps"));
        registry.release(held);
        assertEquals("the-token-after-more-steps",
                registry.contextOf(registry.acquire(Role.WORKER)).pageToken(),
                "a holder that moved the context on hands the moved context over");
    }

    @Test
    void releasingWithoutAContextIsRefused() {
        SessionRegistry.Lease lease = registry.acquireToSignIn(Role.WORKER, NOTHING_WAITING);

        assertThrows(IllegalStateException.class, () -> registry.release(lease),
                "a session released as signed in must carry its transport context");
    }

    @Test
    void aSignOutEndsTheSessionAndDropsTheContext() {
        signIn(Role.WORKER);
        registry.signedOut(registry.acquireOf("worker-01"));

        assertThrows(RegistryStarvedException.class, () -> registry.acquireOf("worker-01"),
                "a signed-out account has no session to acquire");

        SessionRegistry.Lease again = registry.acquireToSignIn(Role.WORKER, NOTHING_WAITING);
        assertEquals("worker-01", again.username(),
                "a signed-out account is again an account to sign in with");
        assertThrows(IllegalStateException.class, () -> registry.contextOf(again),
                "the sign-out dropped the context with the session, in one act");
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
        SessionRegistry.Lease lease = registry.acquireToSignIn(Role.MANAGER, NOTHING_WAITING);
        registry.attach(lease, aContext("a-token"));
        registry.release(lease);

        assertThrows(IllegalStateException.class, () -> registry.release(lease),
                "a released session is not the releaser's to move again");
    }

    @Test
    void theNamesOfARoleAreThePoolsOwn() {
        assertEquals(List.of("manager-01", "manager-02"), registry.namesOf(Role.MANAGER));
        assertEquals(List.of(), registry.namesOf(Role.ADMINISTRATOR));
    }

    @Test
    void aRegistryOverNoAccountsIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> new SessionRegistry(List.of()));
    }

    private void signIn(Role role) {
        SessionRegistry.Lease lease = registry.acquireToSignIn(role, NOTHING_WAITING);
        registry.attach(lease, aContext("a-token"));
        registry.release(lease);
    }

    private static TransportContext aContext(String token) {
        return new TransportContext(List.of(new TransportContext.CookieFact(
                "SESSION", "a-cookie", "an.address.of.a.stack", "/", false, 0)), token);
    }
}
