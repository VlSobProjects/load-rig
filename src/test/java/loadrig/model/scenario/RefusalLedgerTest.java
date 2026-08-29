package loadrig.model.scenario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The ledger that lets a reader of a capture tell the two causes of one refusal code apart, and
 * that counts nothing the application did not refuse.
 */
class RefusalLedgerTest {

    private static final boolean HOLDING_A_TOKEN = true;
    private static final boolean HOLDING_NONE = false;

    @Test
    @DisplayName("what the application answered is not counted as a refusal")
    void countsOnlyRefusals() {
        RefusalLedger ledger = new RefusalLedger();
        ledger.answered("200", HOLDING_A_TOKEN);
        ledger.answered("302", HOLDING_A_TOKEN);
        ledger.answered("399", HOLDING_A_TOKEN);
        assertEquals(0, ledger.total());
        assertTrue(ledger.counts().isEmpty());
    }

    @Test
    @DisplayName("a step that made no request carries no status, and is nothing to count")
    void ignoresWhatIsNotAStatus() {
        RefusalLedger ledger = new RefusalLedger();
        ledger.answered(null, HOLDING_A_TOKEN);
        ledger.answered("", HOLDING_A_TOKEN);
        ledger.answered("   ", HOLDING_A_TOKEN);
        ledger.answered("Non HTTP response code: java.net.SocketException", HOLDING_A_TOKEN);
        assertEquals(0, ledger.total());
    }

    @Test
    @DisplayName("the refusals are counted by their code, in the order of the codes")
    void countsByCode() {
        RefusalLedger ledger = new RefusalLedger();
        ledger.answered("409", HOLDING_A_TOKEN);
        ledger.answered("404", HOLDING_A_TOKEN);
        ledger.answered("409", HOLDING_A_TOKEN);
        ledger.answered("422", HOLDING_A_TOKEN);
        Map<Integer, Long> counts = ledger.counts();
        assertEquals(List.of(404, 409, 422), List.copyOf(counts.keySet()));
        assertEquals(1L, counts.get(404));
        assertEquals(2L, counts.get(409));
        assertEquals(4, ledger.total());
    }

    @Test
    @DisplayName("the one code with two causes is split by the token the request carried")
    void splitsTheForbiddenByTheToken() {
        RefusalLedger ledger = new RefusalLedger();
        ledger.answered("403", HOLDING_A_TOKEN);
        ledger.answered("403", HOLDING_NONE);
        ledger.answered("403", HOLDING_NONE);
        ledger.answered("409", HOLDING_NONE);
        assertEquals(3L, ledger.counts().get(RefusalLedger.FORBIDDEN));
        assertEquals(1, ledger.forbiddenCarryingAToken());
        assertEquals(2, ledger.forbiddenCarryingNone());
    }

    @Test
    @DisplayName("no other code is split: only the forbidden one carries two causes")
    void splitsNothingElse() {
        RefusalLedger ledger = new RefusalLedger();
        ledger.answered("422", HOLDING_NONE);
        assertEquals(0, ledger.forbiddenCarryingAToken());
        assertEquals(0, ledger.forbiddenCarryingNone());
    }
}
