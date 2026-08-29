package loadrig.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * Holds the composition of the account pool: the counts, the determinism of the names and the
 * separation from the accounts a fresh database seeds. The counts themselves are the rig's answer
 * to the SUT project, so a change here is a change of that answer and never an accident.
 */
class AccountPoolTest {

    @Test
    void thePoolIsTheConfiguredComposition() {
        List<AccountPool.Member> members = AccountPool.members();

        assertEquals(AccountPool.size(), members.size());
        assertEquals(AccountPool.ADMINISTRATORS, count(members, Role.ADMINISTRATOR));
        assertEquals(AccountPool.MANAGERS, count(members, Role.MANAGER));
        assertEquals(AccountPool.WORKERS, count(members, Role.WORKER));
    }

    @Test
    void everyMemberHasANameOfItsOwn() {
        List<AccountPool.Member> members = AccountPool.members();

        Set<String> names = members.stream()
                .map(AccountPool.Member::username)
                .collect(Collectors.toSet());

        assertEquals(members.size(), names.size(),
                "two members sharing a name would be one account contended by two sessions");
    }

    @Test
    void thePoolIsTheSamePoolEveryTimeItIsAskedFor() {
        assertEquals(AccountPool.members(), AccountPool.members(),
                "the seeded history and the run refer to the pool by name, "
                        + "so the names cannot depend on when they were asked for");
    }

    @Test
    void theAccountsOfAFreshDatabaseAreNotMembers() {
        Set<String> names = AccountPool.members().stream()
                .map(AccountPool.Member::username)
                .collect(Collectors.toSet());

        for (String builtIn : List.of("admin", "manager", "worker")) {
            assertFalse(names.contains(builtIn),
                    "the built-in account " + builtIn + " is the provisioning tool "
                            + "or a leftover of the smoke walk, never a participant of the load");
        }
    }

    private static long count(List<AccountPool.Member> members, Role role) {
        return members.stream().filter(member -> member.role() == role).count();
    }
}
