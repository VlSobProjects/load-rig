package loadrig.run;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * The version of the system under test, asked of the stand itself.
 *
 * <p>The stand publishes it on an information endpoint, fed from its own build and answered
 * without a session. The rig asks once per run, before the window opens, and records the answer in
 * the capture's description - so the field stops being an operator's statement that nobody
 * verifies, and stops reading unknown on every capture.
 *
 * <p>The address lives here and not on the SUT surface on purpose. That surface holds the
 * business screens the scenarios walk, and it excludes the operational endpoints because traffic
 * on them pollutes the timer a verdict is read from. This one request breaks nothing of that rule:
 * it is made by the harness before the load starts, outside the test plan, and it never becomes a
 * sample of the result log.
 *
 * <p>A stand that does not answer leaves the version unknown. A run is not spoiled by a missing
 * label, and refusing to start over one would trade a whole stand window for a field.
 */
public final class SutVersion {

    /** What the description says when neither the operator nor the stand named a version. */
    public static final String UNKNOWN = "unknown";

    /** Where the stand publishes its build facts. Not a business screen; see the class note. */
    private static final String INFORMATION_ENDPOINT = "/actuator/info";

    /** Short on purpose: the harness asks before the window and does not wait on a sick stand. */
    private static final Duration PATIENCE = Duration.ofSeconds(3);

    private static final ObjectMapper JSON = new ObjectMapper();

    /**
     * Asks the stand for its version, and answers {@link #UNKNOWN} for every way the question can
     * fail: no stand, no endpoint, a refusal, a body in a shape this reader does not know.
     */
    public static String askTheStand(String baseUrl) {
        try {
            HttpClient client = HttpClient.newBuilder().connectTimeout(PATIENCE).build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + INFORMATION_ENDPOINT))
                    .timeout(PATIENCE)
                    .GET()
                    .build();
            HttpResponse<String> answer =
                    client.send(request, HttpResponse.BodyHandlers.ofString());
            if (answer.statusCode() != 200) {
                return UNKNOWN;
            }
            return versionIn(answer.body());
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            return UNKNOWN;
        } catch (Exception theStandDidNotAnswer) {
            return UNKNOWN;
        }
    }

    /**
     * The version inside the answer: the build's version where the stand publishes its build
     * facts, a plain version field where it publishes only that, and unknown where it publishes
     * neither. Read as three named possibilities rather than as a path into an assumed document,
     * because a shape this reader does not recognise must produce an honest unknown and not an
     * exception in the harness.
     */
    static String versionIn(String body) {
        try {
            JsonNode document = JSON.readTree(body);
            JsonNode ofTheBuild = document.path("build").path("version");
            if (ofTheBuild.isTextual() && !ofTheBuild.asText().isBlank()) {
                return ofTheBuild.asText();
            }
            JsonNode stated = document.path("version");
            if (stated.isTextual() && !stated.asText().isBlank()) {
                return stated.asText();
            }
            return UNKNOWN;
        } catch (Exception notAShapeWeKnow) {
            return UNKNOWN;
        }
    }

    private SutVersion() {
    }
}
