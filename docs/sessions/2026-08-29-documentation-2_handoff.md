# Session 2026-08-29-documentation-2

## Type
`documentation`

## Goal
Deliver LR-4: the answers the SUT project's scenarios-and-load-profile specification asks back,
authored through the exchange desk, with what the answer changes written into `docs/`.

## Completion criteria
- An answer document lies on the exchange desk addressing the specification's questions one to
  four with the rig's actual numbers and mechanisms: the account pool's composition and names,
  the mix-survival computation and its result, the convergence mechanism of the discussion, and
  the profile file as the explicit configuration owning the intensities and the skew.
- Question five is answered honestly as not yet answerable: the first measured baseline is
  calibration work (LR-6) and follows the run harness (LR-5).
- The question the rig owes back — whether the seeded history can pre-register hot tasks, and
  how the rig learns their identities — is asked in the same document, because the registry
  starts a run empty and the early window leans on creations.
- The exchange registry records the sent answer; the received specification's row points to it.
- `docs/roadmap.md` marks LR-4 done; `docs/brief.md`'s owed-answers section reflects what was
  delivered and what remains.
- The session closes with a commit on a `docs/` branch.

## Scope boundaries
This session changes no code and no profile: every number in the answer is quoted from
configuration and code that already exist. It does not run anything against the stack, does not
author the run harness (LR-5) and does not negotiate the host map's open points, which stay
listed in `docs/brief.md`.

## Status
`completed`

## Result
Every criterion is met.

- The answer document lies on the exchange desk, answering the four answerable questions from
  what exists: the pool of twenty-five accounts with eight managers and the reasoning of its
  size; the mix-survival check as a computed, refusing invariant with the day populations
  reproducing the proposed mix exactly and the computed intensities quoted; the task registry's
  hot set with lease-free reads and the not-the-creator pick as the convergence mechanism; and
  the strictly loaded profile file as the single owner of the intensities and the skew, with the
  intensities computed from it rather than stated beside it.
- The fifth question is answered as owed, not guessed: the baseline follows the run harness, and
  its answer will carry the realized transitions-per-settlement beside the percentiles.
- The answer asks one question back: whether the seeding item can publish the identities of
  seeded hot tasks — id, status, creator, assignee, the due-soon fact — so the rig can
  pre-register them and the discussion has its hot set from the first minute.
- The exchange registry carries the sent row; the specification's row now points at the answer.
- `docs/roadmap.md` marks LR-4 done; `docs/brief.md` records which answers were delivered,
  where each is encoded, and that the fifth remains.

No code changed. `./gradlew build` was run before merge as the workflow requires.

## Decisions
- **The answer quotes, it does not own.** Every number in the letter names the code or the
  configuration that holds it — the pool constants, the profile file, the validation output —
  so the desk stays correspondence and the sources of truth stay in the repository, per the
  exchange rule.
- **Question five is refused rather than estimated.** An estimated baseline would be the exact
  confident-answer-about-a-system-that-does-not-exist the specification itself warns against;
  the honest answer is the dependency order: harness first, calibrated run second, letter third.
- **The seeded-hot-tasks offer is taken up as a question, not a requirement.** The rig converges
  without seeded marks, so the answer states the mechanism as sufficient and asks for the
  pre-registration form as an improvement of the early window, leaving the seeding item free to
  decline.

## Open questions
- The form in which the seeding item could publish seeded task identities, if it can: the
  LR-4/LR-5 conversation continues when the SUT project answers.
- The realized 2.6 transitions per settlement against the specification's 2.5: parked for the
  first measured window, restated in the answer so the other side is not surprised by it.

## Next steps
1. LR-5: the run harness — the documented command that loads a profile, builds the plan, runs it
   against the stack and lays down the capture artifacts, reporting the starvation ledger; its
   first run is the conformance walk of the open transitions and the weaker marks.
2. When the SUT project answers the seeded-hot-tasks question, write what it changes into
   `docs/` and, if the identities become available, register them at run start (LR-5 work).

## References
- Branch: `docs/lr4-answers-to-sut`.
- Decision records: none touched; the answer rests on DR-2 and DR-3 as implemented.
- Documents changed: `docs/roadmap.md` — LR-4 marked done; `docs/brief.md` — the owed-answers
  section records the delivery. The answer itself and the registry rows live on the exchange
  desk, outside git.
