package loadrig.run;

import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
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
 * <p>The composition of the account pool - how many accounts, how many of them managers - is the
 * load model's subject and lives in {@code loadrig.model.AccountPool}; this class carries only the
 * password the pool is provisioned with and signs in under.
 */
public final class RigConfiguration {

    static final String BASE_URL_PROPERTY = "loadrig.baseUrl";
    static final String RESULTS_DIRECTORY_PROPERTY = "loadrig.results.directory";
    static final String CAMPAIGN_PROPERTY = "loadrig.campaign";
    static final String VIRTUAL_USERS_PROPERTY = "loadrig.smoke.virtualUsers";
    static final String ITERATIONS_PROPERTY = "loadrig.smoke.iterations";
    static final String PROVISIONED_PASSWORD_PROPERTY = "loadrig.provisioning.password";
    static final String SUT_VERSION_PROPERTY = "loadrig.sut.version";

    private static final String DEFAULT_BASE_URL = "http://localhost:8080";
    private static final String DEFAULT_RESULTS_DIRECTORY = "results";
    private static final String DEFAULT_CAMPAIGN = "profiles/campaign.json";
    private static final int DEFAULT_VIRTUAL_USERS = 2;
    private static final int DEFAULT_ITERATIONS = 1;

    /** Long enough for the application's own rule, which refuses anything shorter than eight. */
    private static final String DEFAULT_PROVISIONED_PASSWORD = "loadrig123!";

    private final String baseUrl;
    private final Map<Role, Account> accounts;
    private final String provisionedPassword;
    private final String sutVersion;
    private final Path campaignFile;
    private final Path resultsDirectory;
    private final int virtualUsers;
    private final int iterations;

    private RigConfiguration(String baseUrl, Map<Role, Account> accounts,
            String provisionedPassword, String sutVersion, Path campaignFile,
            Path resultsDirectory, int virtualUsers, int iterations) {
        this.baseUrl = baseUrl;
        this.accounts = accounts;
        this.provisionedPassword = provisionedPassword;
        this.sutVersion = sutVersion;
        this.campaignFile = campaignFile;
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
                System.getProperty(SUT_VERSION_PROPERTY),
                Path.of(property(CAMPAIGN_PROPERTY, DEFAULT_CAMPAIGN)),
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

    /**
     * The version of the SUT under test as the operator stated it, empty when they stated none.
     * The stand publishes its own version on an information endpoint and {@link SutVersion} asks
     * for it, so a stated version is an override and not the only source: an operator who runs a
     * build the stand cannot name - a patched image, a local branch - still says so, and everybody
     * else says nothing and gets the stand's answer.
     */
    public Optional<String> statedSutVersion() {
        return Optional.ofNullable(sutVersion).filter(stated -> !stated.isBlank());
    }

    /**
     * The file this campaign is read from: the service levels its captures are judged by and the
     * depth its seats rotate through. One file serves every profile the campaign runs, because
     * what it holds is fixed before the campaign and held across the baseline and the faulted run
     * alike; a run states another file only when it is another campaign.
     */
    public Path campaignFile() {
        return campaignFile;
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
