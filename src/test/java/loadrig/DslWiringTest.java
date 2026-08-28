package loadrig;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import org.junit.jupiter.api.Test;

/**
 * Proves the dependency wiring: a test plan can be built from the DSL without running it.
 * No request is made; the SUT stack is not required.
 */
class DslWiringTest {

    @Test
    void aTestPlanCanBeBuiltFromTheDsl() {
        var plan = testPlan(
                threadGroup(1, 1,
                        httpSampler("http://localhost:8080/login")));
        assertNotNull(plan);
    }
}
