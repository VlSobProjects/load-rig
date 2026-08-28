package loadrig.model;

/**
 * The three roles of the system under test. The token is the value the application's own forms
 * carry, so that a rig step that creates an account states the role the same way the interface
 * does.
 */
public enum Role {

    WORKER("WORKER"),
    MANAGER("MANAGER"),
    ADMINISTRATOR("ADMIN");

    private final String token;

    Role(String token) {
        this.token = token;
    }

    public String token() {
        return token;
    }
}
