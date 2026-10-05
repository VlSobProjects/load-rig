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
import java.util.Map;
import java.util.Optional;
import loadrig.model.AnswerShape;
import loadrig.model.Correlation;

/**
 * One browsing session against the stand, outside any test plan: its own cookies, redirects
 * followed the way a browser follows them, and the token of the page it last loaded.
 *
 * <p>It is the transport of the rig's administration - the pool provisioning and the warm start -
 * and it is deliberately not the load's: it leaves no result log, and nothing it does belongs to a
 * capture. What it shares with the load model is the surface and the correlation dictionary, so a
 * change in the interface stays a change in one file.
 */
final class StandSession {

    /** How long one request may take before the administration fails instead of hanging. */
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

    private final String baseUrl;
    private final HttpClient client = HttpClient.newBuilder()
            .cookieHandler(new CookieManager())
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(REQUEST_TIMEOUT)
            .build();
    private String lastAnswer = "";

    StandSession(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    String get(String path) throws IOException, InterruptedException {
        return answerOf(path, request(path).GET().build());
    }

    String post(String path, Map<String, String> form)
            throws IOException, InterruptedException {
        return answerOf(path, formRequest(path, form).build());
    }

    /**
     * A request the page's script library would make, answered with the changed fragment instead
     * of a redirect to a whole page. The warm start makes its creations and transitions this way
     * for one reason: the answer carries the row that was just written, and the identity of a
     * created task is read from it - the same rule DR-1 fixes for the load.
     */
    String postFromTheScriptLibrary(String path, Map<String, String> form)
            throws IOException, InterruptedException {
        return answerOf(path, formRequest(path, form)
                .header(AnswerShape.SCRIPT_LIBRARY_HEADER,
                        AnswerShape.SCRIPT_LIBRARY_HEADER_VALUE)
                .build());
    }

    /**
     * The token of the page this session last loaded. It is read again before every mutating
     * request rather than once at the sign-in: the application renders the session's token afresh
     * on every answer, and a request made with the token of a page two answers ago is refused.
     */
    String token(String pageName) {
        return tokenIfAny().orElseThrow(() -> new IllegalStateException(pageName
                + " carries no session token to make the next request with"));
    }

    /**
     * The token of the last answer, when it carried one. Not every answer does - a screen with no
     * form on it renders none - so a caller that can load a page instead of failing asks here.
     */
    Optional<String> tokenIfAny() {
        return Correlation.sessionToken("token").read(lastAnswer);
    }

    static Map<String, String> form(String... namesAndValues) {
        Map<String, String> form = new LinkedHashMap<>();
        for (int index = 0; index < namesAndValues.length; index += 2) {
            form.put(namesAndValues[index], namesAndValues[index + 1]);
        }
        return form;
    }

    private HttpRequest.Builder request(String path) {
        return HttpRequest.newBuilder(URI.create(baseUrl + path)).timeout(REQUEST_TIMEOUT);
    }

    private HttpRequest.Builder formRequest(String path, Map<String, String> form) {
        StringBuilder body = new StringBuilder();
        form.forEach((name, value) -> body
                .append(body.isEmpty() ? "" : "&")
                .append(URLEncoder.encode(name, StandardCharsets.UTF_8))
                .append('=')
                .append(URLEncoder.encode(value, StandardCharsets.UTF_8)));
        return request(path)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()));
    }

    private String answerOf(String path, HttpRequest request)
            throws IOException, InterruptedException {
        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IllegalStateException("the request to " + path + " was answered with "
                    + response.statusCode() + " instead of a page");
        }
        lastAnswer = response.body();
        return lastAnswer;
    }
}
