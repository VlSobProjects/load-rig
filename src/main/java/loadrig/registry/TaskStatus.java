package loadrig.registry;

import loadrig.model.SutSurface;

/**
 * The statuses a task of the system under test moves through, as the rig mirrors them.
 *
 * <p>The scenario specification names {@code OPEN} and the two settled statuses, {@code APPROVED}
 * and {@code ACKNOWLEDGED}, outright; the three between them are this project's own names for
 * the states the scenarios describe - finished work awaiting approval, a question awaiting its
 * answer, a refusal awaiting acknowledgement. The rig never reads the SUT's database, so the
 * registry mirrors the behaviour of the transition table, never the spelling of a schema.
 */
public enum TaskStatus {

    OPEN,
    COMPLETED,
    RETURNED,
    REFUSED,
    APPROVED,
    ACKNOWLEDGED;

    /**
     * The state behind the mark a list row shows. The surface owns the spelling and this enum owns
     * the state, so the two carry the same names and a mark that gains a state - or loses one -
     * fails here loudly instead of arriving as a registered task in a state nobody defined.
     */
    public static TaskStatus behind(SutSurface.StatusMark mark) {
        return valueOf(mark.name());
    }
}
