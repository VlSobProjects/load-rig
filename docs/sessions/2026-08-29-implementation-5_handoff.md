# Session 2026-08-29-implementation-5

## Type
`implementation`

## Goal
Implement LR-5: the run harness — a documented command that loads a profile, builds the plan,
runs it against the stack and lays down the capture artifacts — and prove it with the first
profile run, which doubles as the conformance walk of the transitions and marks no stand run
has driven yet.

## Completion criteria
- A documented Gradle command runs a profile against the stack: it loads the stated profile
  file, holds the invariants, assembles the plan over the account pool and executes it with the
  JTL timestamp semantics and the injector language set explicitly, shared with the smoke run
  rather than duplicated.
- Each run lays its artifacts into a directory of its own under the results root, named by the
  profile and the run's UTC stamp, so a capture consumes one directory: the load-profile
  description, the JTL result log, and the harness's own run report.
- The load-profile description is authored JSON stating what the run was meant to apply: the
  profile's stated facts and the computed target rates — the per-operation intensities the
  profile implies — plus the ramp, the steady window, the populations, the skew, the run stamp
  and the version of the SUT under test, taken from configuration and written as `unknown` when
  no version is stated.
- The run report carries what the console said: the run's parameters, the sample and error
  counts, and the starvation ledger's skip counts by step, so a drifted realized mix names
  where it drifted.
- A run with refused samples exits non-zero and says so; the artifacts stay on disk either way,
  because a spoiled run is evidence too.
- The description writer is unit-tested without a stack; `./gradlew build` passes from the
  command line.
- One run of the day profile against the standing stack is made and read: the conformance walk
  of the five transitions, the reassign form and the weaker no-refusal marks that LR-3 left
  unproven.
- `README.md` documents the command; `docs/brief.md`'s artifact list carries the run report;
  `docs/roadmap.md` marks LR-5 done in the merge.

## Scope boundaries
No morning or evening profile variants and no stepped calibration run (LR-6), no defect
variants (LR-7), no pre-registration of seeded hot tasks — that conversation waits for the SUT
project's answer. The smoke run and the provisioning command keep their behaviour.

## Status
`completed`

## Result
Every criterion is met; the conformance walk took four runs and paid for itself three times.

- The harness exists as the documented `runProfile` command: it loads the stated profile file,
  holds the invariants, assembles the plan over the account pool and drives the stack, with the
  JTL timestamp semantics and the injector language applied by one class the smoke run now
  shares. Each run lays a directory of its own under the results root — the load-profile
  description, the JTL log, the run report — and a run with refused samples exits non-zero with
  its artifacts kept.
- The description artifact carries the profile's stated facts, the computed per-operation
  target rates, the run stamp and the SUT version — a configuration value recorded as unknown
  until somebody states one — and is written before the load starts, so a dying injector still
  leaves the statement of intent. It is unit-tested against the repository's day profile.
- The first run refused nearly half its samples and every sign-in: the conformance walk's first
  finding was the rig's own — the transport context preprocessor cleared the injector's cookie
  store with `CookieManager.clear()`, which wipes every property of the running element and
  stops it keeping what the answers set, so the sign-in POST travelled without its session
  cookie into a CSRF refusal. Cookies are now removed one by one through the supported list
  mutation, and the "green" reads of that first run were the weak no-refusal marks passing on
  the login page — the recorded weak-marks risk observed in the wild.
- The walk then drove every transition of the table end to end and found two places the
  scenarios fell short of the specification: the reopen and the reassign were sent without the
  message the appendix marks required (the reassign never having reached the stand before), and
  the stand refuses both with 422. Both now carry a named business reason, and the fourth run
  accepted every transition.
- Probing the stand status by status established a visibility rule the specification does not
  state: a returned task is invisible to its assignee — gone from their list, 403 on a direct
  open — while every other status stays visible. The registry's visibility mirrors it, with a
  unit test; the rule went to the SUT project for confirmation and documentation.
- The fourth run's only refusals are the concurrent-delete race: a handful of reads of a task
  deleted seconds earlier answer 404, which is real user behaviour on a stale list row. Their
  classification is an open question below.
- Measured against the profile's targets, the realized mix drifted where the registry starved:
  the transitions landed four times under target, the lists a quarter above, the total
  intensity holding near target — because the registry starts a run knowing only the run's own
  creations and the seeded fifty thousand rows are invisible to it. The user's monitoring saw
  the same story from the SUT side: the early list surge, and a buffer-pool read slope
  consistent with the broken-equilibrium window. The warm start became roadmap item LR-10; the
  accepted accuracy band is ten percent either way, with misses fixed or precisely justified.

Verified from the command line: `./gradlew build` passes (82 unit tests, no stack); four
`./gradlew runProfile` runs against the standing stack on 2026-08-29, the fourth realizing
1519 samples with seven refusals, all of them the delete race, and every transition accepted.

## Decisions
- **The artifact layout is a directory per run**, named by the profile and the UTC stamp: the
  capture consumes one directory whole and never pairs files by guessing at names.
- **The description states computed target rates, not only the profile's stated facts**, so the
  capture's reader compares achieved intensity against explicit targets; the rates are computed
  by the same code the validation uses, never restated by hand.
- **The run report is the third artifact**: what the console said — counts and the starvation
  ledger — lands beside the capture, and the brief's artifact list says so.
- **The SUT version is configuration, unknown when unstated**: the stand publishes no version
  the rig could read, so the description records the operator's statement and never invents
  one; the exchange letter asks the SUT project for a publishable version.
- **Refused samples fail the run but never cost the artifacts**: a spoiled capture is evidence,
  and four of them now sit beside the clean one proving it.
- **The rig follows the specification, not the stand's tolerance**: both 422 fixes restore
  fields the appendix already marked required, the visibility fix stops the rig doing what no
  browser user can do, and no assertion was weakened to make a run pass.

## Open questions
- How the capture classifies the concurrent-delete 404s — the rig's ledger, a failed run, or a
  narrowed pick — waits on the SUT project's answer about whether their monitoring treats them
  as the expected trace of concurrent deletion.
- The weak no-refusal marks remain weak: the first run showed them passing on the login page.
  Strengthening them per step against captured answers is unfinished conformance work.
- Whether the buffer-pool read slope survives an equilibrium window is unanswerable until the
  warm start (LR-10) restores the balance inside a window.
- The sub-ten-percent drifts of the fourth run — opens under, reports and creations above —
  are inside the accepted band but unexplained in detail; the equilibrium re-measure will show
  whether they move with the starvation or stand on their own.

## Next steps
1. LR-10: the warm start and the scaled balance — the rig acts on every task the stand holds,
   a set-up group brings the stand to the computed census before the window, and the balance
   scales with the profile.
2. When the SUT project answers the questions letter, write what it changes into `docs/` and
   settle the 404 classification.
3. LR-6 after the baseline holds the ten-percent band: the stepped maximum-finding run and the
   morning and evening variants.

## References
- Branch: `feature/lr5-run-harness`.
- Decision records: none opened; the artifact decisions above are recorded here and in
  `docs/brief.md`'s artifact list.
- Documents changed: `README.md` — the run commands; `docs/brief.md` — the artifact list;
  `docs/roadmap.md` — LR-5 done, LR-10 registered; `CLAUDE.md` — the run command. The
  questions letter and its registry row live on the exchange desk, outside git.
