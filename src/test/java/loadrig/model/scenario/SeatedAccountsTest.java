package loadrig.model.scenario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import loadrig.model.AccountPool;
import loadrig.model.Role;
import loadrig.model.profile.LoadProfile;
import loadrig.model.profile.ProfileLoader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Holds the one fact three readers share: which accounts a run is actually made of. The census
 * stocks these and no others, and every step that names a person draws from these and no others,
 * so a drift here would show up as work handed to people the run never signs in as.
 */
class SeatedAccountsTest {

    private final LoadProfile day = ProfileLoader.load(Path.of("profiles", "day.json"));

    @Test
    @DisplayName("a run occupies the first accounts of each role, in the pool's own order")
    void theSeatsAreThePoolsFirstAccountsOfEachRole() {
        SeatedAccounts seated = SeatedAccounts.of(day);

        assertEquals(List.of("worker-01", "worker-02", "worker-03", "worker-04", "worker-05"),
                seated.names(Role.WORKER),
                "the sign-in step fills the pool in its stable order, so these are the people");
        assertEquals(List.of("manager-01", "manager-02", "manager-03", "manager-04"),
                seated.names(Role.MANAGER),
                "the discussion takes up a manager session, so it counts against the managers");
        assertEquals(List.of("admin-01"), seated.names(Role.ADMINISTRATOR));
    }

    @Test
    void theRestOfThePoolIsNotPartOfTheRun() {
        SeatedAccounts seated = SeatedAccounts.of(day);

        assertEquals(AccountPool.WORKERS - 5,
                AccountPool.WORKERS - seated.names(Role.WORKER).size(),
                "the pool is sized to the ceiling of the load model, and a day of ten users"
                        + " leaves most of it unoccupied - work handed to those accounts would"
                        + " leave the run's own circulation");
    }

    @Test
    void aPopulationThePoolCannotSeatIsRefusedWithTheConsequence() {
        List<AccountPool.Member> oneWorkerShort = List.of(
                new AccountPool.Member("worker-01", Role.WORKER),
                new AccountPool.Member("manager-01", Role.MANAGER),
                new AccountPool.Member("manager-02", Role.MANAGER),
                new AccountPool.Member("manager-03", Role.MANAGER),
                new AccountPool.Member("manager-04", Role.MANAGER),
                new AccountPool.Member("admin-01", Role.ADMINISTRATOR));

        IllegalArgumentException refusal = assertThrows(IllegalArgumentException.class,
                () -> SeatedAccounts.of(day, oneWorkerShort));

        assertTrue(refusal.getMessage().contains("starve"),
                "the refusal states the consequence: " + refusal.getMessage());
        assertTrue(refusal.getMessage().contains("worker"),
                "and which population it is about: " + refusal.getMessage());
    }
}
