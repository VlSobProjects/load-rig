package loadrig.model;

import static us.abstracta.jmeter.javadsl.JmeterDsl.httpCookies;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpHeaders;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.jsr223PreProcessor;
import static us.abstracta.jmeter.javadsl.JmeterDsl.jtlWriter;
import static us.abstracta.jmeter.javadsl.JmeterDsl.responseAssertion;
import static us.abstracta.jmeter.javadsl.JmeterDsl.simpleController;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import java.util.Map;
import java.util.Objects;
import loadrig.model.SutSurface.Scope;
import loadrig.model.SutSurface.Sort;
import loadrig.model.SutSurface.Transition;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.controllers.DslSimpleController;
import us.abstracta.jmeter.javadsl.core.postprocessors.DslRegexExtractor;
import us.abstracta.jmeter.javadsl.core.preprocessors.DslJsr223PreProcessor;
import us.abstracta.jmeter.javadsl.http.DslHttpSampler;
import us.abstracta.jmeter.javadsl.http.HttpHeaders;

/**
 * The transport skeleton: one linear walk that plays each role in turn against the system under
 * test, proving every element a scenario needs - the form login with its scraped token, the
 * answer shape of DR 1, the identity of a task read out of the answer that created it, a
 * transition, a note, the expensive read, the unscoped browsing, the creation of an account and
 * the password change that account is held at.
 *
 * <p>The walk is deliberately linear and self-sufficient per virtual user: each user creates the
 * task it then acts on, so nothing here decides how virtual users share sessions or tasks. That
 * is the subject of the session and task registries and is left to them.
 */
public final class TransportWalk {

    /** The token of the signed-in session, scraped from the whole page the sign-in lands on. */
    private static final String CSRF_VARIABLE = "csrfToken";

    /** The identity of the task this virtual user created and then works on. */
    private static final String TASK_ID_VARIABLE = "taskId";

    /** The identity of the worker account, read from the options the application itself offers. */
    private static final String WORKER_USER_ID_VARIABLE = "workerUserId";

    /** What makes this walk's task and account unique among all virtual users of a run. */
    private static final String WALK_TAG_VARIABLE = "walkTag";

    private static final String WALK_ACCOUNT_VARIABLE = "walkAccount";

    /** The identity of the account this walk provisioned, read from the row it appears in. */
    private static final String WALK_ACCOUNT_ID_VARIABLE = "walkAccountId";

    private static final String FIRST_PAGE = "1";

    /**
     * How far ahead a created task is due. The profile's hot set is the open tasks due within
     * three days, so a walk that creates a task creates one of them; the skew that decides how
     * much attention the hot set receives belongs to the day profile and not here.
     */
    private static final String DUE_WITHIN = "P3D";

    /**
     * The window a manager asks a report about. A manager asks about last week, about a worker
     * they manage; a range drawn at random over the whole history is the most expensive question
     * the application offers and would dominate every capture, the clean baseline included.
     */
    private static final String REPORT_WINDOW_START = "-P7D";

    private final String baseUrl;
    private final Map<Role, Account> accounts;
    private final String provisionedPassword;
    private final String runTag;

    /**
     * @param runTag what tells one run's artifacts from another's inside the application. Two runs
     *     against the same database must not create tasks and accounts of the same names: the
     *     first run's leftovers would answer the second run's questions.
     */
    public TransportWalk(String baseUrl, Map<Role, Account> accounts, String provisionedPassword,
            String runTag) {
        this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl");
        this.accounts = Map.copyOf(accounts);
        this.provisionedPassword = Objects.requireNonNull(provisionedPassword, "provisionedPassword");
        this.runTag = Objects.requireNonNull(runTag, "runTag");
        for (Role role : Role.values()) {
            if (!this.accounts.containsKey(role)) {
                throw new IllegalArgumentException("the walk has no account for the role " + role);
            }
        }
    }

    public DslTestPlan plan(int virtualUsers, int iterations, String jtlDirectory,
            String jtlFileName) {
        return testPlan(
                httpCookies(),
                threadGroup("the transport walk", virtualUsers, iterations,
                        theManagerHandsOutWork(),
                        theWorkerActsOnTheTask(),
                        theManagerSettlesAndReports(),
                        theAdministratorBrowsesAndKeepsTheAccounts(),
                        theFreshAccountIsMadeUsable(),
                        theAdministratorTidiesUp()),
                // One record per user-visible step. A redirect the application answers with is
                // part of the step a person performed, and its own record beside the step's would
                // make every rate computed from this log count the same act twice.
                jtlWriter(jtlDirectory, jtlFileName).withSubResults(false));
    }

    private DslSimpleController theManagerHandsOutWork() {
        Account manager = accounts.get(Role.MANAGER);
        Account worker = accounts.get(Role.WORKER);
        return simpleController("the manager hands out work",
                openTheLoginPage("the manager opens the login page")
                        .children(walkIdentity()),
                signIn("the manager signs in", manager)
                        .children(sessionToken(), userIdOf(worker)),
                listOfTasks("the manager looks at the work they handed out",
                        Scope.ASSIGNED_BY_ME, Sort.CREATED),
                httpSampler("the manager creates a task", url(SutSurface.TASKS))
                        .method("POST")
                        .param(SutSurface.TASK_TITLE_FIELD, variable(WALK_TAG_VARIABLE))
                        .param(SutSurface.TASK_DESCRIPTION_FIELD,
                                "the transport walk of the load rig")
                        .param(SutSurface.TASK_ASSIGNEE_FIELD, variable(WORKER_USER_ID_VARIABLE))
                        .param(SutSurface.TASK_DUE_DATE_FIELD, dateShiftedBy(DUE_WITHIN))
                        .param(SutSurface.SCOPE_FIELD, Scope.ASSIGNED_BY_ME.value())
                        .param(SutSurface.SORT_FIELD, Sort.CREATED.value())
                        .param(SutSurface.PAGE_FIELD, FIRST_PAGE)
                        .param(SutSurface.CSRF_FIELD, variable(CSRF_VARIABLE))
                        .children(
                                scriptLibraryAnswerShape(),
                                responseAssertion("the created task is in the answer")
                                        .containsSubstrings(variable(WALK_TAG_VARIABLE)),
                                taskIdentity()),
                openOneTask("the manager opens the task they created",
                        Scope.ASSIGNED_BY_ME, Sort.CREATED),
                signOut("the manager signs out"));
    }

    private DslSimpleController theWorkerActsOnTheTask() {
        Account worker = accounts.get(Role.WORKER);
        return simpleController("the worker works the task assigned to them",
                openTheLoginPage("the worker opens the login page"),
                signIn("the worker signs in", worker)
                        .children(sessionToken()),
                listOfTasks("the worker looks at their list", Scope.ASSIGNED_TO_ME, Sort.DUE_DATE),
                openOneTask("the worker opens the task", Scope.ASSIGNED_TO_ME, Sort.DUE_DATE),
                httpSampler("the worker reads the notes",
                        url(SutSurface.taskNotes(variable(TASK_ID_VARIABLE))))
                        .param(SutSurface.SCOPE_FIELD, Scope.ASSIGNED_TO_ME.value())
                        .param(SutSurface.SORT_FIELD, Sort.DUE_DATE.value())
                        .param(SutSurface.PAGE_FIELD, FIRST_PAGE)
                        .children(scriptLibraryAnswerShape()),
                httpSampler("the worker writes a note",
                        url(SutSurface.taskNotes(variable(TASK_ID_VARIABLE))))
                        .method("POST")
                        .param(SutSurface.NOTE_TEXT_FIELD,
                                "a remark of " + variable(WALK_TAG_VARIABLE))
                        .param(SutSurface.SCOPE_FIELD, Scope.ASSIGNED_TO_ME.value())
                        .param(SutSurface.SORT_FIELD, Sort.DUE_DATE.value())
                        .param(SutSurface.PAGE_FIELD, FIRST_PAGE)
                        .param(SutSurface.CSRF_FIELD, variable(CSRF_VARIABLE))
                        .children(scriptLibraryAnswerShape()),
                transition("the worker finishes the task", Transition.FINISH,
                        Scope.ASSIGNED_TO_ME, Sort.DUE_DATE)
                        .param(SutSurface.TRANSITION_MESSAGE_FIELD,
                                "finished by the transport walk"),
                signOut("the worker signs out"));
    }

    private DslSimpleController theManagerSettlesAndReports() {
        Account manager = accounts.get(Role.MANAGER);
        return simpleController("the manager settles the work and runs a report",
                openTheLoginPage("the manager opens the login page again"),
                signIn("the manager signs in again", manager)
                        .children(sessionToken()),
                openOneTask("the manager opens the finished task",
                        Scope.ASSIGNED_BY_ME, Sort.CREATED),
                transition("the manager approves the finished work", Transition.APPROVE,
                        Scope.ASSIGNED_BY_ME, Sort.CREATED),
                httpSampler("the manager opens the report form", url(SutSurface.REPORTS)),
                httpSampler("the manager runs a report on one worker", url(SutSurface.REPORTS))
                        .method("POST")
                        .param(SutSurface.REPORT_WORKER_FIELD, variable(WORKER_USER_ID_VARIABLE))
                        .param(SutSurface.REPORT_FROM_DATE_FIELD,
                                dateShiftedBy(REPORT_WINDOW_START))
                        .param(SutSurface.REPORT_TO_DATE_FIELD, today())
                        .param(SutSurface.CSRF_FIELD, variable(CSRF_VARIABLE)),
                signOut("the manager signs out again"));
    }

    private DslSimpleController theAdministratorBrowsesAndKeepsTheAccounts() {
        Account administrator = accounts.get(Role.ADMINISTRATOR);
        return simpleController("the administrator keeps the accounts and watches everything",
                openTheLoginPage("the administrator opens the login page"),
                signIn("the administrator signs in", administrator)
                        .children(sessionToken()),
                httpSampler("the administrator creates a user", url(SutSurface.USERS))
                        .method("POST")
                        .param(SutSurface.USERNAME_FIELD, variable(WALK_ACCOUNT_VARIABLE))
                        .param(SutSurface.ROLE_FIELD, Role.WORKER.token())
                        .param(SutSurface.PASSWORD_FIELD, provisionedPassword)
                        .param(SutSurface.CSRF_FIELD, variable(CSRF_VARIABLE))
                        .children(
                                responseAssertion("the account was created")
                                        .containsSubstrings(SutSurface.ACCEPTED_MARK),
                                accountIdentity()),
                listOfTasks("the administrator looks at every task", Scope.ALL, Sort.CREATED),
                openOneTask("the administrator opens the task", Scope.ALL, Sort.CREATED),
                transition("the administrator deletes the task", Transition.DELETE,
                        Scope.ALL, Sort.CREATED),
                signOut("the administrator signs out"));
    }

    /**
     * An account the administrator has just created reaches no screen of the application until its
     * holder has chosen a password, so the walk proves that step as well: without it a pool of
     * provisioned accounts would be a pool of accounts that cannot sign in.
     */
    private DslSimpleController theFreshAccountIsMadeUsable() {
        return simpleController("the fresh account chooses its password",
                openTheLoginPage("the fresh account opens the login page"),
                httpSampler("the fresh account signs in for the first time", url(SutSurface.LOGIN))
                        .method("POST")
                        .param(SutSurface.USERNAME_FIELD, variable(WALK_ACCOUNT_VARIABLE))
                        .param(SutSurface.PASSWORD_FIELD, provisionedPassword)
                        .param(SutSurface.CSRF_FIELD, variable(CSRF_VARIABLE))
                        .children(
                                responseAssertion("the fresh account is held at the password change")
                                        .containsSubstrings(SutSurface.CHANGE_PASSWORD),
                                sessionToken()),
                httpSampler("the fresh account chooses a password", url(SutSurface.CHANGE_PASSWORD))
                        .method("POST")
                        .param(SutSurface.CURRENT_PASSWORD_FIELD, provisionedPassword)
                        .param(SutSurface.NEW_PASSWORD_FIELD, provisionedPassword)
                        .param(SutSurface.CONFIRM_PASSWORD_FIELD, provisionedPassword)
                        .param(SutSurface.CSRF_FIELD, variable(CSRF_VARIABLE))
                        .children(responseAssertion("the password change was accepted")
                                .containsSubstrings(SutSurface.REFUSED_MARK)
                                .invertCheck()),
                signOut("the fresh account signs out"));
    }

    /**
     * The walk closes what it opened. An account left behind outlives the run that made it, and the
     * next run against the same database is refused the name it asks for - so the administrator
     * takes the provisioned account away again, after its holder has proven it usable.
     */
    private DslSimpleController theAdministratorTidiesUp() {
        Account administrator = accounts.get(Role.ADMINISTRATOR);
        return simpleController("the administrator takes the provisioned account away",
                openTheLoginPage("the administrator opens the login page again"),
                signIn("the administrator signs in again", administrator)
                        .children(sessionToken()),
                httpSampler("the administrator deletes the provisioned user",
                        url(SutSurface.userDeletion(variable(WALK_ACCOUNT_ID_VARIABLE))))
                        .method("POST")
                        .param(SutSurface.CSRF_FIELD, variable(CSRF_VARIABLE))
                        .children(scriptLibraryAnswerShape()),
                signOut("the administrator signs out again"));
    }

    private DslHttpSampler openTheLoginPage(String name) {
        return httpSampler(name, url(SutSurface.LOGIN))
                .children(sessionToken());
    }

    /**
     * A sign-in the application refuses is answered with the login page again, not with a status,
     * so the walk states what a sign-in that worked looks like instead of trusting the code.
     */
    private DslHttpSampler signIn(String name, Account account) {
        return httpSampler(name, url(SutSurface.LOGIN))
                .method("POST")
                .param(SutSurface.USERNAME_FIELD, account.username())
                .param(SutSurface.PASSWORD_FIELD, account.password())
                .param(SutSurface.CSRF_FIELD, variable(CSRF_VARIABLE))
                .children(responseAssertion("the sign-in reached an authenticated screen")
                        .containsSubstrings(SutSurface.AUTHENTICATED_MARK));
    }

    private DslHttpSampler signOut(String name) {
        return httpSampler(name, url(SutSurface.LOGOUT))
                .method("POST")
                .param(SutSurface.CSRF_FIELD, variable(CSRF_VARIABLE));
    }

    private DslHttpSampler listOfTasks(String name, Scope scope, Sort sort) {
        return httpSampler(name, url(SutSurface.TASKS))
                .param(SutSurface.SCOPE_FIELD, scope.value())
                .param(SutSurface.SORT_FIELD, sort.value())
                .param(SutSurface.PAGE_FIELD, FIRST_PAGE)
                .children(scriptLibraryAnswerShape());
    }

    private DslHttpSampler openOneTask(String name, Scope scope, Sort sort) {
        return httpSampler(name, url(SutSurface.task(variable(TASK_ID_VARIABLE))))
                .param(SutSurface.SCOPE_FIELD, scope.value())
                .param(SutSurface.SORT_FIELD, sort.value())
                .param(SutSurface.PAGE_FIELD, FIRST_PAGE)
                .children(scriptLibraryAnswerShape());
    }

    private DslHttpSampler transition(String name, Transition transition, Scope scope, Sort sort) {
        return httpSampler(name,
                url(SutSurface.taskTransition(variable(TASK_ID_VARIABLE), transition)))
                .method("POST")
                .param(SutSurface.SCOPE_FIELD, scope.value())
                .param(SutSurface.SORT_FIELD, sort.value())
                .param(SutSurface.PAGE_FIELD, FIRST_PAGE)
                .param(SutSurface.CSRF_FIELD, variable(CSRF_VARIABLE))
                .children(scriptLibraryAnswerShape());
    }

    /**
     * The header of DR 1, on the requests the page's script library would make and on no others:
     * the sign-in, the sign-out, the account forms and the report are ordinary form posts in the
     * interface, and a browser sends no such header on them.
     */
    private HttpHeaders scriptLibraryAnswerShape() {
        return httpHeaders()
                .header(AnswerShape.SCRIPT_LIBRARY_HEADER, AnswerShape.SCRIPT_LIBRARY_HEADER_VALUE);
    }

    private DslRegexExtractor sessionToken() {
        return Correlation.sessionToken(CSRF_VARIABLE).extractor();
    }

    /**
     * The identity of a task, read from the row the creation answered with and anchored on the
     * title only this virtual user used. Taking the first row of the answer instead would be a
     * lottery the moment the walk runs against a database that already holds tasks.
     */
    static DslRegexExtractor taskIdentity() {
        return Correlation.taskIdentity(TASK_ID_VARIABLE, variable(WALK_TAG_VARIABLE))
                .extractor();
    }

    /**
     * The identity of an account, read from the options the application itself offers rather than
     * assumed from the order a fresh database seeds its accounts in.
     */
    private DslRegexExtractor userIdOf(Account account) {
        return Correlation.userIdentity(WORKER_USER_ID_VARIABLE, account.username()).extractor();
    }

    /**
     * The identity of the account the walk provisioned, read from the row the creation answered
     * with and anchored on the name only this virtual user used.
     */
    private static DslRegexExtractor accountIdentity() {
        return Correlation.accountIdentity(WALK_ACCOUNT_ID_VARIABLE,
                variable(WALK_ACCOUNT_VARIABLE)).extractor();
    }

    /**
     * What makes this walk's artifacts its own: the thread and the iteration name the task and
     * the account, so that two virtual users never look for each other's row.
     */
    private DslJsr223PreProcessor walkIdentity() {
        String walkRunTag = runTag;
        return jsr223PreProcessor(script -> {
            String walk = walkRunTag + "-" + script.ctx.getThreadNum()
                    + "-" + script.vars.getIteration();
            script.vars.put(WALK_TAG_VARIABLE, "lr1-" + walk);
            script.vars.put(WALK_ACCOUNT_VARIABLE, "lr1u" + walk.replace("-", ""));
        });
    }

    private String url(String path) {
        return baseUrl + path;
    }

    static String variable(String name) {
        return "${" + name + "}";
    }

    private static String dateShiftedBy(String shift) {
        return "${__timeShift(yyyy-MM-dd,," + shift + ",,)}";
    }

    private static String today() {
        return "${__timeShift(yyyy-MM-dd,,,,)}";
    }
}
