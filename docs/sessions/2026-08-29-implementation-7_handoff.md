# Session note

## Type

`implementation`

## Goal

Let a clean run carry the `404`s of concurrent deletion — which the SUT confirmed as real user
behaviour a clean baseline may hold — without the harness declaring the capture spoiled, and
without the rig going blind to a `404` that is its own defect.

## Completion criteria

1. A `404` answering a step that acts on a task the registry handed it does not fail the run, and
   is counted and named in the run report as what it is.
2. A `404` answering anything else still fails the run: the tolerance is narrow by construction and
   not a blanket over the code.
3. The tolerated count is bounded by a figure the profile itself states rather than by an invented
   one, and a count above that bound spoils the capture as loudly as today.
4. The result log keeps the status the application actually answered with; nothing in the capture
   claims the request succeeded on the wire.
5. `./gradlew build` passes, and a run against the stand shows a window carrying such answers
   ending clean.

## Scope boundaries

Only the classification of this one answer changes. No other refusal code becomes tolerable, the
registry's own bookkeeping of a task that disappeared under a session is left as it is unless the
tolerance requires it, and the warm start and the profile's stated seeded volume remain the
warm-start item's subject.

## Status

`completed`

## Result

All five criteria are met. A step acting on a task the registry handed it may now meet the answer
that the task is gone: the registry forgets it, the ledger records the task, the sample stops
counting as a failure of the script, and the run report names what happened. Everything else meeting
the same answer still fails the run. The tolerated losses are bounded by the deletions the profile
itself intends over its window, and a count above that bound spoils the capture as loudly as before.
`./gradlew build` passes with 97 tests, and a full ten-minute run on the final build ended clean —
1523 samples, none failed, one task lost under a session — where such a window used to exit
non-zero.

## Decisions

- **The tolerance is attached to requests, not granted to a code.** It lives on the three request
  shapes that name a task the registry picked — opening one task, the discussion's two, and the
  transition — so a list, a report or a sign-in meeting the same answer still fails the run: nothing
  could have deleted what those asked for. The alternative, tolerating the code wherever it appears,
  would have made the rig blind to an address it built wrongly or an identity it read wrongly.
- **The bound counts tasks, not answers.** The first working version counted every tolerated answer
  and held that against the deletions the profile intends — two different things, because several
  sessions can meet one deletion. A one-minute run hit the bound exactly and showed it: the ledger
  now holds the distinct tasks lost, so the two sides of the comparison measure the same thing.
- **The bound is the profile's own intention, rounded up.** A session can only lose a task somebody
  deleted, so the deletions the profile means to make over the window are the natural ceiling, and
  no figure had to be invented. Rounded up, so that a run is never spoiled for behaving exactly as
  the profile asked.
- **Nothing depends on the order the post-processors run in.** The outcome recorders return early on
  the answer that the task is gone, and the tolerance only flips the sample's flag and records the
  task, so neither needs to run before the other. The alternative — relying on JMeter's scoping
  order — would have been a correctness argument nobody can check by reading the code.
- **The result log keeps the status the application answered with.** Only the sample's success flag
  changes, and that flag means the step got an answer it was allowed to meet, not that the wire
  returned success.

## Open questions

Whether the bound behaves well on windows short enough for the deletion intensity to be noise: a
one-minute profile intends two deletions and a capture window intends twelve. It is a property of
the scratch profiles used for verification, not of a capture, so nothing is done about it yet.

## Next steps

1. LR-10 in the form the roadmap states, with the profile's stated seeded volume made honest as
   part of it.
2. LR-6 afterwards: the calibration run and the first measured baseline, which is the fifth answer
   still owed to the SUT project.

## References

- Branch: `fix/tolerated-concurrent-delete`
- Changed: `TaskRegistry`, `RefusalLedger`, `ScenarioWiring`, `ProfilePlan`, `CommonSteps`,
  `ManagerScenario`, `ProfileRun`, and the tests of the registry, the ledger and the bound
- Decision records: none
