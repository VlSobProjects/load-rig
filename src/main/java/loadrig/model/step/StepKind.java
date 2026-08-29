package loadrig.model.step;

/**
 * What a step is in the language of the profile's mix: the seven step kinds of the
 * specification's mix table, plus the keeping of sessions, which is the rig's own act and no
 * row of the mix. Every step of a scenario carries its kind, so the shares a plan implies can
 * be computed from the plan itself and held against the profile instead of trusted.
 */
public enum StepKind {

    LOOKING_AT_A_LIST,
    OPENING_ONE_TASK,
    MOVING_A_TASK,
    DISCUSSION,
    RUNNING_A_REPORT,
    CREATING_A_TASK,
    DELETING_A_TASK,

    /** Sign-ins and sign-outs: not a row of the mix, and never counted against it. */
    SESSION_KEEPING
}
