package loadrig.model.step;

import static us.abstracta.jmeter.javadsl.JmeterDsl.httpHeaders;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.uniformRandomTimer;

import java.time.Duration;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import loadrig.model.AnswerShape;
import loadrig.model.Correlation;
import loadrig.model.profile.LoadProfile;
import us.abstracta.jmeter.javadsl.core.timers.DslUniformRandomTimer;
import us.abstracta.jmeter.javadsl.http.DslHttpSampler;

/**
 * The step kit of DR-3: the thin layer over the DSL whose signatures take what every step owes,
 * so the compiler holds the pattern. A request cannot be built without its stable name and its
 * content assertion; its extractions come from the correlation dictionary; the address comes
 * from the surface through the caller, so a literal address has nowhere to go. Everything the
 * DSL already expresses well - the method, the parameters - passes through untouched on the
 * returned sampler, because the kit's named failure mode is growing into a DSL over the DSL.
 *
 * <p>A step carries the think time of the profile on its first request, once per business act:
 * the pause is what a person spends before the act, not before every request the act consists
 * of, and the intensities the equilibrium check computes rest on exactly that reading.
 */
public final class StepKit {

    /** The gate of a step that needs nothing prepared. */
    static final StepGate NO_GATE = vars -> true;

    private final String baseUrl;
    private final LoadProfile.ThinkTime thinkTime;
    private final Map<DslHttpSampler, String> namesOfRequests = new IdentityHashMap<>();
    private final Map<String, StepKind> kindsByLabel = new LinkedHashMap<>();

    public StepKit(String baseUrl, LoadProfile.ThinkTime thinkTime) {
        this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl");
        this.thinkTime = Objects.requireNonNull(thinkTime, "thinkTime");
    }

    /**
     * A request the page's script library would make, carrying the answer-shape header of DR-1
     * and answered with a fragment.
     */
    public DslHttpSampler fragmentRequest(String name, String path,
            ContentExpectation expectation, Correlation.Rule... extractions) {
        DslHttpSampler request = pageRequest(name, path, expectation, extractions);
        request.children(httpHeaders()
                .header(AnswerShape.SCRIPT_LIBRARY_HEADER,
                        AnswerShape.SCRIPT_LIBRARY_HEADER_VALUE));
        return request;
    }

    /**
     * An ordinary browser request - a full page or a plain form post - carrying no answer-shape
     * header, because a browser sends none on it.
     */
    public DslHttpSampler pageRequest(String name, String path, ContentExpectation expectation,
            Correlation.Rule... extractions) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(path, "path");
        DslHttpSampler request = httpSampler(name, baseUrl + path)
                .children(expectation.assertion());
        for (Correlation.Rule extraction : extractions) {
            request.children(extraction.extractor());
        }
        namesOfRequests.put(request, name);
        return request;
    }

    /** A step that needs nothing prepared: its requests read only what the session holds. */
    public Step step(int weight, StepKind kind, DslHttpSampler... requests) {
        return step(weight, kind, NO_GATE, requests);
    }

    /** A step whose gate settles the registry picks before the requests are made. */
    public Step step(int weight, StepKind kind, StepGate gate, DslHttpSampler... requests) {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(gate, "gate");
        if (weight <= 0) {
            throw new IllegalArgumentException(
                    "a step's weight must be positive, and it is " + weight);
        }
        if (requests.length == 0) {
            throw new IllegalArgumentException("a step without a request is not a step");
        }
        declareKind(kind, requests);
        requests[0].children(thinkTimer());
        return new Step(weight, kind, gate, List.of(requests));
    }

    /**
     * The kind of requests that belong to no step of a scenario's iteration - the sign-ins, which
     * are the rig's own act and no row of the mix - stated so that the plan's labels are complete.
     * A step's requests are declared by {@link #step}, and every request the kit builds passes
     * through one of the two: a sample the plan cannot name is judged by the hard ceiling alone
     * and counted apart, and this is what keeps that count at zero for the steps the rig means to
     * perform.
     */
    public void declareKind(StepKind kind, DslHttpSampler... requests) {
        Objects.requireNonNull(kind, "kind");
        for (DslHttpSampler request : requests) {
            String label = namesOfRequests.get(request);
            if (label == null) {
                throw new IllegalArgumentException("a request this kit did not build cannot be"
                        + " declared under a step kind: its label is not the kit's to know");
            }
            StepKind declared = kindsByLabel.putIfAbsent(label, kind);
            if (declared != null && declared != kind) {
                throw new IllegalArgumentException("the request \"" + label + "\" is declared"
                        + " both as " + declared + " and as " + kind + "; one label is one act,"
                        + " and a label two kinds share is judged in whichever band was built"
                        + " first");
            }
        }
    }

    /**
     * The kind of every request the kit built, by the label the result log carries. It is how a
     * sample is attributed to the act a person performed without the label having to spell the
     * act out: the plan states the kinds, and the label is only a name.
     */
    public Map<String, StepKind> stepKindsByLabel() {
        return Map.copyOf(kindsByLabel);
    }

    /** The pause a virtual user thinks before a step, drawn from the profile's bounds. */
    public DslUniformRandomTimer thinkTimer() {
        return uniformRandomTimer(Duration.ofSeconds(thinkTime.minSeconds()),
                Duration.ofSeconds(thinkTime.maxSeconds()));
    }

    /** A reference the injector resolves at request time. */
    public static String variable(String name) {
        return "${" + name + "}";
    }

    /** A date the injector computes at request time, shifted from today by an ISO period. */
    public static String dateShiftedBy(String period) {
        return "${__timeShift(yyyy-MM-dd,," + period + ",,)}";
    }

    public static String today() {
        return "${__timeShift(yyyy-MM-dd,,,,)}";
    }
}
