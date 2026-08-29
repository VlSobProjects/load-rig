package loadrig.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The account pool of the rig: the accounts a run signs in with, decoupled from the three a fresh
 * database seeds.
 *
 * <p>The pool is sized to the ceiling of the load model - twenty virtual users, the hypothesis the
 * stepped calibration run tests - and not to the ten of the clean baseline, so that one provisioned
 * pool serves a whole campaign and the stand does not change between the runs a capture is compared
 * against. Roughly a third of the step mix is a manager's work - the report, the creation, the
 * manager's half of the transitions and of the discussion - so a third of the twenty-four working
 * accounts are managers, which also carries the discussion scenario's demand for managers who are
 * not the creator of the task they comment on. One administrator is enough for the one percent of
 * the mix that is the administrator's act.
 *
 * <p>The three accounts of a fresh database stay outside the pool: the built-in administrator is
 * the tool the pool is provisioned with, not a participant of the load.
 *
 * <p>The counts and the names go out to the SUT project with the LR-4 answer, so that the seeded
 * history refers to the same accounts a run signs in with. A larger pool would not be safer: the
 * report asks about one worker's history, and every account beyond what the load can occupy thins
 * the history the seeding item can give each one.
 */
public final class AccountPool {

    public static final int ADMINISTRATORS = 1;
    public static final int MANAGERS = 8;
    public static final int WORKERS = 16;

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
