package loadrig.model.step;

import loadrig.model.profile.ServiceLevelBand;

/**
 * What a step is in the language of the profile's mix: the seven step kinds of the
 * specification's mix table, plus the two acts by which a session begins and ends, which are the
 * load model's own and no rows of the mix. Every step of a scenario carries its kind, so the
 * shares a plan implies can be computed from the plan itself and held against the profile instead
 * of trusted.
 *
 * <p>A kind also knows the service-level band its samples are judged in. The mapping is stated
 * here as an exhaustive switch, so a kind added later cannot compile until somebody decides what
 * a person waiting for it is entitled to.
 */
public enum StepKind {

    LOOKING_AT_A_LIST,
    OPENING_ONE_TASK,
    MOVING_A_TASK,
    DISCUSSION,
    RUNNING_A_REPORT,
    CREATING_A_TASK,
    DELETING_A_TASK,

    /** Signing in: not a row of the mix, and never counted against it. */
    SESSION_KEEPING,

    /**
     * Giving up a session, so that another can begin (DR-8). No row of the mix either: it takes
     * no share away from the business steps and carries an intensity of its own, stated by the
     * profile as a share of the iterations that end this way.
     */
    GIVING_UP_A_SESSION;

    /**
     * The band of the service levels this kind's samples are judged in.
     *
     * <p>Two mappings are worth stating. The three kinds that change something - a transition, a
     * creation, a deletion - are one band, because the specification measures the act and its
     * answer rather than the verb. And the discussion, whose one business act reads the notes of
     * a task and leaves a remark on it, is judged in the note's band: the specification gives the
     * read and the write the same figure, and splitting them here would invent a distinction it
     * does not make.
     */
    public ServiceLevelBand band() {
        return switch (this) {
            case LOOKING_AT_A_LIST -> ServiceLevelBand.OPENING_A_LIST;
            case OPENING_ONE_TASK -> ServiceLevelBand.OPENING_ONE_TASK;
            case MOVING_A_TASK, CREATING_A_TASK, DELETING_A_TASK ->
                    ServiceLevelBand.PERFORMING_AN_ACTION;
            case DISCUSSION -> ServiceLevelBand.WRITING_A_NOTE;
            case RUNNING_A_REPORT -> ServiceLevelBand.RUNNING_A_REPORT;
            case SESSION_KEEPING -> ServiceLevelBand.SIGNING_IN;
            case GIVING_UP_A_SESSION -> ServiceLevelBand.SIGNING_OUT;
        };
    }
}
