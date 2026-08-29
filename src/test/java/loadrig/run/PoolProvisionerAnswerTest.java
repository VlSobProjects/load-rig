package loadrig.run;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import loadrig.run.PoolProvisioner.SignInAnswer;
import org.junit.jupiter.api.Test;

/**
 * Holds the reading of the pages the provisioning acts on, without a running stack. The
 * provisioning decides what to do with an account from the answer to its sign-in, so a misread
 * page would create duplicates or take a held account for a usable one.
 */
class PoolProvisionerAnswerTest {

    @Test
    void aScreenWithTheLogoutFormIsASignedInScreen() {
        String screen = "<nav><form action=\"/logout\" method=\"post\">"
                + "<input type=\"hidden\" name=\"_csrf\" value=\"t-1\"/></form></nav>";

        assertEquals(SignInAnswer.SIGNED_IN, PoolProvisioner.classifySignIn(screen));
    }

    /**
     * The held screen is served to a session that is already signed in, so it may well carry the
     * logout form too; the password-change form has to win.
     */
    @Test
    void theScreenAFreshAccountIsHeldAtWinsOverItsOwnLogoutForm() {
        String screen = "<form action=\"/logout\" method=\"post\"></form>"
                + "<form action=\"/change-password\" method=\"post\">"
                + "<input type=\"hidden\" name=\"_csrf\" value=\"t-2\"/></form>";

        assertEquals(SignInAnswer.HELD_AT_CHANGE, PoolProvisioner.classifySignIn(screen));
    }

    @Test
    void aRefusedSignInIsTheLoginPageAgainWithTheRefusalMark() {
        String screen = "<div class=\"alert alert-danger\">Invalid credentials</div>"
                + "<form action=\"/login\" method=\"post\"></form>";

        assertEquals(SignInAnswer.REFUSED, PoolProvisioner.classifySignIn(screen));
    }

    @Test
    void aPageWithNoKnownMarkIsRefusedRatherThanGuessedAt() {
        assertEquals(SignInAnswer.UNKNOWN,
                PoolProvisioner.classifySignIn("<html><body>maintenance</body></html>"));
    }

    @Test
    void theSessionTokenIsReadFromTheHiddenField() {
        String page = "<input type=\"hidden\" name=\"_csrf\" value=\"3f2-abc\"/>";

        assertEquals("3f2-abc", PoolProvisioner.csrfToken(page));
    }

    @Test
    void aPageWithoutTheTokenYieldsNone() {
        assertNull(PoolProvisioner.csrfToken("<html><body>no form here</body></html>"),
                "a missing token must surface as a refusal, not as an empty parameter");
    }
}
