package loadrig.model.scenario;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * The run's record of what the application refused, counted by the status it refused with, and
 * the one code that carries two causes split by the only fact that separates them.
 *
 * <p>The task surface answers five refusal codes and the list is closed. Four of them name their
 * cause on their own: a request the surface never produced, a name that matches nothing, an action
 * the current status does not offer, a required field left blank. The fifth is answered both by an
 * action the actor never owns and by a mutating request that carries no valid token, and the SUT
 * states the two as deliberately indistinguishable by status - so a reader who counts refusals by
 * their code alone reads one cause where there are two. Only the injector can split them, because
 * only the injector knows which of its requests held a token; the split is therefore made here and
 * reported beside the log, never written into it, so that the result log stays the plain
 * per-sample export the capture consumes.
 *
 * <p>What this ledger does not see: three screens - the sign-in, the creation of a user and the
 * password change - refuse with a rendered page under a successful status, and the content
 * assertion of each step is what catches those. These counts are the refusals the transport can
 * name, not every refusal a run met.
 */
public final class RefusalLedger {

    /** The code answering a request the application's own screens never produce. */
    public static final int NEVER_PRODUCED_BY_A_SCREEN = 400;

    /** The code an unowned action and a request without a valid token both answer with. */
    public static final int FORBIDDEN = 403;

    /** The code a request naming a task the system no longer holds answers with. */
    public static final int NAMES_NOTHING = 404;

    /** Below this the application answered; at it and above it the application refused. */
    private static final int FIRST_REFUSAL_CODE = 400;

    private final ConcurrentHashMap<Integer, LongAdder> counts = new ConcurrentHashMap<>();
    private final LongAdder forbiddenCarryingAToken = new LongAdder();
    private final LongAdder forbiddenCarryingNone = new LongAdder();
    private final Set<String> vanishedUnderASession = ConcurrentHashMap.newKeySet();

    /**
     * Records the status one request was answered with. An answer below the first refusal code is
     * what the run is made of and is not counted; a status that is not a number at all belongs to
     * a step that made no request - the injector's own bookkeeping - and is nothing to count
     * either.
     */
    public void answered(String status, boolean carriedAToken) {
        if (status == null || status.isBlank()) {
            return;
        }
        int code;
        try {
            code = Integer.parseInt(status.trim());
        } catch (NumberFormatException notAStatus) {
            return;
        }
        if (code < FIRST_REFUSAL_CODE) {
            return;
        }
        counts.computeIfAbsent(code, unused -> new LongAdder()).increment();
        if (code == FORBIDDEN) {
            (carriedAToken ? forbiddenCarryingAToken : forbiddenCarryingNone).increment();
        }
    }

    /** The refusal counts by status code, in the order of the codes; empty after a clean run. */
    public Map<Integer, Long> counts() {
        Map<Integer, Long> byCode = new TreeMap<>();
        counts.forEach((code, count) -> byCode.put(code, count.sum()));
        return byCode;
    }

    /** Of the refusals under {@link #FORBIDDEN}, those the actor asked for holding a token. */
    public long forbiddenCarryingAToken() {
        return forbiddenCarryingAToken.sum();
    }

    /**
     * Of the refusals under {@link #FORBIDDEN}, those asked without one. A burst of these is a
     * session that lost its token, which is a different event from an actor reaching past what
     * they own, and the two are worth different readings of the same window.
     */
    public long forbiddenCarryingNone() {
        return forbiddenCarryingNone.sum();
    }

    /**
     * Records that a step acting on a task the registry handed it found the task gone: another
     * session deleted it in the seconds between the pick and the request. The SUT confirms this
     * as real user behaviour - a stale list row clicked after a delete - and a clean baseline may
     * carry a handful, so the step does not fail the run over it and this is where the handful is
     * counted.
     *
     * <p>The task is recorded, not the answer, and the tasks are held as a set. Several sessions
     * can meet one deletion, and counting their answers would compare a number of events against
     * the number of deletions the profile intends - two different things, one of which would
     * spoil a run for a race the other says is allowed.
     */
    public void aTaskVanishedUnderASession(String taskId) {
        if (taskId != null && !taskId.isBlank()) {
            vanishedUnderASession.add(taskId);
        }
    }

    /**
     * How many distinct tasks were lost under a session. The harness holds this against what the
     * profile intended to delete: a session can only lose a task somebody deleted, so more tasks
     * lost than the profile meant to remove is not a race any more, and the capture says so.
     */
    public long tasksVanishedUnderASession() {
        return vanishedUnderASession.size();
    }

    public long total() {
        return counts.values().stream().mapToLong(LongAdder::sum).sum();
    }
}
