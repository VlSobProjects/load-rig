package loadrig.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The run's ledger of account identities: which numeric id the system under test knows each
 * pool account by. The rig never reads the SUT's database, so the ids are harvested from the
 * answers the application itself gives - the assignee options a manager's screen offers - and
 * every step that must name an account by id (a creation, a hand-out, a report) reads it here
 * instead of extracting it again.
 *
 * <p>The directory only grows: an id learned once holds for the run, because the pool is
 * provisioned before the run and no scenario deletes a pool account.
 */
public final class UserDirectory {

    private final Map<String, String> ids = new ConcurrentHashMap<>();

    /** Records an identity read out of an answer. Learning the same identity again is idle. */
    public void learned(String username, String id) {
        ids.put(username, id);
    }

    /** The id the SUT knows the account by, if any answer has offered it yet. */
    public Optional<String> idOf(String username) {
        return Optional.ofNullable(ids.get(username));
    }

    /** The accounts among the named ones whose identity is already known. */
    public List<String> knownAmong(Collection<String> usernames) {
        List<String> known = new ArrayList<>();
        for (String username : usernames) {
            if (ids.containsKey(username)) {
                known.add(username);
            }
        }
        return known;
    }
}
