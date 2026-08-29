package loadrig.model.scenario;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * The run's record of skipped steps. A step whose gate finds nothing to act on - a manager with
 * nothing to approve in the run's first minute - is skipped for the iteration, and the skip is
 * written here: the run harness reports the counts, so a run whose realized mix drifted from
 * the profile's says where and by how much instead of keeping the drift a secret of the log.
 */
public final class StarvationLedger {

    private final ConcurrentHashMap<String, LongAdder> skips = new ConcurrentHashMap<>();

    public void skipped(String stepName) {
        skips.computeIfAbsent(stepName, name -> new LongAdder()).increment();
    }

    /** The skip counts by step name, empty when every gate always found its pick. */
    public Map<String, Long> skips() {
        Map<String, Long> counts = new LinkedHashMap<>();
        skips.forEach((name, count) -> counts.put(name, count.sum()));
        return counts;
    }
}
