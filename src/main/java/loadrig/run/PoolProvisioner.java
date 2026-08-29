package loadrig.run;

import java.io.IOException;
import java.net.CookieManager;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import loadrig.model.Account;
import loadrig.model.AccountPool;
import loadrig.model.Role;
import loadrig.model.SutSurface;

/**
 * Brings the account pool of a running stack to the configured state, through the same interface a
 * person uses: the administrator screens LR-1 proved.
 *
 * <p>Provisioning is stand administration, not load. It runs on the JDK's own HTTP client, leaves
 * no result log and sends no answer-shape header, because none of its requests belongs to a
 * capture. What it shares with the load model is the surface: every address, form field and page
 * mark comes from {@link SutSurface}, so a change in the interface stays a change in one file.
 *
 * <p>Presence is proven, not looked up: every member of the pool is made to sign in with the
 * configured password, which keeps the provisioning independent of how the users screen lists or
 * pages its rows. A member that signs in is present. A member held at the forced password change -
 * the leftover of an interrupted provisioning - completes it. A member the application refuses is
 * created by the built-in administrator and then proven the same way. An account that exists under
 * another password surfaces as a refused creation and stops the run loudly, because a pool the rig
 * cannot sign into is worse than no pool.
 */
public final class PoolProvisioner {

    /** How long one request may take before the provisioning fails instead of hanging. */
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

    private static final String CSRF_ANCHOR = "name=\"" + SutSurface.CSRF_FIELD + "\" value=\"";

    private final String baseUrl;
    private final Account administrator;
    private final String poolPassword;
    private Session administratorSession;

    PoolProvisioner(String baseUrl, Account administrator, String poolPassword) {
        this.baseUrl = baseUrl;
        this.administrator = administrator;
        this.poolPassword = poolPassword;
    }

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.ENGLISH);
        RigConfiguration configuration = RigConfiguration.fromSystemProperties();
        new PoolProvisioner(
                configuration.baseUrl(),
                configuration.accounts().get(Role.ADMINISTRATOR),
                configuration.provisionedPassword())
                .provision();
    }

    void provision() throws IOException, InterruptedException {
        System.out.println("bringing " + baseUrl + " to the configured pool of "
                + AccountPool.size() + " accounts");
        int present = 0;
        int completed = 0;
        int created = 0;
        for (AccountPool.Member member : AccountPool.members()) {
            switch (ensureUsable(member)) {
                case PRESENT -> present++;
                case COMPLETED_CHANGE -> completed++;
                case CREATED -> created++;
            }
        }
        System.out.println(created + " account(s) created, "
                + completed + " left held at the password change and completed, "
                + present + " already present; every member of the pool has proven its sign-in");
    }

    private enum Outcome {
        PRESENT, COMPLETED_CHANGE, CREATED
    }

    private Outcome ensureUsable(AccountPool.Member member)
            throws IOException, InterruptedException {
        Session session = new Session();
        String answer = signIn(session, member.username(), poolPassword);
        switch (classifySignIn(answer)) {
            case SIGNED_IN -> {
                signOut(session, answer, member.username());
                return Outcome.PRESENT;
            }
            case HELD_AT_CHANGE -> {
                completeChange(session, answer, member.username());
                proveSignIn(member);
                return Outcome.COMPLETED_CHANGE;
            }
            case REFUSED -> {
                create(member);
                Session fresh = new Session();
                String firstAnswer = signIn(fresh, member.username(), poolPassword);
                if (classifySignIn(firstAnswer) != SignInAnswer.HELD_AT_CHANGE) {
                    throw refusal("the fresh account " + member.username()
                            + " was not held at the password change after its creation");
                }
                completeChange(fresh, firstAnswer, member.username());
                proveSignIn(member);
                return Outcome.CREATED;
            }
            default -> throw refusal("the sign-in of " + member.username()
                    + " was answered with a page this provisioning does not know");
        }
    }

    /** The definitive proof: a whole session with the configured password, opened and closed. */
    private void proveSignIn(AccountPool.Member member) throws IOException, InterruptedException {
        Session session = new Session();
        String answer = signIn(session, member.username(), poolPassword);
        if (classifySignIn(answer) != SignInAnswer.SIGNED_IN) {
            throw refusal("the account " + member.username()
                    + " did not sign in with the configured password after its provisioning");
        }
        signOut(session, answer, member.username());
    }

    private void create(AccountPool.Member member) throws IOException, InterruptedException {
        Session admin = administratorSession();
        String usersPage = admin.get(SutSurface.USERS);
        String answer = admin.post(SutSurface.USERS, form(
                SutSurface.USERNAME_FIELD, member.username(),
                SutSurface.PASSWORD_FIELD, poolPassword,
                SutSurface.ROLE_FIELD, member.role().token(),
                SutSurface.CSRF_FIELD, csrfOf(usersPage, "the users screen")));
        if (answer.contains(SutSurface.REFUSED_MARK)
                || !answer.contains(SutSurface.ACCEPTED_MARK)) {
            throw refusal("the creation of " + member.username() + " was refused; an account of"
                    + " that name under another password is the likely cause, and it has to be"
                    + " resolved by hand rather than walked past");
        }
        System.out.println("created " + member.username()
                + " as " + member.role().name().toLowerCase(Locale.ENGLISH));
    }

    private Session administratorSession() throws IOException, InterruptedException {
        if (administratorSession == null) {
            Session session = new Session();
            String answer = signIn(session, administrator.username(), administrator.password());
            if (classifySignIn(answer) != SignInAnswer.SIGNED_IN) {
                throw refusal("the built-in administrator " + administrator.username()
                        + " cannot sign in, and without it nothing can be provisioned");
            }
            administratorSession = session;
        }
        return administratorSession;
    }

    private String signIn(Session session, String username, String password)
            throws IOException, InterruptedException {
        String loginPage = session.get(SutSurface.LOGIN);
        return session.post(SutSurface.LOGIN, form(
                SutSurface.USERNAME_FIELD, username,
                SutSurface.PASSWORD_FIELD, password,
                SutSurface.CSRF_FIELD, csrfOf(loginPage, "the login page")));
    }

    private void signOut(Session session, String currentPage, String username)
            throws IOException, InterruptedException {
        session.post(SutSurface.LOGOUT, form(
                SutSurface.CSRF_FIELD, csrfOf(currentPage, "the screen " + username + " is on")));
    }

    private void completeChange(Session session, String heldScreen, String username)
            throws IOException, InterruptedException {
        String answer = session.post(SutSurface.CHANGE_PASSWORD, form(
                SutSurface.CURRENT_PASSWORD_FIELD, poolPassword,
                SutSurface.NEW_PASSWORD_FIELD, poolPassword,
                SutSurface.CONFIRM_PASSWORD_FIELD, poolPassword,
                SutSurface.CSRF_FIELD, csrfOf(heldScreen, "the password-change screen")));
        if (answer.contains(SutSurface.REFUSED_MARK)) {
            throw refusal("the password change of " + username + " was refused");
        }
        signOut(session, answer, username);
    }

    /**
     * What a sign-in was answered with. The held screen is served to a session that is already
     * signed in, so the password-change mark is read before the authenticated one.
     */
    enum SignInAnswer {
        SIGNED_IN, HELD_AT_CHANGE, REFUSED, UNKNOWN
    }

    static SignInAnswer classifySignIn(String answer) {
        if (answer.contains(SutSurface.PASSWORD_CHANGE_MARK)) {
            return SignInAnswer.HELD_AT_CHANGE;
        }
        if (answer.contains(SutSurface.AUTHENTICATED_MARK)) {
            return SignInAnswer.SIGNED_IN;
        }
        if (answer.contains(SutSurface.REFUSED_MARK)) {
            return SignInAnswer.REFUSED;
        }
        return SignInAnswer.UNKNOWN;
    }

    /** The token of the page a request is made from, or null when the page carries none. */
    static String csrfToken(String page) {
        int start = page.indexOf(CSRF_ANCHOR);
        if (start < 0) {
            return null;
        }
        int valueStart = start + CSRF_ANCHOR.length();
        int valueEnd = page.indexOf('"', valueStart);
        return valueEnd < 0 ? null : page.substring(valueStart, valueEnd);
    }

    private String csrfOf(String page, String pageName) {
        String token = csrfToken(page);
        if (token == null) {
            throw refusal(pageName + " carries no session token to make the next request with");
        }
        return token;
    }

    private static IllegalStateException refusal(String message) {
        return new IllegalStateException(message);
    }

    private static Map<String, String> form(String... namesAndValues) {
        Map<String, String> form = new LinkedHashMap<>();
        for (int i = 0; i < namesAndValues.length; i += 2) {
            form.put(namesAndValues[i], namesAndValues[i + 1]);
        }
        return form;
    }

    /** One browsing session: its own cookies, redirects followed the way a browser follows them. */
    private final class Session {

        private final HttpClient client = HttpClient.newBuilder()
                .cookieHandler(new CookieManager())
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(REQUEST_TIMEOUT)
                .build();

        String get(String path) throws IOException, InterruptedException {
            return answerOf(path, HttpRequest.newBuilder(URI.create(baseUrl + path))
                    .timeout(REQUEST_TIMEOUT)
                    .GET()
                    .build());
        }

        String post(String path, Map<String, String> form)
                throws IOException, InterruptedException {
            StringBuilder body = new StringBuilder();
            form.forEach((name, value) -> body
                    .append(body.isEmpty() ? "" : "&")
                    .append(URLEncoder.encode(name, StandardCharsets.UTF_8))
                    .append('=')
                    .append(URLEncoder.encode(value, StandardCharsets.UTF_8)));
            return answerOf(path, HttpRequest.newBuilder(URI.create(baseUrl + path))
                    .timeout(REQUEST_TIMEOUT)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build());
        }

        private String answerOf(String path, HttpRequest request)
                throws IOException, InterruptedException {
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw refusal("the request to " + path + " was answered with "
                        + response.statusCode() + " instead of a page");
            }
            return response.body();
        }
    }
}
