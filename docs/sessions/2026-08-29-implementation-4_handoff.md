# Session 2026-08-29-implementation-4

## Type
`implementation`

## Goal
Implement DR-3's half of LR-3: the step kit distilled from the transport walk's private steps,
the scenario classes behind the closed names, and the registries and the per-session transport
context wired into scheduled thread groups driven entirely by the loaded profile.

## Completion criteria
- The step kit exists as typed code: a step cannot be built without its stable name, its
  content assertion and its extractions — the kit's signatures take what a step owes, the
  author cannot forget it (DR-3's pattern-by-types).
- Correlation is enumerable: the rules that read identities and tokens out of answers — today
  private regexes of the transport walk — live in a named dictionary beside the surface, each
  rule verifiable on its own against a captured answer.
- The four scenario classes of the closed list exist — worker, manager, administrator,
  discussion — each one class with the fixed skeleton, built only from the step kit, holding
  business order and business logic and nothing the kit already takes.
- The transport context of a session — the cookie and the CSRF token of the last page the
  session loaded — travels with the session across threads, attached at sign-in and dropped at
  sign-out, so that a session outliving its iteration keeps its identity.
- A test plan is assembled from the loaded profile alone: the populations, the ramp, the
  steady window, the think-time bounds and the hot-set skew all come from the profile object,
  and no scheduling number is a literal in the plan.
- The transport walk keeps working as the conformance walk of DR-3's gates; whatever it now
  shares with the kit it takes from the kit.
- `./gradlew build` passes from the command line; the kit, the correlation dictionary and the
  scenario skeletons are unit-tested without a running stack.
- DR-3 moves `Draft` → `Accepted`, with the kit's actual boundary recorded.

## Scope boundaries
This session does not run the profile against the stack (the run harness and its documented
command are LR-5; a short wiring smoke may prove sign-in context passing if the stand is up,
but no capture is produced), does not author the morning and evening profiles (LR-6), does not
build the defect variants (LR-7), and does not send the LR-4 answer — that is the next session,
quoting this one's mix-survival findings.

## Status
`completed`

## Result
Every criterion is met.

- The step kit is typed code in `loadrig.model.step`: a request takes its stable name, its
  content assertion and its correlation rules by signature and passes methods and parameters
  through to the underlying DSL untouched; a step bundles the weight, the mix kind, the
  registry gate and the think time - drawn once per business act, which is exactly the reading
  the equilibrium intensities rest on. Steps are constructible only through the kit.
- The correlation dictionary lives beside the surface in `loadrig.model`: the token, the task
  identity, the account identities - each rule one expression applied identically by the
  injector's extractor and by the rig's own Java, and proven alone against captured answers.
  The transport walk now reads its identities through the dictionary.
- The four scenario classes behind the closed names exist in `loadrig.model.scenario`, each one
  class with the fixed skeleton, holding business order, its own step weights and the business
  constants (the transition splits, the hot-window creation share, the report window). The
  closed registry is materialized: a name without a scenario refuses the wiring.
- The transport context travels with the session: cookies and page token are a typed attachment
  in the session registry, attached at sign-in, handed out with the lease, dropped at sign-out
  in the same act. During requests the thread's cookie store is only a scratch register, loaded
  and mirrored around each request, so redirect chains stay correct while no session state
  survives in any thread.
- `ProfilePlan` assembles the test plan from the loaded profile alone - populations, ramp,
  steady window, think-time bounds and skew - as one thread group per populated scenario, named
  by the scenario's key. Before assembling anything it holds three invariants: the population
  equilibrium, the survival of the mix, and the seating of the populations on the pool.
- The mix-survival check computes the global step shares from the scenarios' stated weights and
  the profile's populations and refuses drift beyond half a percentage point; the day
  populations reproduce the specification's mix exactly, and `./gradlew validateProfile` now
  reports mix survival beside the intensities.
- Starved gates skip and are counted on the starvation ledger - a skip is a timing state, never
  silent; an exhausted pool stops the thread loudly after one recorded error.
- DR-3 is Accepted, with the kit's realized boundary recorded in the record.

Verified from the command line: `./gradlew build` passes with 78 unit tests and no stack;
`./gradlew validateProfile` holds the day profile and refuses a populations-drifted copy with
the computed and the stated share named. No run against the stack was made - the run harness
and its documented command are LR-5.

## Decisions
- **The transport context is a typed attachment of the session registry** (the alternative - a
  transport-side map by username - was rejected in the plan): the sign-out and the dropped
  context are one act, and a signed-in session without a context is structurally impossible -
  releasing without one is refused.
- **The injector's cookie store is a scratch register, not an owner.** The held session's
  cookies are loaded into the thread's store before its first request and mirrored back after
  every request, because the store is what keeps a mid-redirect `Set-Cookie` - the sign-in's
  own case - correct; the context that survives is always the registry's.
- **Scenarios state their weights, and the weights are checked twice.** Each scenario's stated
  per-kind table is the single source both for the scheduled percentages and for the
  mix-survival computation; the built iteration is validated against the table at construction,
  so the check can never drift from the plan it checks.
- **A session is held per iteration, not per step.** The iteration takes one session up and
  sets it down at the end; sign-ins happen only when no signed-in session is free, so sessions
  live across iterations and threads and sign-ins stay rare, as a day of real users has them.
- **Task-lease starvation skips, pool starvation stops.** A manager with nothing to approve is
  the run's timing and is skipped and counted; a pool that cannot seat even a sign-in is a
  configuration mistake, and the thread ends after one loud error instead of flooding the
  capture with the rig's own failures.
- **Read picks are randomized, leases stay first fit.** Opens and notes spread over the hot set
  - a run converging on the set's first member measures one row's cache - and acquired sessions
  spread over the pool; sign-ins and leases stay first fit, so account usage and settlement
  order remain predictable.
- **Unproven positive marks are not invented.** Steps whose success mark LR-1 proved assert it;
  the rest assert that the answer renders no refusal, the strongest fact stateable without a
  stack. Strengthening them is conformance-walk work, recorded below.

## Open questions
- Five transitions (return, refuse, reassign, acknowledge, reopen), the reassign form's
  assignee field and the weaker no-refusal marks have not yet been driven against a running
  stack; the first LR-5 run doubles as the conformance walk for them.
- The walk's token extractor moved from a boundary to the dictionary's regex rule with the same
  semantics; the next stand run re-proves it as a matter of course.
- The realized transitions-per-settlement of the wired scenarios (about 2.6 by the weights)
  sits beside the equilibrium constant's 2.5; whether the difference matters is a question for
  the first measured window, not for the wiring.
- The task registry starts a run empty, so the early minutes lean on creations; whether the
  seeded history can pre-register tasks - and how the rig learns their identities - remains the
  LR-4/LR-5 conversation.
- `acquireManagerOtherThan` is unused by the wiring: the discussion's not-the-creator demand is
  carried by the hot-task-outside-own-area pick. The acquisition stays as registry contract.

## Next steps
1. LR-4: the answers owed to the SUT project through the exchange desk - the accounts and
   managers count, the mechanism of convergence, the computed intensities and the skew, and the
   mix-survival answer this session made computable.
2. LR-5: the run harness - the documented command that loads a profile, builds the plan, runs
   it against the stack and lays down the capture artifacts, reporting the starvation ledger;
   its first run is the conformance walk of the open questions above.

## References
- Branch: `feature/lr3-scenario-wiring`.
- Decision records: DR-3, implemented and moved to Accepted by this session.
- Documents changed: `docs/roadmap.md` - LR-3 marked done.
