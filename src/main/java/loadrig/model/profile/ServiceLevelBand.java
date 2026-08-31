package loadrig.model.profile;

/**
 * The closed list of acts the service levels are stated for, each one a step as the person
 * performing it experiences it. A band is what a figure is attached to, so that a capture states
 * its latency against the act a user would name and not against the address a request went to.
 *
 * <p>Most rows are the SUT specification's own service-level table. The list is not that table
 * and never was: the load model belongs to this project, and an act the model performs which the
 * specification's table does not name still needs a figure to be judged by, or the run would
 * either leave it unjudged or hide it inside a band measuring something else. Such a row is this
 * project's statement - derived the way a load analyst derives one, from how the act is used
 * rather than from what the system's authors would like it to cost - and it travels to the SUT
 * project through the desk as a fact of the campaign, not as a question. {@link #SIGNING_OUT} is
 * the first of them.
 *
 * <p>Which band judges a step is the step kind's own fact and lives with the kinds; a band knows
 * only its spelling in the configuration file.
 */
public enum ServiceLevelBand {

    /** The screen every session lives on and returns to after each act. */
    OPENING_A_LIST("openingAList"),

    /** One task read: its fields, its history, its notes. */
    OPENING_ONE_TASK("openingOneTask"),

    /** An act with a consequence and the answer that shows it: a transition, a creation, a
     * deletion. Feedback on one's own click, and the least forgiving of the reads. */
    PERFORMING_AN_ACTION("performingAnAction"),

    /** The discussion: the notes of a task read and a remark left on it. */
    WRITING_A_NOTE("writingANote"),

    /** Deliberately looser than the rest: password hashing is real work and is meant to be slow. */
    SIGNING_IN("signingIn"),

    /**
     * Giving up a session at the end of a stint. This project's own row: the specification's
     * table names no such act, and judging it in {@link #SIGNING_IN} would put a request that
     * hashes no password into the one band held loose because hashing is slow - which is how a
     * band stops meaning what it is named after. It is held tighter than signing in for exactly
     * that reason: nothing about ending a session is meant to be expensive.
     */
    SIGNING_OUT("signingOut"),

    /** The wide read a person explicitly asked for and therefore waits for. */
    RUNNING_A_REPORT("runningAReport");

    private final String key;

    ServiceLevelBand(String key) {
        this.key = key;
    }

    /** The spelling the service-levels file uses. */
    public String key() {
        return key;
    }

    /** The band behind a file's spelling, or a loud refusal naming the closed list. */
    public static ServiceLevelBand ofKey(String key) {
        for (ServiceLevelBand band : values()) {
            if (band.key().equals(key)) {
                return band;
            }
        }
        throw new IllegalArgumentException("\"" + key + "\" names no service-level band of the"
                + " rig; the closed list is " + keys());
    }

    private static String keys() {
        StringBuilder list = new StringBuilder();
        for (ServiceLevelBand band : values()) {
            list.append(list.isEmpty() ? "" : ", ").append(band.key());
        }
        return list.toString();
    }
}
