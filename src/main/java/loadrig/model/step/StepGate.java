package loadrig.model.step;

import org.apache.jmeter.threads.JMeterVariables;

/**
 * What a step settles before its requests are made: the registry picks - a task to open, a
 * lease on a transition, an account to name - written into the thread's variables for the
 * requests to read.
 *
 * <p>A gate that cannot be prepared skips the step for this iteration and answers false. A skip
 * is a timing state of the run - a manager with nothing to approve in the first minute - and
 * not a wiring mistake; the gate records it on the starvation ledger, so no skip is silent. A
 * wiring mistake still throws, loudly.
 */
public interface StepGate {

    boolean prepared(JMeterVariables vars);
}
