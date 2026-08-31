# Session note

## Type

`implementation`

## Goal

Make the population turn over: a session is given up at a share the profile states, and the
account that signs in next is chosen from the rig's own registry by what it holds - the first two
rules of DR-8, so that the band measuring sign-ins measures a steady state rather than the ramp.

## Completion criteria

1. The profile states the share of iterations that end their session, validated like every other
   share, and a profile that omits it is refused rather than defaulted.
2. An iteration drawn for that share signs out through the screen the application offers, the
   registry records the account as signed out, and its context is dropped.
3. The account signing in is chosen by the state the registry holds - the depth of its backlog,
   the acts it has taken in the run, how long since its last one - rather than by the pool's
   order, and the rule is unit-tested without a stack.
4. `./gradlew build` passes from the command line.
5. A run against the standing stack shows sign-ins recurring through the window rather than
   ending with the ramp.

## Scope boundaries

The four remaining rules of DR-8 are not touched: the players stay as wide as the seats, the
census stays over the seated accounts, the warm start still levels in one direction only, and the
deletion share stays at the specification's one percent. The campaign file that will hold the
rotation depth and the initial state belongs to the next session.

## Status

`completed`

## Result

All five criteria met. The sign-in band is a steady-state figure for the first time.

**The share.** `endingASessionPercent` is a profile field, validated like the other shares, and the
day profile states two. It is not a row of the mix and takes nothing from the business steps: an
iteration drawn for it has already performed the step it drew. A share of zero is refused by name
rather than allowed, because it is exactly the model the calibration probe measured - everybody
signs in on the ramp and never again - and getting it back is a decision, not a value.

**Signing out is an operation with a level of its own.** The specification's service-level table
names no such act, and the two ways of judging it inside that table are both wrong: the sign-in
band is deliberately loose because password hashing is slow, and a sign-out hashes nothing. So the
closed list of bands gained a row this project states rather than one the SUT project handed over.
The run measured why it matters: signing out realized 7 ms at the 95th percentile against 89 ms
for signing in. Held in the sign-in band, those twelve cheap samples would have pulled the figure
of the band that DR-8 exists to make honest.

**The choice rule.** The account signing in is the one with the deepest backlog - the tasks it may
move, deletion excluded, asked of the rig's own task registry and never of the stand - then the
one that has taken fewest acts, then the one longest since its last. The pool's order breaks what
is still tied, so a ramp, where every account is equally idle, occupies the pool as predictably as
before. The backlog arrives as a function rather than as a reference to the task registry, so the
two registries stay independent and the rule is testable without either a stack or a task.

**The run.** The scratch profile at ten virtual users over a five-minute window, 798 samples, none
failed, nothing refused. Twelve sessions given up against a stated target of 2.4 a minute - the
target exactly - and twenty-two sign-ins: ten on the ramp and twelve after it, spread from the
57th second to the 322nd. Every band met its level; the worst wait of the run was 115 ms.

## Decisions

- **A band the specification does not name is this project's to state.** The load model belongs to
  the rig, and a figure for one of its acts is derived the way a load analyst derives one - from
  how the act is used - not asked of the system's authors. It travels to the SUT project through
  the desk as a fact of the campaign. `signingOut` is the first such row and is stated at 1000 ms,
  the figure of an act with a consequence and an answer.
- **The sign-out carries no think time of its own.** The iteration already paid one for the step it
  performed, and giving the session up ends that same visit. A second pause would lower every
  computed intensity by the share of iterations that end, for no business fact.
- **The act is recorded on both the release and the sign-out.** An account that ends every stint
  would otherwise look like one that never worked, and the choice rule would send it back in first
  every time.
- **The backlog leaves deletion out.** An administrator may delete a task in any status, so
  counting it would make their backlog the whole table and every worker's a strict subset - a depth
  that says nothing about who has work waiting. An administrator's backlog is therefore nil and
  their turn is decided by the second and third facts, which is correct: they are never short of
  work.

## Open questions

- The choice rule is mostly latent while the players are as wide as the seats: the account signing
  back in is usually the only signed-out one of its role. It becomes an instrument in the next
  session, when the rotation is wider than the seats.
- The stand now holds 1726 tasks and the warm start still only creates. Every capture until the
  levelling lands still varies two things at once, which is the confound the probe measured.

## Next steps

1. The campaign file: `profiles/service-levels.json` becomes the campaign's configuration and
   gains the rotation depth and the initial state, as agreed.
2. LR-13 rules three and four: players wider than the seats, and the census over the whole
   rotation.
3. LR-13 rules five and six: the stated initial state levelled in both directions, and deletion at
   a stated share, with the desk letter carrying the deviation and the new band.

## References

- Branch: `feature/lr13-sessions-that-end`
- Decision record: DR-8, rules one and two
- Changed: the profile vocabulary and its loader, `ServiceLevelBand` and `StepKind`,
  `SessionRegistry` and `TaskRegistry`, `SessionSteps` and `ProfilePlan`, `ScenarioDemand` and the
  load-profile description, the day profile and the service levels, `README.md` and
  `docs/brief.md`
- Verified: `./gradlew build` from the command line, 143 unit tests, no failures; one run against
  the standing stack on 2026-08-31 at SUT version 1.0.0, whose figures are above and whose profile
  is a scratch file outside the repository
