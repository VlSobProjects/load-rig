package loadrig.model.scenario;

import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import loadrig.model.AccountPool;
import loadrig.model.Role;
import loadrig.model.profile.LoadProfile;
import loadrig.model.profile.ScenarioName;

/**
 * The accounts a profile's populations occupy: the first accounts of each role, in the pool's own
 * order - which is the order the sign-in step fills them in, so these are the people a run is
 * actually made of.
 *
 * <p>One fact with three readers, because they must agree. The seating check refuses a profile the
 * pool cannot carry. The census stocks these accounts and no others. And every step that names a
 * person - the assignee of a creation, the worker a hand-out goes to, the subject of a report -
 * draws from here rather than from the whole pool: the pool is sized to the ceiling of the load
 * model, so a run of ten users that handed work to all sixteen workers would send most of its
 * creations to people it never signs in as. That work is created and never touched, and the
 * transitions starve for want of open tasks while the table grows - measured, on the first run
 * whose population was otherwise sound: the transitions landed a third under target with every
 * missing move recorded as a starved gate.
 */
public record SeatedAccounts(Map<Role, List<String>> byRole) {

    public SeatedAccounts {
        EnumMap<Role, List<String>> copy = new EnumMap<>(Role.class);
        byRole.forEach((role, names) -> copy.put(role, List.copyOf(names)));
        byRole = Map.copyOf(copy);
    }

    /** The accounts the profile's populations occupy in the rig's own account pool. */
    public static SeatedAccounts of(LoadProfile profile) {
        return of(profile, AccountPool.members());
    }

    /**
     * The accounts the profile's populations occupy in the stated pool, refusing loudly when the
     * pool cannot carry them: a run under a profile and a pool that disagree starves instead of
     * loading, and it must not spend a stand window to find that out.
     */
    public static SeatedAccounts of(LoadProfile profile, Collection<AccountPool.Member> pool) {
        Map<Role, Integer> seats = seatsNeeded(profile);
        EnumMap<Role, List<String>> byRole = new EnumMap<>(Role.class);
        for (Role role : Role.values()) {
            List<String> ofRole = pool.stream()
                    .filter(member -> member.role() == role)
                    .map(AccountPool.Member::username)
                    .toList();
            int needed = seats.get(role);
            if (ofRole.size() < needed) {
                throw new IllegalArgumentException("the " + populationsOf(role) + " population"
                        + " needs " + needed + " " + role + " session(s) at once while the pool"
                        + " holds " + ofRole.size() + "; the profile and the account pool"
                        + " disagree, and a run under them would starve instead of loading");
            }
            byRole.put(role, ofRole.subList(0, needed));
        }
        return new SeatedAccounts(byRole);
    }

    /** The accounts of the role the run occupies, in the order the sign-ins fill them. */
    public List<String> names(Role role) {
        return byRole.getOrDefault(role, List.of());
    }

    /** How many sessions of each role the profile's populations hold at once. */
    private static Map<Role, Integer> seatsNeeded(LoadProfile profile) {
        Map<ScenarioName, Role> roles = Scenarios.sessionRoles();
        Map<Role, Integer> seats = new LinkedHashMap<>();
        for (Role role : Role.values()) {
            seats.put(role, 0);
        }
        profile.scenarioPopulation().forEach((name, population) ->
                seats.merge(roles.get(name), population, Integer::sum));
        return seats;
    }

    /** The scenarios that take up sessions of the role, named as the refusal names them. */
    private static String populationsOf(Role role) {
        Map<ScenarioName, Role> roles = Scenarios.sessionRoles();
        return roles.entrySet().stream()
                .filter(scenario -> scenario.getValue() == role)
                .map(scenario -> scenario.getKey().key())
                .reduce((one, another) -> one + " and " + another)
                .orElse(role.name().toLowerCase(Locale.ENGLISH));
    }
}
