# Session note

## Type

`implementation`

## Goal

Make the population wider than the seats: the campaign becomes a file of its own that states how
many accounts the seats rotate through, the run plays that rotation rather than the accounts it
seats at once, and the census stocks every account of it - the third and fourth rules of DR-8.

## Completion criteria

1. `profiles/campaign.json` is the campaign's configuration: it holds the service levels the file
   before it held and the depth of the rotation, loaded as strictly as a profile, and a run states
   it as one file.
2. The accounts a run plays are the seats of its populations multiplied by that depth, refused
   loudly when the pool cannot carry them, and the session registry admits those accounts alone
   rather than the whole pool.
3. Every step that names a person draws from the rotation, and the census stocks every account of
   it rather than the seated few.
4. The capture's description and the run report state the rotation they played.
5. `./gradlew build` passes from the command line.

## Scope boundaries

The two remaining rules of DR-8 are not touched: the warm start still only creates, the campaign
states no volume for the stand, and deletion stays at the specification's one percent. The
levelling in both directions and the stated deletion share are the next session's, and the desk
letter carrying the deviation goes with them.

## Status

`completed`

## Result

All five criteria met, and a sixth thing was found and fixed on the stand: the census stocked the
rotation for the wrong horizon, which the first run showed and the second confirmed corrected.

**The campaign file.** `profiles/service-levels.json` becomes `profiles/campaign.json` and states
`playersPerSeat` beside the levels; a run reads it through its own property. The depth is stated
as a ratio and not as counts per role, because the steps of a stepped search scale their
populations and stated counts would change the instrument along with them. The day campaign states
three.

**The rotation.** `PlayingAccounts` replaces `SeatedAccounts`: the accounts of each role a run
plays are the seats its populations need multiplied by the campaign's depth. The session registry
is built over those accounts instead of the whole pool, so the choice rule of the sign-in picks
among the run's own people; before this it could seat a stranger the census had never stocked, and
only the backlog such an account lacked kept it from doing so. The pool's capacity is refused
against the rotation, which makes the pool ceiling a constraint of the campaign: at a depth of
three the day profile reaches twice its intensity and no further.

**The horizon a bucket is stocked against - the finding of this session.** The census divided each
demand over the whole rotation, which is right for a window and wrong for a stint: a seat is drawn
from at the seat's rate whoever is sitting in it, and the rotation does not turn over between two
steps. The first run measured it - a worker stocked for four open tasks was drawn from about seven
times in one stint, and the starvation ledger recorded seven skipped transitions where the
previous session's runs had none. A bucket is now stocked for the larger of two covers: the
window's average as before, and the drain of one stint at the seat's rate, the stint derived from
the share of iterations that end a session and the think time. A rotation one account deep leaves
every figure where it was, because there the two horizons coincide.

**The runs.** Two, five-minute windows at ten virtual users against SUT 1.0.0, both with thirty
accounts playing, no failed samples and nothing refused, every band met by one to two orders of
magnitude - the worst wait of either run was 112 ms. The first, on a census of 168 tasks: seven
skipped transitions. The second, on the corrected census of 357: one, a manager's approval, which
is the tail the two-deviation slack allows rather than a shortfall. Twenty-two sign-ins and twelve
sign-outs in each, as the stated share implies.

## Decisions

- **The depth belongs to the campaign, as a ratio.** It decides how wide the working set is, so it
  has to hold across the runs a capture is read against; and it is stated as accounts per seat
  because a search scales its populations, where absolute counts would silently rewrite the
  instrument. The alternative - a second file beside the levels - was refused: two files fixed
  before a campaign are one file somebody forgets to pass.
- **A depth of one stays admissible.** It is the narrow population the calibration probe measured,
  but the pool is finite and the top step of a search may seat every account of a role, so
  refusing it would decide a question that belongs to LR-11. It is stated where a reader of the
  capture can see it, since the depth travels in the description.
- **The registry admits the players and nobody else.** Reading the whole pool and trusting the
  choice rule to stay inside the rotation would make the composition of a run an emergent property
  of a heuristic instead of a stated fact of the campaign.
- **A bucket is stocked for a stint, not only for a window.** The two covers are computed and the
  larger wins, rather than one replacing the other: the window still binds where the feed falls
  short of the draw over the long run, and the stint binds wherever a seat drains a bucket faster
  than the rotation refills it.
- **The rotation travels with the capture.** Two windows of one intensity over rotations of
  different depths are not comparable, and nothing in the result log would say which was which.

## Open questions

- The pool ceiling now binds the campaign: the seats times the depth must fit the pool, so the day
  campaign at a depth of three reaches a step of twice the baseline and no further, while the
  probe found no knee below eight times it. Either the search runs at a shallower rotation, or the
  pool grows and the SUT project is told through the desk. It belongs to LR-11 and is not decided
  here.
- The stint is derived from the share of iterations that end a session and the think time, which
  assumes an account holds its seat continuously until it gives it up. That is what the plan does
  today; a scenario that dropped a session for another reason would make the derived stint too
  long and the census with it.
- The stand has been measured at 1877 tasks and still only grows: every capture until the
  levelling lands varies the volume along with whatever it means to measure.

## Next steps

1. LR-13 rule five: the campaign states how many tasks the stand holds, and the warm start levels
   to it in both directions.
2. LR-13 rule six: deletion at a stated share above the specification's one percent, with the pick
   unbiased.
3. The desk letter carrying this project's deviations to the SUT project: the deletion share, the
   band for signing out, and the rotation as a fact of the campaign.

## References

- Branch: `feature/lr13-players-beyond-the-seats`
- Decision record: DR-8, rules three and four
- Changed: `Campaign` and its loader, `PlayingAccounts` in place of `SeatedAccounts`,
  `WarmStartCensus`, `ScenarioDemand`, `ScenarioWiring`, `ProfilePlan`, `ManagerScenario`,
  `RigConfiguration`, `ProfileRun`, `ProfileValidation`, `LoadProfileDescription`, the campaign
  file, `README.md` and `docs/brief.md`
- Verified: `./gradlew build` from the command line, 153 unit tests, no failures; two runs against
  the standing stack on 2026-08-31 at SUT version 1.0.0, whose figures are above and whose profile
  is a scratch file outside the repository
