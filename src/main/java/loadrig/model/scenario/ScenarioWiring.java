package loadrig.model.scenario;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import loadrig.model.profile.LoadProfile;
import loadrig.registry.SessionRegistry;
import loadrig.registry.TaskRegistry;
import loadrig.registry.UserDirectory;

/**
 * Everything a scenario acts through at run time: the registries of the injector JVM, the
 * user directory, the three ledgers of the run, the skew of the loaded profile, and the run's own
 * identity. One wiring serves one run; the scenarios share it, which is what makes the
 * discussion's several-people-on-one-task and the settle-your-own-creation relations possible
 * at all.
 */
public final class ScenarioWiring {

    private final SessionRegistry sessions;
    private final PlayingAccounts playing;
    private final TaskRegistry tasks;
    private final UserDirectory directory;
    private final StarvationLedger starvation;
    private final RefusalLedger refusals;
    private final ServiceLevelLedger serviceLevels;
    private final LoadProfile.HotSetSkew skew;
    private final String runTag;
    private final String poolPassword;
    private final AtomicLong taskMarks = new AtomicLong();

    public ScenarioWiring(SessionRegistry sessions, PlayingAccounts playing, TaskRegistry tasks,
            UserDirectory directory, StarvationLedger starvation, RefusalLedger refusals,
            ServiceLevelLedger serviceLevels, LoadProfile.HotSetSkew skew, String runTag,
            String poolPassword) {
        this.sessions = Objects.requireNonNull(sessions, "sessions");
        this.playing = Objects.requireNonNull(playing, "playing");
        this.tasks = Objects.requireNonNull(tasks, "tasks");
        this.directory = Objects.requireNonNull(directory, "directory");
        this.starvation = Objects.requireNonNull(starvation, "starvation");
        this.refusals = Objects.requireNonNull(refusals, "refusals");
        this.serviceLevels = Objects.requireNonNull(serviceLevels, "serviceLevels");
        this.skew = Objects.requireNonNull(skew, "skew");
        this.runTag = Objects.requireNonNull(runTag, "runTag");
        this.poolPassword = Objects.requireNonNull(poolPassword, "poolPassword");
    }

    public SessionRegistry sessions() {
        return sessions;
    }

    /**
     * The accounts this run plays over its window - wider than the seats it holds at once, because
     * sessions end and the next account in is chosen by state (DR-8). A step that names a person
     * draws from here and not from the whole pool: the pool is sized to the ceiling of the load
     * model, and work handed to an account the run never signs in as leaves its own circulation.
     */
    public PlayingAccounts playing() {
        return playing;
    }

    public TaskRegistry tasks() {
        return tasks;
    }

    public UserDirectory directory() {
        return directory;
    }

    public StarvationLedger starvation() {
        return starvation;
    }

    public RefusalLedger refusals() {
        return refusals;
    }

    /** What the run's own people waited, timed into the bands the campaign fixed levels for. */
    public ServiceLevelLedger serviceLevels() {
        return serviceLevels;
    }

    public LoadProfile.HotSetSkew skew() {
        return skew;
    }

    public String runTag() {
        return runTag;
    }

    public String poolPassword() {
        return poolPassword;
    }

    /**
     * The title of the next created task: unique within the run and marked with the run's tag,
     * so two runs against one database never answer each other's questions, and the identity of
     * a created task is read from the row carrying exactly this title.
     */
    public String nextTaskTitle() {
        return "lr3-" + runTag + "-t" + taskMarks.incrementAndGet();
    }
}
