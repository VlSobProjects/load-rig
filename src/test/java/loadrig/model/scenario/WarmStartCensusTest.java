package loadrig.model.scenario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import loadrig.model.profile.LoadProfile;
import loadrig.model.profile.ProfileLoader;
import loadrig.model.profile.ScenarioName;
import loadrig.model.profile.StepMix;
import loadrig.registry.TaskRegistry;
import loadrig.registry.TaskStatus;
import loadrig.registry.TransitionTable.ActingParty;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Holds the census against the day profile's own arithmetic, and against the two properties that
 * make it worth computing at all: it degenerates to nothing on a stand that already holds the
 * population, and it scales with the profile instead of being restated for every variant.
 */
class WarmStartCensusTest {

    private final WarmStartCensus day =
            censusOf(ProfileLoader.load(Path.of("profiles", "day.json")));

    @Test
    @DisplayName("a bucket is one account, one relation and one status - never a total")
    void theCensusIsStatedPerAccountAndStatus() {
        assertEquals(21, day.buckets().size(),
                "five workers hold their open work; four manager accounts - the manager"
                        + " population and the discussion's - hold four statuses each");
        assertEquals(11, stockOf("worker-01", TaskStatus.OPEN),
                "a worker draws 1.68 open tasks a minute and is fed 1.44: the window drains two"
                        + " and a half, and two deviations of seventeen draws add eight more");
        assertEquals(7, stockOf("manager-01", TaskStatus.COMPLETED),
                "finished work arrives faster than it is approved, so the stock is slack alone");
        assertEquals(4, stockOf("manager-01", TaskStatus.REFUSED));
        assertEquals(3, stockOf("manager-01", TaskStatus.RETURNED));
        assertEquals(3, stockOf("manager-01", TaskStatus.APPROVED));
        assertEquals(123, day.tasks());
    }

    @Test
    void theBucketsAreTheAccountsTheRunSignsInto() {
        Set<String> workers = day.buckets().stream()
                .filter(bucket -> bucket.relation() == ActingParty.THE_ASSIGNEE)
                .map(WarmStartCensus.Bucket::actor)
                .collect(Collectors.toSet());
        Set<String> managers = day.buckets().stream()
                .filter(bucket -> bucket.relation() == ActingParty.THE_CREATOR)
                .map(WarmStartCensus.Bucket::actor)
                .collect(Collectors.toSet());

        assertEquals(Set.of("worker-01", "worker-02", "worker-03", "worker-04", "worker-05"),
                workers);
        assertEquals(Set.of("manager-01", "manager-02", "manager-03", "manager-04"), managers,
                "the discussion takes up a manager session too, so its account is stocked as well");
    }

    @Test
    void anEmptyStandOwesTheWholeCensusInTheSharesTheRunMaintains() {
        List<WarmStartCensus.Wanted> wanted = day.shortfall(List.of());

        assertEquals(day.tasks(), wanted.size());
        long open = wanted.stream()
                .filter(task -> task.status() == TaskStatus.OPEN).count();
        long hot = wanted.stream().filter(WarmStartCensus.Wanted::hot).count();
        assertEquals(55, open, "five workers hold eleven open tasks each");
        assertEquals(30, hot,
                "the census stocks the hot set in the share the creations use - every second open"
                        + " task, rounded within each worker's own stock - or the skew has nothing"
                        + " to choose between");
        assertTrue(wanted.stream().noneMatch(task -> task.hot()
                        && task.status() != TaskStatus.OPEN),
                "only an open task can be hot; a settled one is no part of the hot set");
    }

    @Test
    void aStandThatAlreadyHoldsThePopulationOwesNothing() {
        List<TaskRegistry.TaskFacts> alreadyThere = new ArrayList<>();
        int id = 0;
        for (WarmStartCensus.Wanted wanted : day.shortfall(List.of())) {
            alreadyThere.add(new TaskRegistry.TaskFacts(String.valueOf(++id), wanted.status(),
                    wanted.creator(), wanted.assignee(), wanted.hot()));
        }

        assertEquals(List.of(), day.shortfall(alreadyThere),
                "read on a seeded stand, created on an empty one, and the same code for both:"
                        + " this is what lets the seeding item land without a rewrite");
    }

    @Test
    void oneTaskSatisfiesOneBucketOnly() {
        TaskRegistry.TaskFacts one = new TaskRegistry.TaskFacts("1", TaskStatus.OPEN,
                "manager-01", "worker-01", true);

        assertEquals(day.tasks() - 1, day.shortfall(List.of(one)).size());
    }

    @Test
    void theCensusScalesWithThePopulationInsteadOfBeingRestated() {
        WarmStartCensus twiceThePeople = censusOf(profileOf(19, 10, 6, 1, 2, 10));

        assertEquals(2 * day.tasks(), twiceThePeople.tasks(),
                "twice the workers and twice the manager sessions each do the same work as"
                        + " before, so the census doubles without anybody restating it - one"
                        + " administrator serves either size, and the deletion is no bucket");
    }

    @Test
    void aCensusNoWarmStartCouldBringAboutIsRefused() {
        WarmStartCensus overTheCeiling = censusOf(profileOf(10, 5, 3, 1, 1, 5000));

        IllegalArgumentException refusal =
                assertThrows(IllegalArgumentException.class, overTheCeiling::check);

        assertTrue(refusal.getMessage().contains("seeded history"),
                "a population counted in thousands is a seeding job and the run says so before it"
                        + " spends a window: " + refusal.getMessage());
    }

    private static WarmStartCensus censusOf(LoadProfile profile) {
        return WarmStartCensus.of(profile, SeatedAccounts.of(profile));
    }

    private int stockOf(String actor, TaskStatus status) {
        return day.buckets().stream()
                .filter(bucket -> bucket.actor().equals(actor) && bucket.status() == status)
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "the census states no bucket of " + status + " for " + actor))
                .tasks();
    }

    /** The day profile's shape at another size, for the properties that are about scale. */
    private static LoadProfile profileOf(int users, int workers, int managers, int administrators,
            int discussion, int windowMinutes) {
        return new LoadProfile("scaled", users, 30, windowMinutes,
                new LoadProfile.ThinkTime(3, 7),
                new StepMix(37, 22, 13, 12, 10, 5, 1),
                new LoadProfile.HotSetSkew(70, 80),
                Map.of(ScenarioName.WORKER, workers,
                        ScenarioName.MANAGER, managers,
                        ScenarioName.ADMINISTRATOR, administrators,
                        ScenarioName.DISCUSSION, discussion));
    }
}
