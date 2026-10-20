# Session note

## Type
`documentation`

## Goal
Land the SUT project's answer EX-31 to EX-20, EX-28 and EX-29 in this repository, and answer the
three things it asks.

## Completion criteria
- DR-10 states what the answer settled — a cold restart on every application of a profile, the
  volume chosen by the SUT project, the stand as the source of the profile's name — and is
  accepted.
- The brief states the stand profile as the initial state, the generator row held by one rig at a
  time, the collector that observes the generator, where its artefacts go and the conditions on the
  starved-host fault, and its open points of the host agreement keep only what is still open.
- The roadmap's LR-7, LR-13, LR-14, LR-15 and LR-17 match the answer.
- A draft answer to the SUT project stands on the desk with the container name, the image name and
  when the top step's census comes.
- `What it changed` is filled for EX-20, EX-28, EX-29 and EX-31 after the commit.

## Scope boundaries
- No code changes: the container, the readings and the campaign runner stay their own items.
- The search's criterion (DR-7, LR-11) is not revisited.
- EX-33, addressed to k6-sut-task-tracker, is not acted on.

## Status
`completed`

## Result
- DR-10 is accepted. It now states that applying a profile is a pipeline step that runs on a stand
  that is up and restarts the database and the application cold, that the SUT project chooses the
  volume against the buffer pool with every rig's census as a lower bound, and that the stand is
  the source of the profile's name, which the run's own readings check.
- The brief states the stand profile as the initial state; the generator row held by one rig at a
  time through a shared container name; the stack's collector observing the generator by its
  image; artefacts outside every volume of the stand; the core set narrowed only on the set; and
  the conditions on the starved-host fault. Its open points keep the volume, the buffer pool and
  the time a profile takes to apply.
- The roadmap's LR-13 drops the levelling in both directions; LR-14 takes the shared container
  name, removal on exit and the image name; LR-15 checks the profile's name; LR-17 sets the
  profile before each step and sizes the settling interval for a cold start; LR-7 takes the
  conditions on the starved injector.
- EX-34, the draft answer to EX-31, stands on the desk.

## Decisions
- The generator's container is removed when it exits, so a failed run never holds the shared
  lock. This project's addition, stated in EX-34.
- The image's repository name is `load-rig-generator`, chosen by the user.
- The description carries the profile the campaign asked for, and the readings of LR-15 check it
  against the stand's own report, instead of the description reading the stand's scrape.

## Open questions
- The volume, the buffer pool and the time applying a profile takes, all fixed when the SUT
  project builds `RM-3`, after its indexed report.

## Next steps
1. The user releases EX-34.
2. When `RM-3` lands: the warm start becomes reading and checking, the provisioning walk retires,
   and the baseline is measured again on the first profile.
3. The census for the search's top step goes to the SUT project when the search's campaign is
   stated.

## References
- Branch `docs/sut-answer-stand-profile`.
- `docs/decisions/dr-10-the-sut-owns-the-initial-state.md`, `docs/brief.md`, `docs/roadmap.md`.
- Desk: EX-31 (answers EX-20, EX-28, EX-29), answered by EX-34.
