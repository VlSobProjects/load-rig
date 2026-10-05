# Session note

## Type

`implementation`

## Goal

Make the service levels the rig's own explicit configuration, measured against every capture:
each run judges the percentiles it realized per service-level band, publishes the levels with the
capture and the verdict beside it, and the day profile is run against the stand to produce the
first measured figures.

## Completion criteria

1. The service levels are external configuration over a code-owned vocabulary, loaded strictly
   with no defaults, held for the whole campaign rather than stated per profile — a profile file
   neither carries them nor may.
2. Every step kind maps to a band of the specification's table, and the completeness of that
   mapping is held by a test rather than by an author's memory.
3. A run measures the 95th percentile and the maximum per band, judges them against the levels
   and states the verdict; a sample whose name no band claims is counted apart and reported, so a
   step that escapes the bands is visible instead of silent.
4. The levels land in the capture's load-profile description and the verdict in the run report;
   a breach is published as a finding and does not make the run fail, because the faulted
   variants this rig exists to produce are meant to breach.
5. The ledger, the percentile method and the loader are unit-tested without a stack, and
   `./gradlew build` passes from the command line.
6. One day-profile run against the standing stack leaves the first measured percentiles, and
   `README.md`, `docs/brief.md` and `docs/roadmap.md` carry what the change makes inaccurate.

## Scope boundaries

No stepped maximum-finding run and no morning or evening variants: LR-6 is split into three items
on the roadmap by this session, and those two are the next ones. The fifth answer owed to the SUT
project is written from these measurements in a documentation session of its own, not here.

The scenarios keep their steps, their weights, their names and their transport. The verdict is
the injector's half only: the quiet application log and the throttled-period counter are read
outside this project, and the report says so rather than implying a verdict it cannot reach.

## Status

`completed`

## Result

All six criteria are met, and the measurement they exist for produced its first figures.

- The service levels are the campaign's own file over a code-owned vocabulary: six bands from the
  specification's table, one 95th-percentile figure each and a hard ceiling over everything, loaded
  as strictly as a profile. A band left out, a band outside the closed list, an unknown key and a
  figure above the ceiling are each refused by name. They are deliberately not a profile's field:
  a morning and an evening variant judged by different figures would compare nothing.
- The band a sample is judged in comes from the kind of act that produced it, stated on the step
  kinds as an exhaustive switch - a kind added later cannot compile until somebody decides what
  the person waiting for it is entitled to. The step kit remembers which act every label it builds
  belongs to, the sign-ins declare themselves under the kind that is no row of the mix, and the
  assembled day plan leaves every one of the six bands with an act to judge, which is held by a
  test rather than by reading.
- A run times every sample the result log carries, states the percentile by nearest rank - the
  method named in the report, because two definitions over the same samples answer differently -
  and reports each band against the figure it was held to. Samples belonging to no band are counted
  apart and still held to the ceiling; on the measured run that count was zero.
- The levels travel in the capture's description and the verdict in the run report. A breach
  changes nothing about the run's outcome: the injector-side variants are meant to breach, and a
  run that failed over one could not capture them.
- Verified from the command line: `./gradlew build` passes with 135 unit tests and no stack. One
  day-profile window against the standing stack on 2026-08-31, SUT version 1.0.0: the warm start
  found 591 tasks on the stand, read 500 and created 22; 1501 samples, none failed, no refusals,
  three starved gates in ten minutes. The realized figures, at the 95th percentile against the
  levels: opening a list 39 ms of 1000, opening one task 38 of 1000, performing an action 58 of
  1000, writing a note 43 of 1000, signing in 144 of 2000, running a report 43 of 3000. The worst
  wait of the whole window was 164 ms, and nothing came near the ceiling. The ledger's attribution
  and its percentile were checked against the result log itself: 1501 rows, the same worst wait,
  and the same figure for the sign-ins computed from the file.
- That baseline is the session's real finding as much as its evidence: at ten virtual users this
  stand answers one to two orders of magnitude inside the chosen figures. The service levels as
  they stand are therefore not what will stop a stepped run anywhere near the assumed ceiling of
  twenty users, and the next item has to face that before it spends stand windows climbing.

## Decisions

No decision record was opened. The criterion is the SUT specification's own and this session only
encodes it; the choices below are implementation choices, and the shaping decision of the campaign
- how a stepped run raises intensity over one profile shape - belongs to the item that runs one.

- **The levels are the configuration of a campaign, not of a profile.** One file, one command-line
  key, carried into every capture's description as read. It is the discipline the specification
  attaches to a tunable criterion: fixed before a campaign, held across the baseline and the
  faulted run, recorded with the evidence.
- **The percentile is the criterion's shape and not a lever.** It stays a named constant and is
  refused as a key of the file: the figures are what a campaign calibrates, and a run that quietly
  judged itself at the median would leave a capture nobody could re-read.
- **A breach is a finding, never a failure of the run.** Refused samples and losses beyond the
  intended deletions still exit non-zero; a breached level is published and nothing else.
- **The verdict is the injector's half and says so in the artifact.** The specification's viability
  criteria are these levels together with the quiet application log and the flat throttled-period
  counter, and the rig drives no operational endpoint: claiming them would be claiming a
  measurement nobody took.
- **The ledger judges what the capture records.** The injector's own bookkeeping elements are
  marked ignored and stay out of the result log, so they stay out of the measurement too.
- **The discussion's two requests are judged in the note's band.** One business act reads the notes
  of a task and leaves a remark on it, and the specification gives the read and the write the same
  figure; splitting them here would invent a distinction it does not make.
- **LR-6 was split into three roadmap items.** The stepped maximum-finding run became LR-11 and the
  morning and evening variants LR-12, so that one item stays one session.

## Open questions

- The headroom measured here is so large that the levels cannot be the binding constraint of a
  stepped run at this stand's resources. Which of the four levers answers that - the resources the
  application and the database are given, or the figures themselves - is a campaign decision the
  next item must take before it climbs, and it must be taken before the campaign, not inside it.
- The sign-in band holds both the login page and the credentials post, which is what a person
  experiences as signing in; whether the two deserve separate figures is unasked, and at 144 ms
  against 2000 nothing forces the question yet.
- The bands are attributed by the label the plan gave a sample. Nothing in the result log itself
  carries the band, so a reader outside the rig re-derives it from the labels; whether the capture
  should carry the attribution is a question for the workflow-engine project's consumer.

## Next steps

1. LR-11: the stepped maximum-finding run over the same profile shape, with the lever question
   above settled first and recorded as a decision record.
2. LR-12: the morning and evening profile variants.
3. A documentation session delivering the fifth answer - the measured baseline above - to the SUT
   project through the exchange desk.

## References

- Branch: `feature/lr6-service-levels`
- Added: `ServiceLevelBand`, `ServiceLevels`, `ServiceLevelsLoader`, `ServiceLevelLedger`,
  `profiles/service-levels.json`, and their tests
- Changed: `StepKind` (the band of an act), `StepKit`, `SessionSteps`, `ScenarioWiring`,
  `ProfilePlan`, `RigConfiguration`, `ProfileRun`, `ProfileValidation`, `LoadProfileDescription`,
  `build.gradle`
- Documents changed: `README.md`, `docs/brief.md`, `docs/roadmap.md` (LR-6 rewritten, LR-11 and
  LR-12 registered)
