package loadrig.model.scenario;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import loadrig.model.Role;
import loadrig.model.SutSurface.Transition;
import loadrig.model.profile.LoadProfile;
import loadrig.model.profile.ScenarioName;
import loadrig.registry.TaskRegistry;
import loadrig.registry.TaskStatus;
import loadrig.registry.TransitionTable;
import loadrig.registry.TransitionTable.ActingParty;

/**
 * The population a profile needs before its window opens, computed rather than assumed.
 *
 * <p>It is stated per bucket and never as a total, because a total does not say what starves: a
 * worker with no open task assigned to them skips every move they own however many tasks the stand
 * holds. A bucket is one acting account, one relation to the task - its assignee or its creator -
 * and one status; its stock covers the drain of the whole window wherever the flows into the
 * bucket do not match the draws out of it, plus the slack that the randomness of the draw asks
 * for.
 *
 * <p>The flows are {@link ScenarioDemand}'s, so the census is computed from the same weights the
 * plan is built from. The deletion is deliberately outside the census: it draws from every status
 * of every task, and the only stand it starves on is one with no tasks at all.
 *
 * <p>The buckets are the accounts the run <em>plays</em> and not the ones it seats at once
 * (DR-8): a session ends at a stated share and the account that signs in next is chosen by state,
 * so every player takes its turn in the seats over the window and each of them must hold work
 * when it does. A demand is therefore spread over the whole rotation, which lowers what any one
 * account is drawn from per minute and raises the census by roughly the rotation's depth - the
 * cost the decision names, and the whole point of it: the working set the stand must hold is as
 * wide as the people who play, not as the seats.
 */
public final class WarmStartCensus {

    /**
     * How much slack a bucket carries against the randomness of the draw, in standard deviations
     * of what the window draws from it. Draws and feeds arrive at random moments, so a bucket
     * whose stock covers only the average deficit empties now and then anyway - measured: a stock
     * that covered the drain exactly still left a fifth of the transitions starved. The spread of
     * a count of random arrivals grows as its square root, so the slack does too, and two
     * deviations is what makes an empty bucket rare rather than occasional.
     */
    static final double COVER_DEVIATIONS = 2.0;

    /** A bucket a step draws from at all holds more than one task, or its first pick empties it. */
    static final int MINIMUM_STOCK = 2;

    /**
     * Beyond this a census is not a warm start. A profile whose population must be counted in
     * thousands is asking for a seeded history - the SUT project's own item - and the run says so
     * before it spends a stand window creating one through the screens.
     */
    static final int CEILING = 1000;

    /**
     * One stock the census states, with the flows it was computed from: what the account is drawn
     * from per minute averaged over the window, what it is drawn from per minute while it holds a
     * seat, and what flows into it.
     */
    public record Bucket(String actor, ActingParty relation, TaskStatus status,
            double drawnPerMinute, double seatedDrawnPerMinute, double fedPerMinute, int tasks) {
    }

    /** One task the warm start must bring about, because the stand does not hold it yet. */
    public record Wanted(String creator, String assignee, TaskStatus status, boolean hot) {
    }

    private final List<Bucket> buckets;
    private final List<String> managers;
    private final List<String> workers;

    private WarmStartCensus(List<Bucket> buckets, List<String> managers, List<String> workers) {
        this.buckets = List.copyOf(buckets);
        this.managers = List.copyOf(managers);
        this.workers = List.copyOf(workers);
    }

    /** The census the profile implies, over the accounts the run plays across its window. */
    public static WarmStartCensus of(LoadProfile profile, PlayingAccounts playing) {
        ScenarioDemand demand = ScenarioDemand.of(profile);
        Map<Transition, Role> actingRoles = actingRoles();

        Map<String, Bucket> byKey = new LinkedHashMap<>();
        for (Transition transition : Transition.values()) {
            if (transition == Transition.DELETE || demand.rateOf(transition) == 0) {
                continue;
            }
            List<String> actors = playing.names(actingRoles.get(transition));
            if (actors.isEmpty()) {
                continue;
            }
            ActingParty relation = TransitionTable.actingParty(transition);
            TaskStatus status = drawnFrom(transition);
            double drawnPerActor = demand.rateOf(transition) / actors.size();
            double drawnPerSeat =
                    demand.rateOf(transition) / playing.seats(actingRoles.get(transition));
            for (String actor : actors) {
                String key = actor + "/" + relation + "/" + status;
                Bucket held = byKey.get(key);
                double drawn = drawnPerActor + (held == null ? 0 : held.drawnPerMinute());
                double seated = drawnPerSeat + (held == null ? 0 : held.seatedDrawnPerMinute());
                byKey.put(key, new Bucket(actor, relation, status, drawn, seated, 0, 0));
            }
        }

        List<Bucket> buckets = new ArrayList<>();
        double stintMinutes = Math.min(demand.seatStintMinutes(), profile.steadyWindowMinutes());
        for (Bucket bucket : byKey.values()) {
            double fed = fedPerMinute(demand, bucket, playing);
            buckets.add(new Bucket(bucket.actor(), bucket.relation(), bucket.status(),
                    bucket.drawnPerMinute(), bucket.seatedDrawnPerMinute(), fed,
                    stock(bucket.drawnPerMinute(), bucket.seatedDrawnPerMinute(), fed,
                            profile.steadyWindowMinutes(), stintMinutes)));
        }
        return new WarmStartCensus(buckets, playing.names(Role.MANAGER),
                playing.names(Role.WORKER));
    }

    public List<Bucket> buckets() {
        return buckets;
    }

    /** The whole population the census asks for, over every bucket. */
    public int tasks() {
        return buckets.stream().mapToInt(Bucket::tasks).sum();
    }

    /**
     * Refuses a census no warm start may bring about, before a stand window is spent on it: one
     * counted in thousands, and one whose tasks nobody could be assigned.
     */
    public void check() {
        if (buckets.isEmpty()) {
            return;
        }
        if (workers.isEmpty()) {
            throw new IllegalArgumentException("the profile plays no worker account, and every"
                    + " task the census asks for is assigned to one; a population that moves work"
                    + " without anybody to do it is not a load model");
        }
        if (tasks() > CEILING) {
            throw new IllegalArgumentException(String.format(Locale.ENGLISH,
                    "the census of this profile is %d tasks over %d buckets, past the %d a warm"
                            + " start may bring about; a population of this size is a seeded"
                            + " history and belongs to the seeding item, not to the minutes"
                            + " before a window",
                    tasks(), buckets.size(), CEILING));
        }
    }

    /**
     * What the stand still owes the census, given what it was read to hold already. It is the
     * whole census on an empty stand and nothing at all on one that already holds the population,
     * which is what lets the same code serve both and lets the seeding item land without a
     * rewrite.
     *
     * <p>A task counts for one bucket only: the buckets of a worker and of a manager never name
     * the same status, so nothing is claimed twice, and the guard is kept because a task that
     * satisfied two buckets would leave one of them empty at the first pick.
     */
    public List<Wanted> shortfall(Collection<TaskRegistry.TaskFacts> known) {
        Set<String> claimed = new HashSet<>();
        List<Wanted> wanted = new ArrayList<>();
        int creatorTurn = 0;
        int assigneeTurn = 0;
        for (Bucket bucket : buckets) {
            int held = 0;
            for (TaskRegistry.TaskFacts facts : known) {
                if (held == bucket.tasks()) {
                    break;
                }
                if (!claimed.contains(facts.taskId()) && matches(bucket, facts)) {
                    claimed.add(facts.taskId());
                    held++;
                }
            }
            int missing = bucket.tasks() - held;
            int hot = bucket.status() == TaskStatus.OPEN
                    ? (int) Math.round(missing * ManagerScenario.CREATED_DUE_SOON_PERCENT / 100.0)
                    : 0;
            for (int index = 0; index < missing; index++) {
                String creator = bucket.relation() == ActingParty.THE_CREATOR
                        ? bucket.actor()
                        : managers.get(creatorTurn++ % managers.size());
                String assignee = bucket.relation() == ActingParty.THE_ASSIGNEE
                        ? bucket.actor()
                        : workers.get(assigneeTurn++ % workers.size());
                wanted.add(new Wanted(creator, assignee, bucket.status(), index < hot));
            }
        }
        return List.copyOf(wanted);
    }

    /** The census as the run report and the capture's description state it, bucket by bucket. */
    public String statement() {
        StringBuilder statement = new StringBuilder();
        for (Bucket bucket : buckets) {
            statement.append(String.format(Locale.ENGLISH,
                    "    %s as %s holds %d %s (drawn %.2f, %.2f while seated, fed %.2f per"
                            + " minute)%n",
                    bucket.actor(),
                    bucket.relation() == ActingParty.THE_ASSIGNEE ? "assignee" : "creator",
                    bucket.tasks(), bucket.status(), bucket.drawnPerMinute(),
                    bucket.seatedDrawnPerMinute(), bucket.fedPerMinute()));
        }
        return statement.toString();
    }

    private static boolean matches(Bucket bucket, TaskRegistry.TaskFacts facts) {
        if (facts.status() != bucket.status()) {
            return false;
        }
        return bucket.relation() == ActingParty.THE_ASSIGNEE
                ? bucket.actor().equals(facts.assignee())
                : bucket.actor().equals(facts.creator());
    }

    /**
     * The status the transition is stocked in. A transition that may be applied from several
     * statuses is stocked in the first of them by the statuses' own order: one of them is enough
     * for the draw to find a task, and taking the first keeps the census reproducible.
     */
    private static TaskStatus drawnFrom(Transition transition) {
        return TransitionTable.movesFrom(transition).stream()
                .min(Comparator.comparingInt(Enum::ordinal))
                .orElseThrow(() -> new IllegalStateException(
                        transition + " is applied from no status at all"));
    }

    /**
     * What flows into the bucket per minute: every transition that lands a task in its status, and
     * the creations when the status is the one a creation lands in, divided over the accounts the
     * landed tasks spread across - the assignee of a task is drawn among the workers the run
     * plays and its creator among the managers it plays.
     */
    private static double fedPerMinute(ScenarioDemand demand, Bucket bucket,
            PlayingAccounts playing) {
        double into = 0;
        for (Transition transition : Transition.values()) {
            if (transition == Transition.DELETE) {
                continue;
            }
            if (TransitionTable.movesTo(transition) == bucket.status()) {
                into += demand.rateOf(transition);
            }
        }
        if (bucket.status() == TaskStatus.OPEN) {
            into += demand.creationsPerMinute();
        }
        List<String> spread = bucket.relation() == ActingParty.THE_ASSIGNEE
                ? playing.names(Role.WORKER)
                : playing.names(Role.MANAGER);
        return spread.isEmpty() ? 0 : into / spread.size();
    }

    /**
     * What one bucket holds when the window opens. Two horizons ask for a stock and the larger
     * wins, because a bucket that empties on either of them starves the gate it feeds.
     *
     * <p>Over the <strong>window</strong> the account is drawn from at the rate its share of the
     * rotation implies, and the stock covers whatever the feed does not, plus the slack the
     * randomness of the draw asks for.
     *
     * <p>Over one <strong>stint</strong> - the time the account holds a seat before it gives the
     * session up - it is drawn from at the seat's own rate, because the rotation does not turn
     * over between two steps, while the feed arrives no faster than before: the work landing on an
     * account is spread over every player whether that player is seated or not. A bucket stocked
     * for the window's average alone therefore empties inside a stint. Measured, on the first run
     * played over a rotation three accounts deep: a worker stocked for four open tasks was drawn
     * from seven times in one stint, and the starvation ledger recorded the difference as skipped
     * transitions - the mix drifting for a reason the census could have foreseen.
     *
     * <p>The two coincide when a seat rotates through one account, which is why the figures of a
     * rotation that deep are the ones they always were.
     */
    private static int stock(double drawnPerMinute, double seatedDrawnPerMinute,
            double fedPerMinute, int windowMinutes, double stintMinutes) {
        return Math.max(MINIMUM_STOCK, (int) Math.ceil(Math.max(
                cover(drawnPerMinute, fedPerMinute, windowMinutes),
                cover(seatedDrawnPerMinute, fedPerMinute, stintMinutes))));
    }

    /** The drain of one horizon wherever the feed does not match the draw, plus its slack. */
    private static double cover(double drawnPerMinute, double fedPerMinute, double minutes) {
        return Math.max(0, drawnPerMinute - fedPerMinute) * minutes
                + COVER_DEVIATIONS * Math.sqrt(drawnPerMinute * minutes);
    }

    /** Whose accounts a transition's demand lands on: the role of the scenarios that apply it. */
    private static Map<Transition, Role> actingRoles() {
        Map<ScenarioName, Role> roles = Scenarios.sessionRoles();
        Map<Transition, Role> acting = new LinkedHashMap<>();
        Scenarios.transitionWeights().forEach((name, table) -> table.keySet().forEach(transition -> {
            Role role = roles.get(name);
            Role held = acting.putIfAbsent(transition, role);
            if (held != null && held != role) {
                throw new IllegalStateException("the transition " + transition + " is applied by"
                        + " both a " + held + " and a " + role + " scenario; the census cannot say"
                        + " whose accounts its demand lands on");
            }
        }));
        return acting;
    }
}
