package loadrig.model.step;

import static us.abstracta.jmeter.javadsl.JmeterDsl.responseAssertion;

import java.util.Objects;
import loadrig.model.SutSurface;
import us.abstracta.jmeter.javadsl.core.assertions.DslResponseAssertion;

/**
 * What a step expects to read in the answer to its request. Every step states one, because a
 * step without a content assertion is how a false capture begins: three steps of this
 * application answer a refusal with a rendered page and a 200, and a rig that reads only the
 * status walks past all three.
 *
 * <p>Two forms exist. A step whose successful answer has a proven mark states the mark. A step
 * whose positive mark has not been proven against the stack yet states the weaker fact that the
 * answer renders no refusal; the conformance walk is where such marks are strengthened.
 */
public final class ContentExpectation {

    private final String description;
    private final String mark;
    private final boolean markMustBeAbsent;

    private ContentExpectation(String description, String mark, boolean markMustBeAbsent) {
        this.description = Objects.requireNonNull(description, "description");
        this.mark = Objects.requireNonNull(mark, "mark");
        this.markMustBeAbsent = markMustBeAbsent;
    }

    /** The answer carries the mark a successful step is known to render. */
    public static ContentExpectation renders(String description, String mark) {
        return new ContentExpectation(description, mark, false);
    }

    /** The answer renders no refusal: the weaker expectation of a step with no proven mark. */
    public static ContentExpectation rendersNoRefusalMark() {
        return new ContentExpectation("the answer renders no refusal",
                SutSurface.REFUSED_MARK, true);
    }

    /** The expectation as the injector holds it, built fresh for each step that states it. */
    DslResponseAssertion assertion() {
        DslResponseAssertion assertion = responseAssertion(description).containsSubstrings(mark);
        return markMustBeAbsent ? assertion.invertCheck() : assertion;
    }
}
