# Session note

## Type

`documentation`

## Goal

Answer the SUT project's letter of 2026-08-29 through the exchange desk, in one document: confirm
what it settles, correct one statement this project made, and ask what its seeding item must take
as input before it runs.

## Completion criteria

1. One letter on the desk answers both documents received on 2026-08-29 and carries: the pool's
   composition confirmed as the seeding item's input, together with the credential the seeded
   accounts are given, and which side states it; the fifth refusal code acknowledged with the
   invariant it hands the SUT's own reader; the split of the code that carries two causes
   accepted, with the form it
   takes in the capture; the version source accepted, with the one request it costs their logs;
   the correction of this project's statement about the message a reassignment requires; and the
   deferral of the read-growth question agreed.
2. The same letter asks the four questions the answer leaves open: whether the census the profile
   needs is an input of the seeding item, how deep an inventory walk before the window is
   acceptable given what it does to the buffer pool the SUT measures, how the seeded due dates are
   distributed, and whether the two rows of the closed transition table their manager walk never
   uses are a deliberate narrowing of the load model.
3. `exchange/README.md` registers the letter and names it on the rows of both received documents.
4. This note is completed and its row is added to the sessions registry when the branch merges.

## Scope boundaries

No code and no javadoc. `docs/brief.md` and `docs/roadmap.md` are not touched: nothing in this
letter is a fact received, it is a statement made and a question asked, and what the reply changes
is carried when the reply lands. LR-10 is not designed here, and the two omitted rows of the
transition table are asked about rather than decided.

## Status

`completed`

## Result

One letter is on the desk answering both documents received on 2026-08-29, and the exchange
registry names it on their rows and on its own. All four completion criteria are met. The letter
carries the six settled points with the place each one landed, the correction of this project's
statement about the message a reassignment requires, the pool confirmed as the seeding item's input
together with the credential that input still lacks, and four questions each stating the assumption
this project holds while it is unanswered.

## Decisions

- **The split of the refusal code that carries two causes lives in the run report, not in the
  result log.** The log stays the plain per-sample export the capture consumer was promised; the
  report carries the refusal counts by code with that one code broken by whether the request held a
  token. The alternative — a per-sample marker in the log — would change the export's shape for
  every consumer to serve one question, and the log is the artifact another project's scenario
  reads.
- **The pool size is not recorded on this side.** It is the SUT's observation of its own stack and
  reaches a capture through its collectors; a second copy here is a number nobody keeps aligned.
- **The two rows of the closed transition table that the manager walk never uses are asked about,
  not decided.** They are real user behaviour, so their absence is either a deliberate narrowing of
  the load model or an omission of the walk — and the two answers change the mix and the
  equilibrium arithmetic differently. The scenario specification is the SUT's document, so the
  question is theirs to settle.

## Open questions

The four the letter asks, each held under a stated assumption so that no work stalls: whether the
census is an input of the seeding item; how deep a pre-window inventory walk is acceptable, given
that a deep one warms the buffer pool whose slope the SUT measures; how the seeded due dates fall,
on which the hot-set skew depends; and whether the two unused rows of the closed table are a
deliberate narrowing. The credential of the seeded accounts is open in the same sense: the letter
asks which side states it, and proposes that the seeding item does, since it owns the initial
state. The value itself is not written on the desk — a document there travels — and passes between
the projects directly.

## Next steps

1. Deliver the letter to the SUT project's desk.
2. The `implementation` session already owed: the version read from the stand before the window,
   the refusal counts split by whether the request carried a token, the correction of the rig
   configuration's statement that the stack publishes no version, and the transition table's
   documentation.
3. LR-10, built under the assumptions this letter states, so that an answer removes work rather
   than adding it.

## References

- Branch: `docs/reply-to-the-sut-answer`
- Changed: the exchange desk outside the repository — the letter and the registry
- Decision records: none
