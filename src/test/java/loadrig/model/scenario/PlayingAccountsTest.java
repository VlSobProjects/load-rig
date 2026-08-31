package loadrig.model.scenario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import loadrig.model.AccountPool;
import loadrig.model.Role;
import loadrig.model.profile.Campaign;
import loadrig.model.profile.CampaignLoader;
import loadrig.model.profile.LoadProfile;
import loadrig.model.profile.ProfileLoader;
import loadrig.registry.SessionRegistry;
import loadrig.registry.TransportContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Holds the one fact four readers share: which accounts a run is actually made of. The session
 * registry admits these, the census stocks these, and every step that names a person draws from
 * these - so a drift here would show up as work handed to people the run never signs in as, or as
 * a seat taken by somebody with nothing to do.
 */
class PlayingAccountsTest {

    private final LoadProfile day = ProfileLoader.load(Path.of("profiles", "day.json"));
    private final Campaign campaign = CampaignLoader.load(Path.of("profiles", "campaign.json"));

    @Test
    @DisplayName("a run plays its seats multiplied by the campaign's depth, in the pool's order")
    void thePlayersAreTheSeatsTimesTheDepth() {
        PlayingAccounts playing = PlayingAccounts.of(day, twoAccountsASeat());

        assertEquals(5, playing.seats(Role.WORKER));
        assertEquals(List.of("worker-01", "worker-02", "worker-03", "worker-04", "worker-05",
                        "worker-06", "worker-07", "worker-08", "worker-09", "worker-10"),
                playing.names(Role.WORKER),
                "five seats rotate through ten people, so the working set is twice the seats");
        assertEquals(4, playing.seats(Role.MANAGER),
                "the discussion takes up a manager session, so it counts against the managers");
        assertEquals(8, playing.names(Role.MANAGER).size());
        assertEquals(List.of("admin-01", "admin-02"), playing.names(Role.ADMINISTRATOR));
    }

    @Test
    void theSeatsAloneArePlayedWhenTheCampaignRotatesThroughOne() {
        PlayingAccounts playing = PlayingAccounts.of(day, oneAccountASeat());

        assertEquals(List.of("worker-01", "worker-02", "worker-03", "worker-04", "worker-05"),
                playing.names(Role.WORKER),
                "a depth of one is the narrow population the calibration probe measured, and it"
                        + " stays statable because the pool is finite");
    }

    @Test
    void theRegistryIsBuiltOverThePlayersWithTheirRoles() {
        PlayingAccounts playing = PlayingAccounts.of(day, twoAccountsASeat());

        List<AccountPool.Member> members = playing.members();

        assertEquals(20, members.size(), "ten workers, eight managers and two administrators");
        assertTrue(members.contains(new AccountPool.Member("worker-10", Role.WORKER)));
        assertTrue(members.stream().noneMatch(member -> member.username().equals("worker-11")),
                "an account outside the rotation is a stranger to the run: the census never"
                        + " stocked it, so a seat it took would starve the gates");
    }

    @Test
    void theRestOfThePoolIsNotPartOfTheRun() {
        PlayingAccounts playing = PlayingAccounts.of(day, campaign);

        assertTrue(playing.names(Role.WORKER).size() < AccountPool.WORKERS,
                "the pool is sized to the ceiling of the load model, and a day of ten users"
                        + " leaves most of it unplayed - work handed to those accounts would"
                        + " leave the run's own circulation");
    }

    @Test
    void aRotationThePoolCannotCarryIsRefusedWithTheConsequence() {
        IllegalArgumentException refusal = assertThrows(IllegalArgumentException.class,
                () -> PlayingAccounts.of(day, new Campaign(9, campaign.serviceLevels())));

        assertTrue(refusal.getMessage().contains("starve"),
                "the refusal states the consequence: " + refusal.getMessage());
        assertTrue(refusal.getMessage().contains("worker"),
                "and which population it is about: " + refusal.getMessage());
        assertTrue(refusal.getMessage().contains("45"),
                "and how many accounts the campaign asked to play: " + refusal.getMessage());
    }

    @Test
    void aPopulationThePoolCannotSeatIsRefusedEvenAtTheNarrowestRotation() {
        List<AccountPool.Member> oneWorkerShort = List.of(
                new AccountPool.Member("worker-01", Role.WORKER),
                new AccountPool.Member("manager-01", Role.MANAGER),
                new AccountPool.Member("manager-02", Role.MANAGER),
                new AccountPool.Member("manager-03", Role.MANAGER),
                new AccountPool.Member("manager-04", Role.MANAGER),
                new AccountPool.Member("admin-01", Role.ADMINISTRATOR));

        IllegalArgumentException refusal = assertThrows(IllegalArgumentException.class,
                () -> PlayingAccounts.of(day, oneAccountASeat(), oneWorkerShort));

        assertTrue(refusal.getMessage().contains("starve"),
                "the refusal states the consequence: " + refusal.getMessage());
        assertTrue(refusal.getMessage().contains("worker"),
                "and which population it is about: " + refusal.getMessage());
    }

    @Test
    @DisplayName("a seat given up goes to somebody who has not held one yet")
    void theRotationTurnsOverInsteadOfRecyclingTheSeats() {
        PlayingAccounts playing = PlayingAccounts.of(day, twoAccountsASeat());
        SessionRegistry registry = new SessionRegistry(playing.members());
        for (int seat = 0; seat < playing.seats(Role.WORKER); seat++) {
            signIn(registry);
        }

        registry.signedOut(registry.acquireOf("worker-01"));

        assertEquals("worker-06",
                registry.acquireToSignIn(Role.WORKER, username -> 0).username(),
                "with the backlogs equal, the account that has taken no act yet comes back, so"
                        + " the window is played by more people than it seats (DR-8)");
    }

    private static void signIn(SessionRegistry registry) {
        SessionRegistry.Lease lease = registry.acquireToSignIn(Role.WORKER, username -> 0);
        registry.attach(lease, new TransportContext(List.of(), "a-token"));
        registry.release(lease);
    }

    private Campaign oneAccountASeat() {
        return new Campaign(1, campaign.serviceLevels());
    }

    private Campaign twoAccountsASeat() {
        return new Campaign(2, campaign.serviceLevels());
    }
}
