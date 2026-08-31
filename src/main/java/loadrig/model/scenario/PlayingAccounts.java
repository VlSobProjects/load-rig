package loadrig.model.scenario;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import loadrig.model.AccountPool;
import loadrig.model.Role;
import loadrig.model.profile.Campaign;
import loadrig.model.profile.LoadProfile;
import loadrig.model.profile.ScenarioName;

/**
 * The accounts a run plays over its window: the first accounts of each role, in the pool's own
 * order, as many of them as the profile's populations seat at once multiplied by the depth the
 * campaign rotates those seats through.
 *
 * <p>The rotation is the point (DR-8). A run seats as many sessions as its populations state, but
 * sessions end at a stated share and the account that signs in next is chosen by state, so over a
 * window the seats are occupied by more people than sit in them at any moment. An account between
 * sessions is waiting its turn rather than absent, which is why it is a player here: every list it
 * appears in, every task assigned to it and every report about it is part of the working set the
 * stand must hold, and a working set as narrow as the seats sits in whatever cache holds it.
 *
 * <p>One fact with four readers, because they must agree. The check refuses a profile and campaign
 * the pool cannot carry. The session registry admits these accounts and no others, so the choice
 * rule picks a player rather than a stranger to the run. The census stocks these accounts and no
 * others. And every step that names a person - the assignee of a creation, the worker a hand-out
 * goes to, the subject of a report - draws from here rather than from the whole pool: the pool is
 * sized to the ceiling of the load model, so a run that handed work to all forty workers would
 * send most of its creations to people it never signs in as. That work is created and never
 * touched, and the transitions starve for want of open tasks while the table grows - measured, on
 * the first run whose population was otherwise sound: the transitions landed a third under target
 * with every missing move recorded as a starved gate (DR-6, amended by DR-8 from the accounts a
 * run occupies to the accounts it plays).
 */
public record PlayingAccounts(Map<Role, List<String>> byRole, Map<Role, Integer> seatsByRole,
        int playersPerSeat) {

    public PlayingAccounts {
        EnumMap<Role, List<String>> players = new EnumMap<>(Role.class);
        byRole.forEach((role, names) -> players.put(role, List.copyOf(names)));
        byRole = Map.copyOf(players);
        seatsByRole = Map.copyOf(seatsByRole);
    }

    /** The accounts the profile's populations play in the rig's own account pool. */
    public static PlayingAccounts of(LoadProfile profile, Campaign campaign) {
        return of(profile, campaign, AccountPool.members());
    }

    /**
     * The accounts the profile's populations play in the stated pool, refusing loudly when the
     * pool cannot carry them: a run under a profile, a campaign and a pool that disagree starves
     * instead of loading, and it must not spend a stand window to find that out.
     */
    public static PlayingAccounts of(LoadProfile profile, Campaign campaign,
            Collection<AccountPool.Member> pool) {
        Map<Role, Integer> seats = seatsNeeded(profile);
        EnumMap<Role, List<String>> byRole = new EnumMap<>(Role.class);
        for (Role role : Role.values()) {
            List<String> ofRole = pool.stream()
                    .filter(member -> member.role() == role)
                    .map(AccountPool.Member::username)
                    .toList();
            int played = seats.get(role) * campaign.playersPerSeat();
            if (ofRole.size() < played) {
                throw new IllegalArgumentException("the " + populationsOf(role) + " population"
                        + " needs " + seats.get(role) + " " + role + " session(s) at once and the"
                        + " campaign rotates every seat through " + campaign.playersPerSeat()
                        + " account(s), so the run plays " + played + " of them while the pool"
                        + " holds " + ofRole.size() + "; the profile, the campaign and the account"
                        + " pool disagree, and a run under them would starve instead of loading");
            }
            byRole.put(role, ofRole.subList(0, played));
        }
        return new PlayingAccounts(byRole, seats, campaign.playersPerSeat());
    }

    /** The accounts of the role the run plays, in the order the sign-ins first fill them. */
    public List<String> names(Role role) {
        return byRole.getOrDefault(role, List.of());
    }

    /**
     * How many sessions of the role the profile's populations hold at once. A seat is drawn from
     * at its own rate whoever is sitting in it, which is what the census stocks a player against.
     */
    public int seats(Role role) {
        return seatsByRole.getOrDefault(role, 0);
    }

    /**
     * The players as members of a pool: what the session registry is built over, so that the
     * account an iteration signs in with is one of the run's own people. The registry over the
     * whole pool would leave the choice rule free to seat an account the census never stocked.
     */
    public List<AccountPool.Member> members() {
        List<AccountPool.Member> members = new ArrayList<>();
        byRole.forEach((role, names) ->
                names.forEach(username -> members.add(new AccountPool.Member(username, role))));
        return List.copyOf(members);
    }

    /** The rotation as the run report and the capture's description state it, role by role. */
    public String statement() {
        StringBuilder statement = new StringBuilder();
        for (Role role : Role.values()) {
            statement.append(String.format(Locale.ENGLISH,
                    "    %d %s account(s) play %d seat(s)%n",
                    names(role).size(), role.name().toLowerCase(Locale.ENGLISH), seats(role)));
        }
        return statement.toString();
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
