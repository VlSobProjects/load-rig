package loadrig.registry;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import loadrig.model.AccountPool;
import loadrig.model.Role;

/**
 * The rig's ledger of SUT sessions, decoupled from virtual users: a session belongs to an
 * account of the pool and lives across iterations, and a step acquires whichever session fits
 * it by state - a role, one named account such as the creator of the task being settled, a
 * manager other than named accounts - instead of every virtual user owning a login of its own.
 *
 * <p>The decoupling is what lets sign-ins be a share of the mix rather than a per-iteration
 * fixture, and what lets the discussion put several distinct people on one task. The registry
 * records state only; signing in and out is the transport's act, reported here by the holder of
 * the lease.
 */
public final class SessionRegistry {

    /** An exclusive hold on one account's session, or on the account while it is signed in. */
    public record Lease(String username, Role role) {
    }

    private enum State {

        SIGNED_OUT,
        SIGNED_IN,
        IN_USE
    }

    private final Map<String, Role> roles = new LinkedHashMap<>();
    private final Map<String, State> states = new LinkedHashMap<>();

    public SessionRegistry(Collection<AccountPool.Member> members) {
        for (AccountPool.Member member : members) {
            roles.put(member.username(), member.role());
            states.put(member.username(), State.SIGNED_OUT);
        }
        if (roles.isEmpty()) {
            throw new IllegalArgumentException("a session registry over no accounts can only starve");
        }
    }

    /** An account of the role without a live session, held for the sign-in step. */
    public synchronized Lease acquireToSignIn(Role role) {
        return firstFitting(State.SIGNED_OUT, role, null,
                "no account of role " + role + " is without a session");
    }

    /** A free signed-in session of the role. */
    public synchronized Lease acquire(Role role) {
        return firstFitting(State.SIGNED_IN, role, null,
                "no signed-in session of role " + role + " is free");
    }

    /** The session of the one account the step needs - the creator of the task it settles. */
    public synchronized Lease acquireOf(String username) {
        Role role = roles.get(username);
        if (role == null) {
            throw new IllegalArgumentException(username + " is not an account of the pool");
        }
        State state = states.get(username);
        if (state != State.SIGNED_IN) {
            throw new RegistryStarvedException("the session of " + username + " is not free: "
                    + (state == State.IN_USE ? "another step holds it" : "it is signed out"));
        }
        states.put(username, State.IN_USE);
        return new Lease(username, role);
    }

    /**
     * A free manager session that is none of the named accounts: the discussion scenario's
     * demand for a manager who is not the creator of the task they comment on.
     */
    public synchronized Lease acquireManagerOtherThan(Collection<String> excluded) {
        return firstFitting(State.SIGNED_IN, Role.MANAGER, excluded,
                "no signed-in manager session outside " + excluded + " is free");
    }

    /** Returns the session, signed in and free for the next step. */
    public synchronized void release(Lease lease) {
        held(lease);
        states.put(lease.username(), State.SIGNED_IN);
    }

    /** Records the sign-out: the account has no session until it signs in again. */
    public synchronized void signedOut(Lease lease) {
        held(lease);
        states.put(lease.username(), State.SIGNED_OUT);
    }

    private Lease firstFitting(State wanted, Role role, Collection<String> excluded,
            String starvedReason) {
        for (Map.Entry<String, Role> account : roles.entrySet()) {
            String username = account.getKey();
            if (account.getValue() != role || states.get(username) != wanted) {
                continue;
            }
            if (excluded != null && excluded.contains(username)) {
                continue;
            }
            states.put(username, State.IN_USE);
            return new Lease(username, account.getValue());
        }
        throw new RegistryStarvedException(starvedReason);
    }

    private void held(Lease lease) {
        if (states.get(lease.username()) != State.IN_USE) {
            throw new IllegalStateException(
                    "the lease on the session of " + lease.username() + " is not held");
        }
    }
}
