package loadrig.model.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import loadrig.model.profile.ServiceLevelBand;
import org.junit.jupiter.api.Test;

/**
 * Holds the mapping a run's verdict rests on: every kind of act the rig performs is judged in a
 * band, the three kinds that change something are judged as one act with its answer, and the
 * sign-ins are the only ones judged in the looser band the specification gives password hashing.
 */
class StepKindTest {

    @Test
    void everyKindOfActIsJudgedInABand() {
        for (StepKind kind : StepKind.values()) {
            assertNotNull(kind.band(), kind + " is judged somewhere");
        }
    }

    @Test
    void theActsThatChangeSomethingShareOneBand() {
        assertEquals(ServiceLevelBand.PERFORMING_AN_ACTION, StepKind.MOVING_A_TASK.band());
        assertEquals(ServiceLevelBand.PERFORMING_AN_ACTION, StepKind.CREATING_A_TASK.band());
        assertEquals(ServiceLevelBand.PERFORMING_AN_ACTION, StepKind.DELETING_A_TASK.band());
    }

    @Test
    void onlyTheRigsOwnSessionKeepingIsJudgedAsASignIn() {
        for (StepKind kind : StepKind.values()) {
            if (kind != StepKind.SESSION_KEEPING) {
                assertNotEquals(ServiceLevelBand.SIGNING_IN, kind.band(),
                        kind + " is a row of the mix and is not a sign-in");
            }
        }
        assertEquals(ServiceLevelBand.SIGNING_IN, StepKind.SESSION_KEEPING.band());
    }
}
