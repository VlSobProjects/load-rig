# Session note

## Type

`implementation`

## Goal

Close the two fields the first runs could not fill — the SUT version and the cause behind a refusal
that carries two — and correct the two places in the code whose text the SUT's answer made untrue.

## Completion criteria

1. The version of the system under test is read from the stand once per run, before the window
   opens and outside the result log; an operator's stated version still wins over it, and a stand
   that does not answer leaves the field unknown instead of failing the run.
2. The run report counts the refusals by status code and splits the one code that carries two
   causes by whether the request held a token, while the result log keeps the shape the capture
   consumer was promised.
3. The report says plainly, when it appears, that the code no screen of the application produces
   cannot be user behaviour in a capture of this rig.
4. `RigConfiguration` no longer states that the stack publishes no version, and `TransitionTable`
   no longer claims to mirror the whole transition table where it mirrors the subset the profile's
   business acts need.
5. `./gradlew build` passes, and one run against the stand shows the version in the description and
   in the report and the refusal ledger filled.

## Scope boundaries

The profile's stated seeded volume is knowingly left untouched: the answer made it untrue, but what
it becomes is the warm-start item's subject, not this session's. The registry's mirror of the
transition table is documented, not widened. No reply to the desk, no new dependency: the version is
read with the platform's own client and parsed with the loader's parser.

## Status

`completed`

## Result

All five criteria are met. The harness asks the stand for its version before the window and the
capture's description and report carry it instead of `unknown`; an operator's stated version still
wins. The run report counts refusals by status code, splits the one code that carries two causes by
the token the request held, and says of the code no screen produces that no person can have made
it. The two untrue texts are corrected. `./gradlew build` passes, and three runs against the stand
proved it end to end: the version reached both artifacts, and a ten-minute window filled the ledger
with the four concurrent-delete answers the SUT confirmed as expected behaviour.

## Decisions

- **The tally sits in the thread group, not on the samplers.** It therefore runs after every
  request the group makes, and a step added later cannot escape it by forgetting to carry it. The
  alternative — attaching it where each mutating request is built — would have made the ledger a
  thing every future step must remember, which is the failure mode the step kit exists to prevent.
- **The stated version becomes an override, not the source.** The stand publishes its own version,
  so the operator's property is for the case the stand cannot name — a patched image, a local
  branch — and everybody else gets the stand's answer. A stand that does not answer leaves the
  field unknown rather than failing the run: refusing to start over a label would trade a whole
  stand window for a field.
- **What counts as a token is stated once.** The run's ledger asks of a thread's variable exactly
  what the session registry asks of a context, so the rule lives in the transport context and both
  read it. Two spellings of one rule drift, and this one decides which of two causes a refusal is
  attributed to.
- **A failed sample and a refusal are named apart in the report.** They are different facts: a
  sample fails when its content assertion is not satisfied, which includes the three screens that
  refuse under a successful status, while a refusal is a status the application answered with. The
  report previously called both refusals, which the new block would have contradicted.

## Open questions

None raised here. The four the desk carries are unchanged.

## Next steps

1. Let the harness carry the concurrent-delete `404` on the ledger instead of failing the run over
   it. The SUT confirmed those answers are real user behaviour — a stale list row clicked after a
   delete — and that a clean baseline may carry a handful, but the harness still marks such a
   capture spoiled: the ten-minute run of this session exited non-zero on four of them. The work
   needs a way for a step to tell "the task was deleted under me" from a genuine defect, so the
   tolerance is narrow and not a blanket.
2. LR-10 in the form the roadmap now states, under the assumptions the desk letter holds.
3. The profile's stated seeded volume, left untrue on purpose, belongs to that same item.

## References

- Branch: `feature/version-and-refusal-ledger`
- Added: `loadrig.run.SutVersion`, `loadrig.model.scenario.RefusalLedger`, and the tests of both
- Changed: `ProfilePlan`, `ProfileRun`, `RigConfiguration`, `TransportContext`, `TransitionTable`
- Decision records: none
