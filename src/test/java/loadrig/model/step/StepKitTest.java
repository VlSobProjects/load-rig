package loadrig.model.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import loadrig.model.Correlation;
import loadrig.model.profile.LoadProfile;
import org.junit.jupiter.api.Test;

/**
 * Holds the kit's half of DR-3: what a step owes is taken by the signatures, and what a step
 * must not be is refused. Whether the built requests drive the application is the conformance
 * walk's question, not this test's.
 */
class StepKitTest {

    private final StepKit kit = new StepKit("http://an.address.of.a.stack:8080",
            new LoadProfile.ThinkTime(3, 7));

    @Test
    void aStepCarriesItsKindWeightAndRequests() {
        Step step = kit.step(7, StepKind.LOOKING_AT_A_LIST,
                kit.fragmentRequest("someone looks at a list", "/tasks",
                        ContentExpectation.rendersNoRefusalMark()));

        assertEquals(7, step.weight());
        assertEquals(StepKind.LOOKING_AT_A_LIST, step.kind());
        assertEquals(1, step.requests().size());
        assertTrue(step.gate().isEmpty(), "an ungated step states so");
        assertNotNull(step.scheduled(50f));
    }

    @Test
    void aGatedStepStatesItsGate() {
        Step step = kit.step(3, StepKind.OPENING_ONE_TASK, vars -> true,
                kit.fragmentRequest("someone opens a task", "/tasks/1",
                        ContentExpectation.rendersNoRefusalMark(),
                        Correlation.sessionToken("csrfToken")));

        assertTrue(step.gate().isPresent());
        assertNotNull(step.scheduled(30f));
    }

    @Test
    void theKitRemembersWhichActEachLabelBelongsTo() {
        kit.step(4, StepKind.RUNNING_A_REPORT,
                kit.pageRequest("someone runs the report", "/reports",
                        ContentExpectation.rendersNoRefusalMark()));
        var signsIn = kit.pageRequest("someone signs in", "/login",
                ContentExpectation.renders("an authenticated screen", "action=\"/logout\""));
        kit.declareKind(StepKind.SESSION_KEEPING, signsIn);

        assertEquals(StepKind.RUNNING_A_REPORT,
                kit.stepKindsByLabel().get("someone runs the report"));
        assertEquals(StepKind.SESSION_KEEPING, kit.stepKindsByLabel().get("someone signs in"));
    }

    @Test
    void oneLabelStandingForTwoActsIsRefused() {
        var request = kit.pageRequest("someone does a thing", "/tasks",
                ContentExpectation.rendersNoRefusalMark());
        kit.declareKind(StepKind.LOOKING_AT_A_LIST, request);

        assertThrows(IllegalArgumentException.class,
                () -> kit.declareKind(StepKind.RUNNING_A_REPORT, request));
    }

    @Test
    void aWeightlessStepAndAStepWithoutARequestAreRefused() {
        var request = kit.pageRequest("someone signs in", "/login",
                ContentExpectation.renders("an authenticated screen", "action=\"/logout\""));

        assertThrows(IllegalArgumentException.class,
                () -> kit.step(0, StepKind.SESSION_KEEPING, request));
        assertThrows(IllegalArgumentException.class,
                () -> kit.step(1, StepKind.SESSION_KEEPING));
    }
}
