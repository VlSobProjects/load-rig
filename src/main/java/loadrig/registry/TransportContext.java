package loadrig.registry;

import java.util.List;
import loadrig.model.Correlation;

/**
 * The transport identity of one SUT session: the cookies of the browser that holds it and the
 * token of the last page it loaded. A session outlives the iteration and passes between threads
 * (LR-3 breaks the one-thread-one-session equality of the transport walk), so this context
 * travels with the session - attached to the session registry's entry at sign-in, handed to
 * whoever leases the session, dropped at sign-out - instead of living in any thread's own
 * state. Keeping it beside the lease makes the sign-out and the dropped context one act, not
 * two structures kept consistent by discipline.
 *
 * <p>The record is transport-shaped but deliberately free of injector types, so the registry
 * stays plain Java a unit test drives without a stack.
 */
public record TransportContext(List<CookieFact> cookies, String pageToken) {

    /** One cookie as the injector's cookie store holds it, round-tripped faithfully. */
    public record CookieFact(String name, String value, String domain, String path,
            boolean secure, long expires) {
    }

    public TransportContext {
        cookies = List.copyOf(cookies);
    }

    /** The context of a session that does not exist yet: no cookies, no token. */
    public static TransportContext fresh() {
        return new TransportContext(List.of(), "");
    }

    /**
     * Whether this context can drive an authenticated step. A context without a token is not a
     * session: the sign-in that should have produced it failed, and the holder reports the
     * session signed out instead of handing the failure to the next step.
     */
    public boolean carriesAToken() {
        return isAToken(pageToken);
    }

    /**
     * Whether a scraped value is a token at all: present, not blank, and not the mark the
     * correlation dictionary leaves where an extraction found nothing. Stated once and read from
     * both sides, because the run's ledger asks the same question of a thread's own variable as
     * the session registry asks of a context, and two spellings of one rule drift.
     */
    public static boolean isAToken(String scraped) {
        return scraped != null && !scraped.isBlank()
                && !Correlation.EXTRACTION_FAILED.equals(scraped);
    }
}
