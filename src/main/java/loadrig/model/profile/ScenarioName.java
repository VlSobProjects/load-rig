package loadrig.model.profile;

import java.util.Locale;

/**
 * The closed list of scenario names a profile may refer to: the three walked role scenarios and
 * the assembled discussion of the SUT project's scenario specification. A profile names a
 * scenario only out of this list, and a name outside it refuses the load before any is applied.
 *
 * <p>The factories behind the names - the scenario classes DR-3 shapes - join this list when
 * the scenarios become classes; the list itself is the vocabulary's fact and exists first, so
 * that a profile file can already be validated against it.
 */
public enum ScenarioName {

    WORKER,
    MANAGER,
    ADMINISTRATOR,
    DISCUSSION;

    /** The spelling a profile file uses. */
    public String key() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** The name behind a profile file's spelling, or a loud refusal naming the closed list. */
    public static ScenarioName ofKey(String key) {
        for (ScenarioName name : values()) {
            if (name.key().equals(key)) {
                return name;
            }
        }
        throw new IllegalArgumentException("\"" + key + "\" names no scenario of the rig; "
                + "the closed list is worker, manager, administrator, discussion");
    }
}
