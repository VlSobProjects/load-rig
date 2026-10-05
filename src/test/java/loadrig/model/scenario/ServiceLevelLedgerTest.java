package loadrig.model.scenario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumMap;
import java.util.Map;
import loadrig.model.profile.ServiceLevelBand;
import loadrig.model.profile.ServiceLevels;
import loadrig.model.step.StepKind;
import org.junit.jupiter.api.Test;

/**
 * Holds the measurement the campaign's criterion rests on: the percentile is the one the report
 * names, a sample reaches the band of the act that produced it, and a breach is a finding the
 * ledger states rather than a decision it takes.
 */
class ServiceLevelLedgerTest {

    private static final String A_LIST = "the worker looks at their list";
    private static final String A_REPORT = "the manager runs the report";
    private static final String NOBODY_DECLARED = "a step the plan cannot name";

    private final ServiceLevels levels = new ServiceLevels(
            figures(1000, 1000, 1000, 1000, 2000, 1000, 3000), 10000);

    @Test
    void thePercentileIsTheValueAtTheNearestRank() {
        long[] hundred = new long[100];
        for (int i = 0; i < hundred.length; i++) {
            hundred[i] = i + 1;
        }

        assertEquals(95, ServiceLevelLedger.percentile(hundred, 95));
        assertEquals(1, ServiceLevelLedger.percentile(new long[] {1}, 95),
                "one sample is its own percentile");
        assertEquals(0, ServiceLevelLedger.percentile(new long[0], 95),
                "a band nothing landed in has no percentile to state");
    }

    @Test
    void aSampleIsJudgedInTheBandOfTheActThatProducedIt() {
        ServiceLevelLedger ledger = ledgerOverTheDayLabels();
        ledger.took(A_LIST, 200);
        ledger.took(A_LIST, 400);
        ledger.took(A_REPORT, 2500);

        Map<ServiceLevelBand, ServiceLevelLedger.BandResult> bands = bandsOf(ledger);

        assertEquals(2, bands.get(ServiceLevelBand.OPENING_A_LIST).samples());
        assertEquals(400, bands.get(ServiceLevelBand.OPENING_A_LIST).percentileMillis());
        assertTrue(bands.get(ServiceLevelBand.OPENING_A_LIST).exercised());
        assertFalse(bands.get(ServiceLevelBand.OPENING_A_LIST).breached());
        assertEquals(1, bands.get(ServiceLevelBand.RUNNING_A_REPORT).samples());
        assertFalse(bands.get(ServiceLevelBand.SIGNING_IN).exercised(),
                "a band nobody exercised is neither met nor breached");
    }

    @Test
    void aBreachIsStatedAndNothingElseHappens() {
        ServiceLevelLedger ledger = ledgerOverTheDayLabels();
        for (int i = 0; i < 20; i++) {
            ledger.took(A_LIST, 1500);
        }

        ServiceLevelLedger.Verdict verdict = ledger.verdict(levels);

        assertTrue(verdict.breached());
        assertTrue(bandsOf(ledger).get(ServiceLevelBand.OPENING_A_LIST).breached());
        assertEquals(0, verdict.samplesOverTheCeiling(),
                "a breached level is not a breached ceiling");
        assertEquals(1500, verdict.worstMillis());
    }

    @Test
    void aSampleNoBandClaimsIsCountedApartAndStillHeldToTheCeiling() {
        ServiceLevelLedger ledger = ledgerOverTheDayLabels();
        ledger.took(NOBODY_DECLARED, 12000);
        ledger.took(A_LIST, 300);

        ServiceLevelLedger.Verdict verdict = ledger.verdict(levels);

        ServiceLevelLedger.BandResult list = bandsOf(ledger).get(ServiceLevelBand.OPENING_A_LIST);

        assertEquals(1, verdict.samplesOutsideTheBands());
        assertEquals(12000, verdict.worstMillis(),
                "the worst wait of a run is the worst of every sample, band or none");
        assertEquals(1, list.samples(), "the unnamed sample landed in no band");
        assertEquals(300, list.worstMillis());
        assertEquals(1, verdict.samplesOverTheCeiling(),
                "a sample no band claims is still held to the ceiling");
    }

    @Test
    void aSamplePastTheCeilingIsCountedWhicheverBandItIsIn() {
        ServiceLevelLedger ledger = ledgerOverTheDayLabels();
        ledger.took(A_REPORT, 11000);

        ServiceLevelLedger.Verdict verdict = ledger.verdict(levels);

        assertEquals(1, verdict.samplesOverTheCeiling());
        assertTrue(verdict.breached());
    }

    private static ServiceLevelLedger ledgerOverTheDayLabels() {
        ServiceLevelLedger ledger = new ServiceLevelLedger();
        ledger.judgeByTheKindsOf(Map.of(
                A_LIST, StepKind.LOOKING_AT_A_LIST,
                A_REPORT, StepKind.RUNNING_A_REPORT));
        return ledger;
    }

    private Map<ServiceLevelBand, ServiceLevelLedger.BandResult> bandsOf(
            ServiceLevelLedger ledger) {
        Map<ServiceLevelBand, ServiceLevelLedger.BandResult> byBand =
                new EnumMap<>(ServiceLevelBand.class);
        ledger.verdict(levels).bands().forEach(band -> byBand.put(band.band(), band));
        return byBand;
    }

    private static Map<ServiceLevelBand, Integer> figures(int list, int task, int action,
            int note, int signIn, int signOut, int report) {
        Map<ServiceLevelBand, Integer> figures = new EnumMap<>(ServiceLevelBand.class);
        figures.put(ServiceLevelBand.OPENING_A_LIST, list);
        figures.put(ServiceLevelBand.OPENING_ONE_TASK, task);
        figures.put(ServiceLevelBand.PERFORMING_AN_ACTION, action);
        figures.put(ServiceLevelBand.WRITING_A_NOTE, note);
        figures.put(ServiceLevelBand.SIGNING_IN, signIn);
        figures.put(ServiceLevelBand.SIGNING_OUT, signOut);
        figures.put(ServiceLevelBand.RUNNING_A_REPORT, report);
        return figures;
    }
}
