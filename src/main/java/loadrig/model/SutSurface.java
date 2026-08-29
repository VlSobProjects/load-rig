package loadrig.model;

import java.util.Optional;

/**
 * The HTTP surface of the system under test: the addresses behind the business steps and the
 * fields of the forms that drive them.
 *
 * <p>It states in one place what the appendix of the SUT project's scenario specification
 * describes, verified against a running stack. A scenario step names the business act; the
 * address behind it is looked up here, so that a change in the interface is a change in one file.
 *
 * <p>The operational endpoints of the stack are deliberately absent: they belong to the
 * collectors, and traffic on them pollutes the timer a verdict is read from.
 */
public final class SutSurface {

    public static final String LOGIN = "/login";
    public static final String LOGOUT = "/logout";
    public static final String CHANGE_PASSWORD = "/change-password";
    public static final String TASKS = "/tasks";
    public static final String USERS = "/users";
    public static final String REPORTS = "/reports";

    /** The token every mutating request carries, scraped from the page the request is made from. */
    public static final String CSRF_FIELD = "_csrf";

    public static final String USERNAME_FIELD = "username";
    public static final String PASSWORD_FIELD = "password";
    public static final String ROLE_FIELD = "role";

    public static final String CURRENT_PASSWORD_FIELD = "currentPassword";
    public static final String NEW_PASSWORD_FIELD = "newPassword";
    public static final String CONFIRM_PASSWORD_FIELD = "confirmPassword";

    public static final String TASK_TITLE_FIELD = "title";
    public static final String TASK_DESCRIPTION_FIELD = "description";
    public static final String TASK_ASSIGNEE_FIELD = "assigneeId";
    public static final String TASK_DUE_DATE_FIELD = "dueDate";

    /** Optional on a completion, required on a return with a question and on a refusal. */
    public static final String TRANSITION_MESSAGE_FIELD = "message";

    /** The body of a note. A wrong field name is refused with a 422, not with a 400. */
    public static final String NOTE_TEXT_FIELD = "text";

    public static final String REPORT_WORKER_FIELD = "workerId";
    public static final String REPORT_FROM_DATE_FIELD = "fromDate";
    public static final String REPORT_TO_DATE_FIELD = "toDate";

    /**
     * What the application renders when a step it nevertheless answers with a 200 was refused, and
     * what it renders when such a step was accepted. Three steps answer a refusal with a rendered
     * page rather than with a status - the sign-in, the creation of a user and the password change
     * - and a rig that reads only the status walks past all three.
     */
    public static final String ACCEPTED_MARK = "alert alert-success";

    public static final String REFUSED_MARK = "alert alert-danger";

    /** Rendered by every screen an authenticated session reaches, and by no screen before it. */
    public static final String AUTHENTICATED_MARK = "action=\"" + LOGOUT + "\"";

    /**
     * The form of the password change, rendered by the screen a fresh account is held at until it
     * chooses a new password. Checked before the authenticated mark: the held screen is served to a
     * signed-in session, and a reader looking for the authenticated mark alone would take an
     * account that cannot reach any other screen for a usable one.
     */
    public static final String PASSWORD_CHANGE_MARK = "action=\"" + CHANGE_PASSWORD + "\"";

    /** The state of the list, carried by every request to the task surface and by every answer. */
    public static final String SCOPE_FIELD = "scope";
    public static final String SORT_FIELD = "sort";
    public static final String PAGE_FIELD = "page";

    public static String task(String taskId) {
        return TASKS + "/" + taskId;
    }

    public static String taskNotes(String taskId) {
        return task(taskId) + "/notes";
    }

    public static String taskTransition(String taskId, Transition transition) {
        return task(taskId) + "/" + transition.address();
    }

    public static String userDeletion(String userId) {
        return USERS + "/" + userId + "/delete";
    }

    /**
     * What a list is asked for. A closed list: the scope narrows what the viewer may already see
     * and never widens it, and a value outside the list is refused.
     */
    public enum Scope {

        ALL("all"),
        ASSIGNED_BY_ME("by-me"),
        ASSIGNED_TO_ME("to-me");

        private final String value;

        Scope(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }

    /** The sort key of a list. A closed list: a key outside it is answered with a rendered 400. */
    public enum Sort {

        CREATED("created"),
        DUE_DATE("due"),
        STATUS("status"),
        ASSIGNEE("assignee");

        private final String value;

        Sort(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }

    /**
     * How a list row spells the status of a task. The names are the registry's states; the marks
     * are the application's own words for them, and the two are not the same word everywhere -
     * finished work is stored as completed and shown as done. The spelling belongs here beside the
     * addresses, the state belongs to the registry, and the mapping between them is made once, the
     * way the transitions are.
     *
     * <p>A row also carries marks that are no status at all - an open task past its due date is
     * marked overdue - so a reader takes the first mark it can name and refuses a row whose marks
     * it cannot, instead of registering a task in a state the rig invented.
     */
    public enum StatusMark {

        OPEN("Open"),
        COMPLETED("Done"),
        RETURNED("Returned"),
        REFUSED("Refused"),
        APPROVED("Approved"),
        ACKNOWLEDGED("Acknowledged");

        private final String mark;

        StatusMark(String mark) {
            this.mark = mark;
        }

        public String mark() {
            return mark;
        }

        /** The status behind the mark a row shows, or empty when the mark names no status. */
        public static Optional<StatusMark> ofMark(String mark) {
            for (StatusMark known : values()) {
                if (known.mark.equals(mark)) {
                    return Optional.of(known);
                }
            }
            return Optional.empty();
        }
    }

    /**
     * The transitions of the task transition table, named after the business act each one is and
     * carrying the address behind it. Who may apply which of them is the task registry's subject
     * and not this class's.
     */
    public enum Transition {

        FINISH("complete"),
        RETURN_WITH_A_QUESTION("return"),
        REFUSE("refuse"),
        HAND_OUT_AGAIN("reassign"),
        APPROVE("approve"),
        ACCEPT_THE_REFUSAL("acknowledge"),
        PUT_BACK_TO_WORK("reopen"),
        DELETE("delete");

        private final String address;

        Transition(String address) {
            this.address = address;
        }

        public String address() {
            return address;
        }
    }

    private SutSurface() {
    }
}
