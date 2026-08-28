package loadrig.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/**
 * Holds the expressions the walk reads identities with against a captured answer of the
 * application. The identity of a task decides which task every step after a creation acts on, so
 * it is proven here rather than in a run: a run that reads the wrong row still passes.
 *
 * <p>The expressions use only constructs the injector's own regular expression engine and this
 * test's engine both accept.
 */
class TaskIdentityTest {

    private static final String TASK_TABLE_FRAGMENT = "task-table-fragment.html";

    @Test
    void theIdentityOfATaskIsReadFromTheRowCarryingTheWalkTitle() throws IOException {
        String answer = capturedAnswer();

        assertEquals("41", identityOf("lr1-1-0", answer));
        assertEquals("42", identityOf("lr1-2-0", answer));
    }

    @Test
    void aTitleThatIsNotInTheAnswerYieldsNoIdentity() throws IOException {
        Matcher matcher = Pattern.compile(TransportWalk.taskIdentityRegex("lr1-9-9"))
                .matcher(capturedAnswer());

        assertFalse(matcher.find(),
                "a walk whose task is missing from the answer must find no identity at all, "
                        + "so that the run states the refusal instead of acting on a stranger's task");
    }

    @Test
    void theIdentityOfAnAccountIsReadFromTheOptionsTheApplicationOffers() {
        String options = "<select id=\"assigneeId\" name=\"assigneeId\">"
                + "<option value=\"2\">manager</option>"
                + "<option value=\"3\">worker</option>"
                + "</select>";

        Matcher matcher = Pattern.compile(TransportWalk.userIdRegex("worker")).matcher(options);

        assertTrue(matcher.find());
        assertEquals("3", matcher.group(1));
    }

    @Test
    void theIdentityOfTheProvisionedAccountIsReadFromTheRowItAppearsIn() {
        String users = "<tr id=\"user-4\"><td>lr1u20260828093000t0i1</td>"
                + "<td><form action=\"/users/4/delete\" hx-post=\"/users/4/delete\">"
                + "</form></td></tr>"
                + "<tr id=\"user-5\"><td>lr1u20260828093000t1i1</td>"
                + "<td><form action=\"/users/5/delete\" hx-post=\"/users/5/delete\">"
                + "</form></td></tr>";

        Matcher matcher = Pattern
                .compile(TransportWalk.accountIdentityRegex("lr1u20260828093000t1i1"))
                .matcher(users);

        assertTrue(matcher.find());
        assertEquals("5", matcher.group(1));
    }

    private String identityOf(String walkTag, String answer) {
        Matcher matcher = Pattern.compile(TransportWalk.taskIdentityRegex(walkTag)).matcher(answer);
        assertTrue(matcher.find(), "the answer carries no row for " + walkTag);
        return matcher.group(1);
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
