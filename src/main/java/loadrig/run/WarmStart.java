package loadrig.run;

import static loadrig.run.StandSession.form;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import loadrig.model.AccountPool;
import loadrig.model.Correlation;
import loadrig.model.HotWindow;
import loadrig.model.Role;
import loadrig.model.SutSurface;
import loadrig.model.SutSurface.Scope;
import loadrig.model.SutSurface.Sort;
import loadrig.model.SutSurface.Transition;
import loadrig.model.TaskListReading;
import loadrig.model.scenario.WarmStartCensus;
import loadrig.registry.TaskRegistry;
import loadrig.registry.TaskStatus;
import loadrig.registry.UserDirectory;

/**
 * Brings a stand to the census the profile needs, before the window opens.
 *
 * <p>It reads what the stand already holds from the list page ordered by due date - the only
 * inventory the application offers, and the order that puts the hot end first - registers those
 * tasks as the run's own facts, and then creates and drives only the difference. On an empty stand
 * it is pure creation, on a stand that already holds the population it is pure reading, and when
 * the SUT project's seeding item lands it needs no rewrite.
 *
 * <p>Like the pool provisioning it is stand administration and not load (DR-6): it runs on the
 * administration transport, writes no samples, and the result log holds the window and nothing
 * else. It fails loudly - a stand it cannot read, an account it cannot sign into, a refused
 * creation - because a run that starts on a population it only half established measures the
 * script and not the system.
 */
final class WarmStart {

    /**
     * How far into a stand's history the reading goes. Twenty-five pages is more than any census
     * asks for; a stand whose first pages hold none of what the profile needs is answered by
     * creating the difference, not by reading its whole history one page at a time.
     */
    static final int PAGES_READ_AT_MOST = 25;

    private static final String DESCRIPTION = "a task of the warm start";
    private static final String QUESTION = "what should be done about the open points?";
    private static final String REFUSAL_REASON = "cannot be done as assigned";

    /** What the warm start did, for the run report and the capture's description. */
    record Result(int tasksOnTheStand, int read, int created, int census) {
    }

    private final String baseUrl;
    private final String poolPassword;
    private final String runTag;
    private final WarmStartCensus census;
    private final TaskRegistry tasks;
    private final UserDirectory directory;
    private final Map<String, StandSession> sessions = new LinkedHashMap<>();
    private final AtomicLong marks = new AtomicLong();

    WarmStart(String baseUrl, String poolPassword, String runTag, WarmStartCensus census,
            TaskRegistry tasks, UserDirectory directory) {
        this.baseUrl = baseUrl;
        this.poolPassword = poolPassword;
        this.runTag = runTag;
        this.census = census;
        this.tasks = tasks;
        this.directory = directory;
    }

    Result bringAbout() throws IOException, InterruptedException {
        List<TaskRegistry.TaskFacts> known = new ArrayList<>();
        int onTheStand = read(known);
        harvestTheDirectory();
        List<WarmStartCensus.Wanted> wanted = census.shortfall(known);
        for (WarmStartCensus.Wanted task : wanted) {
            bringAbout(task);
        }
        signOutEverySession();
        return new Result(onTheStand, known.size(), wanted.size(), census.tasks());
    }

    /**
     * Reads the stand into the registry, page by page in due-date order, and stops as soon as what
     * was read satisfies the census. The number of tasks the stand holds is the list's own count,
     * measured here and published by the capture.
     */
    private int read(List<TaskRegistry.TaskFacts> known) throws IOException, InterruptedException {
        StandSession administrator = signedIn(administratorOfThePool());
        LocalDate today = LocalDate.now();
        Set<String> seen = new HashSet<>();
        int onTheStand = 0;
        int pages = 1;
        for (int page = 1; page <= Math.min(pages, PAGES_READ_AT_MOST); page++) {
            String answer = administrator.get(list(page));
            List<TaskListReading.Row> rows = TaskListReading.rowsOf(answer);
            TaskListReading.Paging paging = TaskListReading.pagingOf(answer)
                    .orElseGet(() -> emptyStand(rows));
            if (page == 1) {
                onTheStand = paging.tasksInTotal();
            }
            pages = paging.pages();
            for (TaskListReading.Row row : rows) {
                // Two pages of one listing may show one task when several share a due date and
                // the stand breaks the tie its own way; reading it twice would say the stand
                // holds two.
                if (!seen.add(row.taskId())) {
                    continue;
                }
                TaskStatus status = TaskStatus.behind(row.status());
                TaskRegistry.TaskFacts facts = new TaskRegistry.TaskFacts(row.taskId(), status,
                        row.creator(), row.assignee(),
                        status == TaskStatus.OPEN && row.dueSoon(today));
                tasks.register(facts);
                known.add(facts);
            }
            if (census.shortfall(known).isEmpty()) {
                break;
            }
        }
        return onTheStand;
    }

    /**
     * Creates the task and drives it to the status the census wants it in, through the same
     * screens a person uses: the creator makes it, its assignee finishes, returns or refuses it,
     * and the creator approves what was finished.
     */
    private void bringAbout(WarmStartCensus.Wanted wanted)
            throws IOException, InterruptedException {
        String taskId = create(wanted);
        switch (wanted.status()) {
            case OPEN -> {
            }
            case COMPLETED -> move(wanted.assignee(), taskId, Transition.FINISH, null);
            case RETURNED -> move(wanted.assignee(), taskId,
                    Transition.RETURN_WITH_A_QUESTION, QUESTION);
            case REFUSED -> move(wanted.assignee(), taskId, Transition.REFUSE, REFUSAL_REASON);
            case APPROVED -> {
                move(wanted.assignee(), taskId, Transition.FINISH, null);
                move(wanted.creator(), taskId, Transition.APPROVE, null);
            }
            case ACKNOWLEDGED -> {
                move(wanted.assignee(), taskId, Transition.REFUSE, REFUSAL_REASON);
                move(wanted.creator(), taskId, Transition.ACCEPT_THE_REFUSAL, null);
            }
        }
        tasks.register(new TaskRegistry.TaskFacts(taskId, wanted.status(), wanted.creator(),
                wanted.assignee(), wanted.hot()));
    }

    private String create(WarmStartCensus.Wanted wanted) throws IOException, InterruptedException {
        StandSession creator = signedIn(wanted.creator());
        String title = "warm-" + runTag + "-t" + marks.incrementAndGet();
        String assigneeId = directory.idOf(wanted.assignee())
                .orElseThrow(() -> refusal("the identity of " + wanted.assignee()
                        + " was offered by no screen, so no task can be assigned to it"));
        String answer = creator.postFromTheScriptLibrary(SutSurface.TASKS, form(
                SutSurface.TASK_TITLE_FIELD, title,
                SutSurface.TASK_DESCRIPTION_FIELD, DESCRIPTION,
                SutSurface.TASK_ASSIGNEE_FIELD, assigneeId,
                SutSurface.TASK_DUE_DATE_FIELD, dueDate(wanted.hot()),
                SutSurface.CSRF_FIELD, tokenOf(creator, wanted.creator())));
        if (answer.contains(SutSurface.REFUSED_MARK)) {
            throw refusal("the creation of " + title + " by " + wanted.creator() + " was refused");
        }
        return Correlation.taskIdentity("created", title).read(answer)
                .orElseThrow(() -> refusal("the task " + title + " is not in the answer that"
                        + " created it, so the warm start cannot act on it"));
    }

    private void move(String actor, String taskId, Transition transition, String message)
            throws IOException, InterruptedException {
        StandSession session = signedIn(actor);
        Map<String, String> fields = form(SutSurface.CSRF_FIELD, tokenOf(session, actor));
        if (message != null) {
            fields.put(SutSurface.TRANSITION_MESSAGE_FIELD, message);
        }
        String answer = session.postFromTheScriptLibrary(
                SutSurface.taskTransition(taskId, transition), fields);
        if (answer.contains(SutSurface.REFUSED_MARK)) {
            throw refusal("the " + transition + " of task " + taskId + " by " + actor
                    + " was refused; the warm start drives the transitions the table permits, so a"
                    + " refusal here is the rig's own defect");
        }
    }

    /**
     * The identities the creations name their assignees by, read from the options the application
     * itself offers - the same harvest the load makes, made once before the window so that no
     * step of the run has to starve while waiting for an answer to offer them.
     */
    private void harvestTheDirectory() throws IOException, InterruptedException {
        StandSession manager = signedIn(firstOfThePool(Role.MANAGER));
        String answer = manager.get(list(1));
        for (AccountPool.Member member : AccountPool.members()) {
            Correlation.userIdentity("directory", member.username()).read(answer)
                    .ifPresent(id -> directory.learned(member.username(), id));
        }
    }

    private StandSession signedIn(String username) throws IOException, InterruptedException {
        StandSession held = sessions.get(username);
        if (held != null) {
            return held;
        }
        StandSession session = new StandSession(baseUrl);
        session.get(SutSurface.LOGIN);
        String answer = session.post(SutSurface.LOGIN, form(
                SutSurface.USERNAME_FIELD, username,
                SutSurface.PASSWORD_FIELD, poolPassword,
                SutSurface.CSRF_FIELD, session.token("the login page")));
        if (PoolProvisioner.classifySignIn(answer) != PoolProvisioner.SignInAnswer.SIGNED_IN) {
            throw refusal("the pool account " + username + " does not sign in with the configured"
                    + " password; the warm start acts as the accounts the run signs in with, so"
                    + " the pool has to be provisioned before a window is spent");
        }
        sessions.put(username, session);
        return session;
    }

    private void signOutEverySession() throws IOException, InterruptedException {
        for (Map.Entry<String, StandSession> held : sessions.entrySet()) {
            StandSession session = held.getValue();
            session.post(SutSurface.LOGOUT,
                    form(SutSurface.CSRF_FIELD, tokenOf(session, held.getKey())));
        }
        sessions.clear();
    }

    /**
     * The token the next request of this session carries: the one the last answer rendered, or the
     * one of a list loaded for the purpose. A person acts from the screen they are on, and an
     * answer that rendered no form - a fragment of a table alone - leaves them without one.
     */
    private String tokenOf(StandSession session, String actor)
            throws IOException, InterruptedException {
        Optional<String> rendered = session.tokenIfAny();
        if (rendered.isPresent()) {
            return rendered.get();
        }
        session.get(list(1));
        return session.token("the list " + actor + " is on");
    }

    /** The whole stand, oldest due date first: the hot end of the history comes on page one. */
    private static String list(int page) {
        return SutSurface.TASKS
                + "?" + SutSurface.SCOPE_FIELD + "=" + Scope.ALL.value()
                + "&" + SutSurface.SORT_FIELD + "=" + Sort.DUE_DATE.value()
                + "&" + SutSurface.PAGE_FIELD + "=" + page;
    }

    private static String dueDate(boolean hot) {
        return LocalDate.now()
                .plus(Period.parse(hot ? HotWindow.DUE_INSIDE : HotWindow.DUE_OUTSIDE))
                .toString();
    }

    private static TaskListReading.Paging emptyStand(List<TaskListReading.Row> rows) {
        if (!rows.isEmpty()) {
            throw refusal("the list answers with rows and no paging state; it is not the list page"
                    + " the rig reads a stand from, and a stand the rig misreads cannot be acted"
                    + " on");
        }
        return new TaskListReading.Paging(1, 1, 0);
    }

    private static String administratorOfThePool() {
        return firstOfThePool(Role.ADMINISTRATOR);
    }

    private static String firstOfThePool(Role role) {
        return AccountPool.members().stream()
                .filter(member -> member.role() == role)
                .map(AccountPool.Member::username)
                .findFirst()
                .orElseThrow(() -> refusal("the pool holds no account of role " + role));
    }

    private static IllegalStateException refusal(String message) {
        return new IllegalStateException(message);
    }
}
