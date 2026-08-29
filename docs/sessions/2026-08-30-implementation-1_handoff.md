# Session note

## Type

`implementation`

## Goal

Implement LR-10: the population the profile needs is computed rather than assumed and brought
about before the window opens, so the mix holds from the first minute; and the profile's stated
seeded volume, which names a history no stand holds, is replaced by what the stand itself
reports.

## Completion criteria

1. The census is computed, per bucket, from the scenarios' own coded weights and the profile's
   populations and think time — the stock of tasks each acting account needs in each status the
   steps draw from, covering both the cold start and the drain the window causes — and it scales
   with the profile instead of being stated by it.
2. A warm start brings the stand to that census before the window: it reads what the stand
   already holds from the list page ordered by due date, registers those tasks as the run's own
   facts, and creates and drives only the difference. It degenerates to pure creation on an empty
   stand and to pure reading on a seeded one.
3. The warm start is stand administration, not load: none of its requests appears in the result
   log, and the capture's achieved intensity is the window's alone.
4. `seededVolume` leaves the profile vocabulary. The number of tasks the stand holds is measured
   at the warm start from the list's own count, and the load-profile description publishes the
   measured figure together with the census the run established.
5. The population invariant is re-based: what a run is refused for is a census it cannot bring
   about, and the table's growth over the window is published as a fact of the capture rather
   than judged against a figure nobody measured.
6. The census, the shortfall arithmetic and the reading of the list page are unit-tested without
   a stack; `./gradlew build` passes from the command line.
7. A run against the standing stack shows the transitions inside the accepted band instead of
   four times under target, and its run report and description carry the warm start's facts.
8. `docs/decisions/dr-6-*.md` records the choice; `README.md`, `docs/brief.md` and
   `docs/roadmap.md` carry what the change makes inaccurate.

## Scope boundaries

No calibration run, no morning or evening variant and no measured baseline against the service
levels — that is LR-6. The account pool and its provisioning are untouched: the warm start
assumes a provisioned pool and says so when it does not find one.

The scenarios were to keep their steps, their weights and their transport, and they did. One
boundary was crossed deliberately and with the user's decision: the rule by which a step names a
person — the assignee of a creation, the worker a hand-out goes to, the subject of a report — is
now the accounts the run occupies rather than the whole pool. The measurement that forced it and
the alternative that was rejected are in the result and in DR-6.

## Status

`completed`

## Result

All eight criteria are met, and the last of them took three measured windows to reach.

- The census is computed per bucket — one acting account, one relation to the task, one status —
  from the scenarios' coded weights. The day profile asks for 123 tasks over 21 buckets; a
  profile with twice the workers and twice the manager sessions asks for exactly twice that,
  without a number being restated anywhere. It refuses loudly a census no warm start could bring
  about, which is what stops a profile from asking for a seeded history in the minutes before a
  window.
- The warm start reads the stand from the list page ordered by due date, registers what it finds,
  and creates and drives only the difference. It runs on the administration transport beside the
  pool provisioning: the result log holds the window and nothing else. On the standing stack it
  read 500 tasks and created 42 of them; against a stand that already held the population it
  would create none.
- `seededVolume` is gone from the vocabulary and from the day profile; a profile that still
  states it is refused by name. The stand's volume is measured from the list's own count and
  published beside the census in the load-profile description.
- The equilibrium check dissolved into the two pieces behind it: the demand the coded scenarios
  imply — where the specification's weighting of about two and a half transitions per settlement
  is replaced by rates the weights state exactly — and the census that demand requires. What
  refuses a run is now the census; what the window adds to the table is published as a fact.
- The three windows, each ten minutes on the standing stack, are the record of how the number was
  reached. The first, with the census as first written, ended clean but realized its transitions
  a third under target with 61 starved gates. The measurement named the cause: the pool is sized
  to the ceiling of the load model, and the manager's creation drew its assignee from all of it,
  so eleven of every sixteen created tasks went to accounts the run never signs in as. With the
  draw narrowed to the accounts the run occupies, the second window came to 13% under with 21
  starved gates — the remainder being the randomness of the draw against a stock that covered only
  the average deficit. The slack was then scaled to the square root of what the window draws, and
  the third window realized every row of the mix within five percent of its target, with one
  starved gate in ten minutes.

Verified from the command line: `./gradlew build` passes with 118 unit tests and no stack; four
runs against the standing stack on 2026-08-29 — one one-minute scratch profile and the three day
windows above, the last realizing 1493 samples, none failed, no refusals.

## Decisions

Recorded in `docs/decisions/dr-6-the-run-establishes-its-own-population.md`, which this session
opened as a Draft. Beyond it:

- **The reading of the list page is its own named thing, not a correlation rule.** A rule of the
  correlation dictionary is one expression with one capturing group applied by the injector as an
  extractor; a list row is six facts read by the rig's own Java outside any test plan. Both are
  proven against a captured answer, and the adaptation ledger of DR-5 now names the list's columns
  and status words among what a SUT change would cost.
- **The application's word for a status is not the status.** Finished work is stored as completed
  and shown as done. The spelling belongs to the surface, the state to the registry, and the
  mapping is made once — a mark the rig cannot name refuses the warm start instead of registering
  a task in a state nobody defined.
- **The administration transport is shared, not duplicated.** The provisioning's own session
  became a class the warm start uses too, and both now read the session token through the
  correlation dictionary's rule rather than through two expressions that happen to agree. The
  token is read from the last answer before every mutating request, because a screen with no form
  on it renders none.
- **The hot window is one constant.** Three facts rested on "due within three days" — the due
  date a creation draws, the flag the registry marks a task with, and the census the warm start
  establishes — and they now read one name.

## Open questions

- The realized mix now sits three to five percent under target across every row, uniformly. That
  is inside the band and consistent across kinds, so the mix itself is right; whether the shortfall
  is the ramp, the sign-in steps that consume a think time without being a row of the mix, or
  something else is unmeasured.
- The slack of two deviations was chosen for a ten-minute window and proven there. A much longer
  window drains further and asks for a bigger census, and where that meets the ceiling — a census
  a warm start may not bring about — is the calibration item's business.
- The warm start reads at most twenty-five pages of a stand's history. On the seeded stand the SUT
  project's item will produce, the pages that matter are the hot end of the due-date order; whether
  twenty-five is the right depth there is not knowable until that stand exists.

## Next steps

1. LR-6: the stepped maximum-finding run, the morning and evening variants, and the first measured
   baseline against the service levels — the fifth answer still owed to the SUT project.
2. Accept or supersede DR-6 once LR-6 has run a campaign over it.

## References

- Branch: `feature/lr10-warm-start`
- Decision records: DR-6 (Draft, opened here); DR-5's adaptation ledger extended
- Added: `HotWindow`, `TaskListReading`, `ScenarioDemand`, `WarmStartCensus`, `SeatedAccounts`,
  `StandSession`, `WarmStart`, and their tests with a captured list page
- Changed: the four scenarios, `Scenarios`, `ScenarioWiring`, `ProfilePlan`, `LoadProfile`,
  `ProfileLoader`, `SutSurface`, `TaskStatus`, `PoolProvisioner`, `ProfileRun`,
  `ProfileValidation`, `LoadProfileDescription`, the day profile
- Removed: `EquilibriumCheck` and its test
- Documents changed: `README.md`, `docs/brief.md`, `docs/roadmap.md` (LR-10 done),
  `docs/decisions/dr-5-a-rig-for-this-sut-not-a-universal-one.md`
