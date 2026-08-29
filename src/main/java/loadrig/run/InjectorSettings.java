package loadrig.run;

import java.util.Locale;

/**
 * The settings of the injector itself, applied by every entry point that runs a plan. They live
 * in one place so that the smoke run and the profile run cannot drift apart: two runs whose logs
 * carry different timestamp semantics are two logs nobody can compare.
 */
final class InjectorSettings {

    /**
     * The timestamp semantics of the JTL log, set explicitly and never left to a default. A sample
     * is stamped with the moment it started, so that achieved intensity over a window is the rate
     * of requests a user made and not the rate at which the answers happened to arrive.
     *
     * <p>It is set as a property of the machine and not through the engine, because the injector
     * reads it once, when the class that stamps a sample is first loaded. A value handed to the
     * engine arrives after that moment and changes nothing, which was verified by reading the log
     * a run left: the stamps were the moments the answers came back.
     */
    private static final String JTL_TIMESTAMP_SEMANTICS = "sampleresult.timestamp.start";

    private static final String SAMPLES_ARE_STAMPED_WHEN_THEY_START = "true";

    /**
     * The language of the injector itself, stated rather than taken from the machine a run happens
     * to start on. A generator that reads its own messages in one language on one host and in
     * another on the next has one more difference between two captures that nobody wrote down,
     * and on a host whose language the injector carries no resources for every run opens with an
     * error of its own that is not the system's.
     */
    private static final Locale INJECTOR_LANGUAGE = Locale.ENGLISH;

    /** Applied before the plan is even assembled, because the injector reads the settings once. */
    static void apply() {
        Locale.setDefault(INJECTOR_LANGUAGE);
        System.setProperty(JTL_TIMESTAMP_SEMANTICS, SAMPLES_ARE_STAMPED_WHEN_THEY_START);
    }

    private InjectorSettings() {
    }
}
