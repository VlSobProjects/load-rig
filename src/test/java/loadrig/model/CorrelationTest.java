package loadrig.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Holds every rule of the correlation dictionary against a captured answer of the application.
 * The identities the rules read decide which row every later step acts on, so they are proven
 * here rather than in a run: a run that reads the wrong row still passes.
 *
 * <p>The expressions use only constructs the injector's own regular expression engine and this
 * test's engine both accept; {@code Rule.read} is the same expression both apply.
 */
class CorrelationTest {

    private static final String TASK_TABLE_FRAGMENT = "task-table-fragment.html";

    @Test
    void theIdentityOfATaskIsReadFromTheRowCarryingTheAnchoredTitle() throws IOException {
        String answer = capturedAnswer();

        assertEquals(Optional.of("41"),
                Correlation.taskIdentity("taskId", "lr1-1-0").read(answer));
        assertEquals(Optional.of("42"),
                Correlation.taskIdentity("taskId", "lr1-2-0").read(answer));
    }

    @Test
    void aTitleThatIsNotInTheAnswerYieldsNoIdentity() throws IOException {
        assertEquals(Optional.empty(),
                Correlation.taskIdentity("taskId", "lr1-9-9").read(capturedAnswer()),
                "a step whose task is missing from the answer must find no identity at all, "
                        + "so that the run states the refusal instead of acting on a stranger's task");
    }

    @Test
    void theIdentityOfAnAccountIsReadFromTheOptionsTheApplicationOffers() {
        String options = "<select id=\"assigneeId\" name=\"assigneeId\">"
                + "<option value=\"2\">manager</option>"
                + "<option value=\"3\">worker</option>"
                + "</select>";

        assertEquals(Optional.of("3"),
                Correlation.userIdentity("userId", "worker").read(options));
        assertEquals(Optional.of("2"),
                Correlation.userIdentity("userId", "manager").read(options));
    }

    @Test
    void theIdentityOfTheProvisionedAccountIsReadFromTheRowItAppearsIn() {
        String users = "<tr id=\"user-4\"><td>lr1u20260828093000t0i1</td>"
                + "<td><form action=\"/users/4/delete\" hx-post=\"/users/4/delete\">"
                + "</form></td></tr>"
                + "<tr id=\"user-5\"><td>lr1u20260828093000t1i1</td>"
                + "<td><form action=\"/users/5/delete\" hx-post=\"/users/5/delete\">"
                + "</form></td></tr>";

        assertEquals(Optional.of("5"),
                Correlation.accountIdentity("accountId", "lr1u20260828093000t1i1").read(users));
    }

    @Test
    void theTokenIsReadFromTheHiddenFieldTheFormsRender() {
        String page = "<form action=\"/login\" method=\"post\">"
                + "<input type=\"hidden\" name=\"_csrf\" value=\"a-token-of-the-session\"/>"
                + "</form>";

        assertEquals(Optional.of("a-token-of-the-session"),
                Correlation.sessionToken("csrfToken").read(page));
    }

    @Test
    void aRuleAppliesAsAnExtractorWithTheLoudFailureMarker() {
        assertTrue(Correlation.taskIdentity("taskId", "an-anchor").extractor() != null);
        assertEquals("EXTRACTION-FAILED", Correlation.EXTRACTION_FAILED,
                "the marker is a contract: a request built from it is refused, never quietly sent");
    }

    private String capturedAnswer() throws IOException {
        try (InputStream captured = getClass().getResourceAsStream(TASK_TABLE_FRAGMENT)) {
            if (captured == null) {
                throw new IOException("the captured answer " + TASK_TABLE_FRAGMENT + " is missing");
            }
            return new String(captured.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
