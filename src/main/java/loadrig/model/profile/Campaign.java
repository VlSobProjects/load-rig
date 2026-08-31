package loadrig.model.profile;

/**
 * The configuration of a campaign: what is fixed before a campaign opens and holds across every
 * run of it, as against a profile, which is what one run applies.
 *
 * <p>Two things live here so far. The service levels, because a baseline and the faulted run it is
 * read against must be judged by the same figures - a morning and an evening variant judged by
 * different numbers compare nothing. And the depth of the rotation, because the accounts a run
 * plays are a property of the campaign and not of a step: the steps of a stepped search differ in
 * intensity alone, so the ratio between the people who play and the seats a step holds at once has
 * to be the same on all of them (DR-8).
 *
 * <p>One file states them and every run of the campaign reads it, so that which campaign a capture
 * belongs to is part of what the capture means.
 */
public record Campaign(int playersPerSeat, ServiceLevels serviceLevels) {

    public Campaign {
        requireRotationDepth(playersPerSeat);
        if (serviceLevels == null) {
            throw new IllegalArgumentException("a campaign states the service levels its captures"
                    + " are judged by; a capture judged against figures nobody wrote down cannot"
                    + " be re-read");
        }
    }

    /**
     * How many accounts of the pool each seat of a step rotates through: the instrument that
     * decides how wide the working set is, and therefore how little of it a cache can hold (DR-8).
     *
     * <p>A depth of one is admissible and states something: the run plays exactly the accounts it
     * seats, which is the narrow working set this rule exists to widen. It stays admissible
     * because the pool is finite - the top step of a stepped search may seat every account of a
     * role - and a campaign that states it says so where a reader of the capture can see it, since
     * the depth travels in the description.
     */
    private static void requireRotationDepth(int playersPerSeat) {
        if (playersPerSeat < 1) {
            throw new IllegalArgumentException("playersPerSeat is " + playersPerSeat
                    + "; a campaign whose seats rotate through less than one account each plays"
                    + " nobody");
        }
    }
}
