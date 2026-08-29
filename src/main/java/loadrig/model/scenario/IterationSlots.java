package loadrig.model.scenario;

/**
 * The thread variables a gate prepares for the requests of its step. Each thread runs one
 * scenario, so the slots are shared by all scenarios without colliding.
 */
final class IterationSlots {

    /** The task the step acts on: opened, discussed or moved. */
    static final String TASK = "stepTask";

    /** The lease a moving step holds until its outcome is recorded. */
    static final String MOVE_LEASE = "stepMoveLease";

    /** The identity the SUT knows the named account by, for a creation or a hand-out. */
    static final String ASSIGNEE_ID = "stepAssigneeId";

    /** The pool name behind {@link #ASSIGNEE_ID}, for the registry's record of the outcome. */
    static final String ASSIGNEE_NAME = "stepAssigneeName";

    /** The title of the task being created, unique within the run. */
    static final String TASK_TITLE = "stepTaskTitle";

    /** How far ahead the created task is due: inside the hot window or beyond it. */
    static final String DUE_SHIFT = "stepDueShift";

    /** Whether the created task lands in the hot set, for the registry's record. */
    static final String CREATED_HOT = "stepCreatedHot";

    /** The identity of the task the creation answered with. */
    static final String CREATED_TASK = "stepCreatedTask";

    /** The worker a report is asked about. */
    static final String REPORT_WORKER_ID = "stepReportWorkerId";

    /** The page every list is asked from: the load model browses, it does not crawl. */
    static final String FIRST_PAGE = "1";

    private IterationSlots() {
    }
}
