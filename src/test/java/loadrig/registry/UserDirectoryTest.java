package loadrig.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import loadrig.model.Correlation;
import org.junit.jupiter.api.Test;

/**
 * Holds the user directory's obligation: identities come only from answers of the application,
 * and a step that must name an account by id finds here exactly what some answer has offered.
 */
class UserDirectoryTest {

    private final UserDirectory directory = new UserDirectory();

    @Test
    void anIdentityHarvestedFromAnAnswerIsFoundByName() {
        String landing = "<select name=\"assigneeId\">"
                + "<option value=\"7\">worker-01</option>"
                + "<option value=\"9\">worker-02</option>"
                + "</select>";
        for (String username : List.of("worker-01", "worker-02", "worker-03")) {
            Correlation.userIdentity("directory", username).read(landing)
                    .ifPresent(id -> directory.learned(username, id));
        }

        assertEquals(Optional.of("7"), directory.idOf("worker-01"));
        assertEquals(Optional.of("9"), directory.idOf("worker-02"));
        assertEquals(Optional.empty(), directory.idOf("worker-03"),
                "an account no answer has offered has no identity to act with");
    }

    @Test
    void theKnownAccountsAreTheOnesAStepMayDrawFrom() {
        directory.learned("worker-02", "9");

        assertEquals(List.of("worker-02"),
                directory.knownAmong(List.of("worker-01", "worker-02", "worker-03")));
    }
}
