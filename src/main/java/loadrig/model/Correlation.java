package loadrig.model;

import static us.abstracta.jmeter.javadsl.JmeterDsl.regexExtractor;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import us.abstracta.jmeter.javadsl.core.postprocessors.DslRegexExtractor;

/**
 * The correlation dictionary of DR 3: every rule that reads an identity or a token out of an
 * answer of the system under test lives here, named, beside the surface - never inline in
 * scenario logic - so that each rule can be verified, and one day regenerated, on its own
 * against a captured answer.
 *
 * <p>A rule is one expression with one capturing group. It is applied by the injector as an
 * extractor, and by the rig's own Java - the user directory harvest, the tests - through
 * {@link Rule#read}, so both readers hold the same expression by construction.
 */
public final class Correlation {

    /**
     * What an extractor leaves behind when it finds nothing. It is not a fallback: the request
     * built from it is refused, and the run states the refusal instead of walking on in silence.
     */
    public static final String EXTRACTION_FAILED = "EXTRACTION-FAILED";

    /**
     * One correlation rule: the variable the injector reads the finding into, and the expression
     * that finds it, whose first group is the value.
     */
    public record Rule(String variable, String expression) {

        public Rule {
            Objects.requireNonNull(variable, "variable");
            Objects.requireNonNull(expression, "expression");
        }

        /** The rule as the injector applies it, refusing loudly through the failure marker. */
        public DslRegexExtractor extractor() {
            return regexExtractor(variable, expression).defaultValue(EXTRACTION_FAILED);
        }

        /** The rule as the rig's own Java applies it, for harvests and for the tests. */
        public Optional<String> read(String answer) {
            Matcher matcher = Pattern.compile(expression).matcher(answer);
            return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
        }
    }

    /**
     * The token of the session, read from the hidden field every form of the application
     * renders. The token is scraped per session (DR 1): the whole page the sign-in lands on
     * carries it, and it holds until the session ends.
     */
    public static Rule sessionToken(String variable) {
        return new Rule(variable,
                "name=\"" + SutSurface.CSRF_FIELD + "\" value=\"([^\"]*)\"");
    }

    /**
     * The identity of a task, read from the row the creation answered with and anchored on the
     * title only this creator used. Taking the first row of the answer instead would be a
     * lottery the moment the run acts on a database that already holds tasks.
     */
    public static Rule taskIdentity(String variable, String titleAnchor) {
        return new Rule(variable,
                "(?s)" + titleAnchor + ".*?href=\"" + SutSurface.TASKS + "/(\\d+)\\?");
    }

    /**
     * The identity of an account, read from the options the application itself offers rather
     * than assumed from the order a database seeds its accounts in.
     */
    public static Rule userIdentity(String variable, String username) {
        return new Rule(variable, "<option value=\"(\\d+)\">" + username + "<");
    }

    /**
     * The identity of a provisioned account, read from the row the creation answered with and
     * anchored on the name only its creator used.
     */
    public static Rule accountIdentity(String variable, String accountName) {
        return new Rule(variable,
                "(?s)" + accountName + ".*?" + SutSurface.USERS + "/(\\d+)/delete");
    }

    private Correlation() {
    }
}
