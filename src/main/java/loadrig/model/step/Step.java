package loadrig.model.step;

import static us.abstracta.jmeter.javadsl.JmeterDsl.ifController;
import static us.abstracta.jmeter.javadsl.JmeterDsl.percentController;

import java.util.List;
import java.util.Optional;
import us.abstracta.jmeter.javadsl.core.threadgroups.BaseThreadGroup.ThreadGroupChild;
import us.abstracta.jmeter.javadsl.http.DslHttpSampler;

/**
 * One step of a scenario: a business act with its weight in the scenario's own mix, its kind in
 * the language of the profile, the gate that prepares it and the requests that perform it.
 *
 * <p>A step is built only by {@link StepKit#step}, which is how DR-3's obligations are taken by
 * signatures instead of remembered by authors: the requests already carry their stable names,
 * their content assertions and their extractions, and the think time sits on the first request,
 * once per business act.
 */
public final class Step {

    private final int weight;
    private final StepKind kind;
    private final StepGate gate;
    private final List<DslHttpSampler> requests;

    Step(int weight, StepKind kind, StepGate gate, List<DslHttpSampler> requests) {
        this.weight = weight;
        this.kind = kind;
        this.gate = gate;
        this.requests = List.copyOf(requests);
    }

    public int weight() {
        return weight;
    }

    public StepKind kind() {
        return kind;
    }

    public Optional<StepGate> gate() {
        return Optional.of(gate).filter(g -> g != StepKit.NO_GATE);
    }

    public List<DslHttpSampler> requests() {
        return requests;
    }

    /**
     * The step as the plan schedules it: performed on the given share of iterations, and only
     * when its gate could be prepared.
     */
    public ThreadGroupChild scheduled(float percentOfIterations) {
        ThreadGroupChild[] children = requests.toArray(new ThreadGroupChild[0]);
        if (gate != StepKit.NO_GATE) {
            return percentController(percentOfIterations,
                    ifController(vars -> gate.prepared(vars.vars), children));
        }
        return percentController(percentOfIterations, children);
    }
}
