package loadrig.model;

import java.util.Objects;

/**
 * An account of the system under test, as a virtual user signs in with it.
 *
 * <p>The password is held in memory only: the rig receives it from its configuration and never
 * writes it into a repository artifact.
 */
public record Account(String username, String password) {

    public Account {
        Objects.requireNonNull(username, "username");
        Objects.requireNonNull(password, "password");
        if (username.isBlank()) {
            throw new IllegalArgumentException("an account with a blank username cannot sign in");
        }
    }
}
