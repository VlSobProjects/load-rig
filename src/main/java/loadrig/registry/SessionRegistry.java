package loadrig.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
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
 * the lease together with the {@link TransportContext} the session travels with: a session is
 * signed in exactly while a context is attached to it.
 *
 * <p>Which fitting session an acquisition gets is drawn at random, so that the load spreads
 * over the pool instead of wearing the first accounts - a run whose every note is written by
 * the same manager measures one person's cache, not a population. The one deliberate exception
 * is {@link #acquireToSignIn}: sign-ins fill the pool in its stable order, so which accounts a
 * small run occupies is predictable.
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
    private final Map<String, TransportContext> contexts = new LinkedHashMap<>();

    public SessionRegistry(Collection<AccountPool.Member> members) {
        for (AccountPool.Member member : members) {
            roles.put(member.username(), member.role());
            states.put(member.username(), State.SIGNED_OUT);
        }
        if (roles.isEmpty()) {
            throw new IllegalArgumentException("a session registry over no accounts can only starve");
        }
    }

    /**
     * The accounts of the role, in the pool's stable order: the ceiling a population is seated
     * against, and the names a step draws an assignee or a report subject from.
     */
    public synchronized List<String> namesOf(Role role) {
        List<String> names = new ArrayList<>();
        roles.forEach((username, accountRole) -> {
            if (accountRole == role) {
                names.add(username);
            }
        });
        return names;
    }

    /** An account of the role without a live session, held for the sign-in step. */
    public synchronized Lease acquireToSignIn(Role role) {
        List<Lease> fitting = allFitting(State.SIGNED_OUT, role, null);
        if (fitting.isEmpty()) {
            throw new RegistryStarvedException(
                    "no account of role " + role + " is without a session");
        }
        return held(fitting.get(0));
    }

    /** A free signed-in session of the role, drawn at random among the fitting ones. */
    public synchronized Lease acquire(Role role) {
        return anyFitting(State.SIGNED_IN, role, null,
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
        return held(new Lease(username, role));
    }

    /**
     * A free manager session that is none of the named accounts: the discussion scenario's
     * demand for a manager who is not the creator of the task they comment on.
     */
    public synchronized Lease acquireManagerOtherThan(Collection<String> excluded) {
        return anyFitting(State.SIGNED_IN, Role.MANAGER, excluded,
                "no signed-in manager session outside " + excluded + " is free");
    }

    /**
     * Attaches the transport context the session travels with, replacing what was attached
     * before: a sign-in attaches the fresh session's context, and a holder whose steps moved
     * the context on attaches it again before setting the session down.
     */
    public synchronized void attach(Lease lease, TransportContext context) {
        requireHeld(lease);
        contexts.put(lease.username(), context);
    }

    /** The context the leased session travels with. Holding a lease is the way to read it. */
    public synchronized TransportContext contextOf(Lease lease) {
        requireHeld(lease);
        TransportContext context = contexts.get(lease.username());
        if (context == null) {
            throw new IllegalStateException("the session of " + lease.username()
                    + " carries no transport context; a signed-in session without one is a"
                    + " wiring mistake, not a session");
        }
        return context;
    }

    /** Returns the session, signed in and free for the next step, with its context attached. */
    public synchronized void release(Lease lease) {
        requireHeld(lease);
        if (contexts.get(lease.username()) == null) {
            throw new IllegalStateException("the session of " + lease.username()
                    + " cannot be released as signed in without its transport context;"
                    + " a holder that lost the context reports the session signed out");
        }
        states.put(lease.username(), State.SIGNED_IN);
    }

    /** Records the sign-out: the account has no session and no context until it signs in again. */
    public synchronized void signedOut(Lease lease) {
        requireHeld(lease);
        states.put(lease.username(), State.SIGNED_OUT);
        contexts.remove(lease.username());
    }

    private List<Lease> allFitting(State wanted, Role role, Collection<String> excluded) {
        List<Lease> fitting = new ArrayList<>();
        for (Map.Entry<String, Role> account : roles.entrySet()) {
            String username = account.getKey();
            if (account.getValue() != role || states.get(username) != wanted) {
                continue;
            }
            if (excluded != null && excluded.contains(username)) {
                continue;
            }
            fitting.add(new Lease(username, account.getValue()));
        }
        return fitting;
    }

    private Lease anyFitting(State wanted, Role role, Collection<String> excluded,
            String starvedReason) {
        List<Lease> fitting = allFitting(wanted, role, excluded);
        if (fitting.isEmpty()) {
            throw new RegistryStarvedException(starvedReason);
        }
        return held(fitting.get(ThreadLocalRandom.current().nextInt(fitting.size())));
    }

    private Lease held(Lease lease) {
        states.put(lease.username(), State.IN_USE);
        return lease;
    }

    private void requireHeld(Lease lease) {
        if (states.get(lease.username()) != State.IN_USE) {
            throw new IllegalStateException(
                    "the lease on the session of " + lease.username() + " is not held");
        }
    }
}
