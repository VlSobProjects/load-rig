# Session note

## Type
`architecture`

## Goal
Answer EX-25 of the load-rig-locust project by deciding who owns the stand's initial state when two
rigs reproduce the same profile to compare their results, and put that decision to both projects
on the desk.

## Completion criteria
- A decision record states the owner of the initial state, with its alternatives and costs.
- The letters this project already sent to the SUT project are checked for what they say about the
  pool, the census and the volume, and the new letters follow them instead of repeating them.
- Two draft letters stand on the desk: the answer to EX-25 for load-rig-locust, and the proposal
  to the SUT project.

## Scope boundaries
- No code changes.
- The brief and the roadmap are not changed: the decision is a proposal until the SUT project
  answers, and they are corrected when it lands.
- EX-26, a draft of another project, is not read.
- No letter is released and no sent letter is edited.

## Status
`completed`

## Result
- DR-10 (Draft) states that the initial state passes to the SUT project as a named stand profile
  applied on request, with two alternatives rejected. The status lines of DR-6 and DR-8 point to
  the rules it amends.
- EX-19 and EX-20, both sent to the SUT project on 2026-10-19 and not yet answered, were checked.
  EX-20 already carries the pool's composition, the rotation and the day profile's census, and
  asks whether `RM-3` is open. Its first question assumed a volume that is not fixed, and that
  assumption is what the proposal changes.
- Two drafts stand on the desk: EX-27, the answer to EX-25 for load-rig-locust, and EX-28, the
  request to the SUT project that follows EX-20.

## Decisions
- Alternative 1 of DR-10: the SUT project owns the accounts and the initial state, the stand is set
  to a named profile before every run and every step, each rig states its demand, and a capture
  names its profile.
- EX-25's request that this project tell load-rig-locust of every change to the pool and the census
  is not taken as asked. The census is a per-run figure of this rig, not a state of the stand, so a
  change to the initial state reaches both rigs as a changed profile through the SUT project.

## Open questions
- Whether the SUT project takes the pool and the profiles, under `RM-3` or an item of its own.
- How long setting the stand to a profile takes, and who chooses the volume.
- Whether load-rig-locust agrees, and whether its draft EX-26 to the SUT project changes to match.

## Next steps
1. The user releases EX-27 and EX-28, or corrects them.
2. When both rigs agree, the user takes the letters to the SUT project in a session there.
3. When the SUT project answers: DR-10 is accepted or revised, the brief's initial state and the
   roadmap's LR-13 and LR-17 are corrected, and EX-25 and EX-28 are marked landed.
4. Until then the warm start and the provisioning walk stay as they are, and the levelling in both
   directions planned by DR-8 is not built.

## References
- Branch `docs/dr_10-the-sut-owns-the-initial-state`.
- `docs/decisions/dr-10-the-sut-owns-the-initial-state.md`; status lines of DR-6 and DR-8.
- Desk: EX-25 (answered by EX-27), EX-20 (followed by EX-28).
