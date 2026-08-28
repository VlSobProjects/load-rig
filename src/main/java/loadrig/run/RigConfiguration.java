package loadrig.run;

import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;
import loadrig.model.Account;
import loadrig.model.Role;

/**
 * What a run is given from outside: the address of the stack, the accounts a virtual user signs in
 * with, the password a provisioned account is created with, where the artifacts land and how large
 * the run is.
 *
 * <p>Every value is a system property with a default, so that a run states its parameters on the
 * command line and nothing about a particular machine is compiled into the rig. The defaults are
 * the ones a stack brought up from nothing answers with: the application publishes its port on the
 * capture host the generator itself runs on, and a fresh database seeds one account per role.
 *
 * <p>The account pool of a full profile is not this class's subject. It needs as many accounts as
 * the profile has virtual users, and creating them is work of its own.
 */
public final class RigConfiguration {

    static final String BASE_URL_PROPERTY = "loadrig.baseUrl";
    static final String RESULTS_DIRECTORY_PROPERTY = "loadrig.results.directory";
    static final String VIRTUAL_USERS_PROPERTY = "loadrig.smoke.virtualUsers";
    static final String ITERATIONS_PROPERTY = "loadrig.smoke.iterations";
    static final String PROVISIONED_PASSWORD_PROPERTY = "loadrig.provisioning.password";

    private static final String DEFAULT_BASE_URL = "http://localhost:8080";
    private static final String DEFAULT_RESULTS_DIRECTORY = "results";
    private static final int DEFAULT_VIRTUAL_USERS = 2;
    private static final int DEFAULT_ITERATIONS = 1;

    /** Long enough for the application's own rule, which refuses anything shorter than eight. */
    private static final String DEFAULT_PROVISIONED_PASSWORD = "loadrig123!";

    private final String baseUrl;
    private final Map<Role, Account> accounts;
    private final String provisionedPassword;
    private final Path resultsDirectory;
    private final int virtualUsers;
    private final int iterations;

    private RigConfiguration(String baseUrl, Map<Role, Account> accounts,
            String provisionedPassword, Path resultsDirectory, int virtualUsers, int iterations) {
        this.baseUrl = baseUrl;
        this.accounts = accounts;
        this.provisionedPassword = provisionedPassword;
        this.resultsDirectory = resultsDirectory;
        this.virtualUsers = virtualUsers;
        this.iterations = iterations;
    }

    public static RigConfiguration fromSystemProperties() {
        Map<Role, Account> accounts = new EnumMap<>(Role.class);
        accounts.put(Role.WORKER, account(Role.WORKER, "worker", "worker123!"));
        accounts.put(Role.MANAGER, account(Role.MANAGER, "manager", "manager123!"));
        accounts.put(Role.ADMINISTRATOR, account(Role.ADMINISTRATOR, "admin", "admin123!"));
        return new RigConfiguration(
                property(BASE_URL_PROPERTY, DEFAULT_BASE_URL),
                accounts,
                property(PROVISIONED_PASSWORD_PROPERTY, DEFAULT_PROVISIONED_PASSWORD),
                Path.of(property(RESULTS_DIRECTORY_PROPERTY, DEFAULT_RESULTS_DIRECTORY)),
                number(VIRTUAL_USERS_PROPERTY, DEFAULT_VIRTUAL_USERS),
                number(ITERATIONS_PROPERTY, DEFAULT_ITERATIONS));
    }

    public String baseUrl() {
        return baseUrl;
    }

    public Map<Role, Account> accounts() {
        return Map.copyOf(accounts);
    }

    public String provisionedPassword() {
        return provisionedPassword;
    }

    public Path resultsDirectory() {
        return resultsDirectory;
    }

    public int virtualUsers() {
        return virtualUsers;
    }

    public int iterations() {
        return iterations;
    }

    static String accountUsernameProperty(Role role) {
        return "loadrig.account." + role.name().toLowerCase() + ".username";
    }

    static String accountPasswordProperty(Role role) {
        return "loadrig.account." + role.name().toLowerCase() + ".password";
    }

    private static Account account(Role role, String defaultUsername, String defaultPassword) {
        return new Account(
                property(accountUsernameProperty(role), defaultUsername),
                property(accountPasswordProperty(role), defaultPassword));
    }

    private static String property(String name, String defaultValue) {
        String value = System.getProperty(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private static int number(String name, int defaultValue) {
        String value = System.getProperty(name);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    name + " must be a whole number, and it is " + value, e);
        }
    }
}
