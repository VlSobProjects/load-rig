package loadrig.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The account pool of the rig: the accounts a run signs in with, decoupled from the three a fresh
 * database seeds.
 *
 * <p>The pool is sized to the top step of the stepped search and not to the ten of the clean
 * baseline, so that one provisioned pool serves a whole campaign and the stand does not change
 * between the runs a capture is compared against. It was sized to twenty virtual users until the
 * search that tests that hypothesis was designed, and twenty turned out to be the wrong ceiling to
 * size against twice over: the baseline met every service level by one to two orders of magnitude,
 * so the search has to climb far past twenty, and a day profile scaled proportionally to twenty
 * already asks for a second administrator the pool did not hold.
 *
 * <p>What decides the counts is therefore not a share of the mix but the seats a scaled profile
 * needs at once. The day profile seats five workers, four managers - the discussion occupies
 * manager sessions - and one administrator per ten virtual users, so a search whose top step is
 * eight times the baseline needs forty workers, thirty-two managers and eight administrators. The
 * ratio carries the discussion scenario's demand for managers who are not the creator of the task
 * they comment on, which the seats already imply.
 *
 * <p>Accounts beyond what a step occupies cost that step nothing: the seating takes the first
 * accounts of each role and the run's work circulates among those alone, so a baseline of ten users
 * on this pool is the run it was before the pool grew. What they do cost is the history the seeding
 * item can give each one, which is why the pool is sized once for a campaign rather than grown
 * between its steps.
 *
 * <p>The three accounts of a fresh database stay outside the pool: the built-in administrator is
 * the tool the pool is provisioned with, not a participant of the load.
 *
 * <p>The counts and the names go out to the SUT project, so that the seeded history refers to the
 * same accounts a run signs in with. The twenty-five of the LR-4 answer stood until the search was
 * designed; the amendment travels through the desk when the campaign's top step is settled, and
 * until then the names of the first twenty-five are unchanged, so nothing already agreed moves.
 */
public final class AccountPool {

    public static final int ADMINISTRATORS = 8;
    public static final int MANAGERS = 32;
    public static final int WORKERS = 40;

    /** A member of the pool: the name an account signs in with and the role it is created in. */
    public record Member(String username, Role role) {
    }

    /** The members, in a stable order: the administrator, then the managers, then the workers. */
    public static List<Member> members() {
        List<Member> members = new ArrayList<>();
        addMembers(members, Role.ADMINISTRATOR, "admin", ADMINISTRATORS);
        addMembers(members, Role.MANAGER, "manager", MANAGERS);
        addMembers(members, Role.WORKER, "worker", WORKERS);
        return List.copyOf(members);
    }

    public static int size() {
        return ADMINISTRATORS + MANAGERS + WORKERS;
    }

    private static void addMembers(List<Member> members, Role role, String prefix, int count) {
        for (int index = 1; index <= count; index++) {
            members.add(new Member(String.format("%s-%02d", prefix, index), role));
        }
    }

    private AccountPool() {
    }
}
