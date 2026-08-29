# Session note

## Type

`documentation`

## Goal

Carry the SUT project's answer of 2026-08-29 into `docs/`: the facts it settles, the ownership it
moves, and the false premise the warm-start item was written on.

## Completion criteria

1. `docs/brief.md` carries, as binding facts received: the closed list of the five refusal codes
   the task surface answers with, the two causes that share the `403` and where the split lives,
   and the address the SUT version is read from.
2. `docs/brief.md` records that the initial state of a run — the accounts and the seeded tasks
   both — becomes the SUT's to own under its seeding item, and that this project provisions
   nothing once that item lands.
3. `docs/roadmap.md` LR-10 is rewritten on the true premise: the measured stand held no seeded
   history, so the item computes the census and brings the stand to it in a form that works on an
   empty stand and on a seeded one alike.
4. `docs/roadmap.md` LR-9 states that the provisioning walk retires when the SUT owns the initial
   state, and that its idempotence is the guarantee that must hold until then.
5. `exchange/README.md` marks both letters this project sent as answered, names where each answer
   went, and registers the two documents received on 2026-08-29.
6. This note is completed and its row is added to the sessions registry when the branch merges.

## Scope boundaries

This session changes no code and no javadoc. Three findings of the analysis behind it are
deliberately left to their own sessions and recorded in the next steps: the version probe and the
`403` ledger split, the correction of `RigConfiguration`'s statement that the stack publishes no
version, and the javadoc of `TransitionTable`, which claims to mirror the SUT's transition table
where it mirrors the subset the profile's business acts need.

No reply letter is written back through the desk in this session, and no decision record is opened:
nothing here is a shaping choice of this project, only the recording of facts another project
settled.

## Status

`completed`

## Result

All six completion criteria are met. `docs/brief.md` gained the closed list of refusal codes, the
obligation to split the one code that carries two causes, the version read from the stand rather
than stated by an operator, and the ownership of the initial state; its account of the answers owed
to the SUT project now records that the question asked back has been answered. `docs/roadmap.md`
carries LR-10 rewritten on the true premise and LR-9 amended with the walk's retirement. The
exchange registry marks both sent letters answered and registers the two documents received.

## Decisions

- **The warm start reads and tops up, rather than choosing between the two.** The set-up group
  computes the census, reads what the stand holds from the list page ordered by due date, and
  creates only the difference. The alternatives were a group that creates the whole census — which
  works today and is thrown away when the seeding lands — and a group that only reads, which works
  only after the seeding and would block calibration on another project's schedule. The chosen form
  degenerates to each of them at the two ends and needs no rewrite in between. Its cost is that the
  census must be computed before the window on every run, including runs where the stand already
  satisfies it.
- **The registry's transition table stays a subset of the SUT's, and says so.** The verbatim
  extract shows two rows the mirror does not carry: a reassignment from the open status, and a
  reopen from the two statuses the creator has not yet reviewed. Both are absent from the scenario
  specification's manager walk, which reassigns only in answer to a returned question and puts back
  to work only what is already settled, and the specification's equilibrium arithmetic is built on
  that same set. Widening the mirror would add two rules the profile never drives and disturb the
  equilibrium check for nothing; the honest correction is that the class stops claiming to mirror
  the whole table. No decision record: the choice belongs to the SUT's model, not to this project's
  shape.
- **The version is asked once, before the window, off the result log.** The stand publishes it on
  an information endpoint outside the task surface. The brief's rule that operational endpoints
  stay out of the scenarios is unchanged — the ask is not a sample and does not enter the timer a
  verdict is read from.

## Open questions

- The pool's composition — twenty-five accounts, of them one administrator, eight managers and
  sixteen workers — is the seeding item's input and is to be settled through the desk before that
  item runs. Nothing collides today, but the confirmation has not been sent.
- The expected growth of logical reads at seeded volume under a balanced load stays unanswerable
  until the seeding fixes the volume and the buffer pool, as this project asked and the SUT project
  agreed.

## Next steps

1. An `implementation` session: the version read from the stand before the window, the refused
   samples split by whether the request carried a token, and the statement in the rig
   configuration that the stack publishes no version, which is no longer true.
2. An `implementation` session, or the same one if it stays small: the transition table's
   documentation, so that the two omitted rows read as a deliberate narrowing to the profile's
   business acts instead of an oversight.
3. A reply through the desk: the pool's composition confirmed as the seeding item's input, the
   fifth refusal code acknowledged, and the correction that a reassignment requires its message
   only in answer to a returned question.
4. LR-10 itself, in the form this session recorded.

## References

- Branch: `docs/sut-answers-after-first-runs`
- Changed: `docs/brief.md`, `docs/roadmap.md`, and the exchange registry outside the repository
- Decision records: none
