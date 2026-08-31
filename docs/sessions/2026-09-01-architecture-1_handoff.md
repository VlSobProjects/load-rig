# Session note

## Type

`architecture`

## Goal

Settle what the calibration probe made unavoidable: how the load model stops holding a fixed set of
people on a stand that is whatever the last run left, and record it as the decision the next item
implements.

## Completion criteria

1. The four properties the probe measured are stated as the problem, with what each one costs a
   capture.
2. Each of the three shaping choices — how the population turns over, what the stand holds when a
   window opens, and how much the window itself deletes — is weighed against at least one
   alternative with its cost.
3. The decision says plainly which earlier rule it amends and how that rule now reads.
4. The roadmap carries the item that implements it, in the order the work is done, and the brief
   carries what the decision makes inaccurate there.
5. What the decision cannot deliver is stated in it, so that no later reader credits it with the
   database's disk.

## Scope boundaries

No code and no profile file: the shares, the depth of the rotation and the stated volume are
numbers the implementing item and its campaign choose, not this record.

The seeded volume and the buffer pool stay the SUT project's; they leave as questions for the next
letter and are not decided here.

## Status

`completed`

## Result

All five criteria are met. [DR-8](../decisions/dr-8-the-population-a-campaign-plays.md) is drafted,
LR-13 is registered before LR-11 in the roadmap, LR-11 carries why it waits, and the brief's profile
discipline carries the amended circulation rule and the stated initial state.

The probe's four findings are the record's context: nothing in the model ends a session, so nobody
signs in after the ramp and that band measures a cold start; the people a step draws from are as few
as its seats; the stand is whatever the previous run left, which is why the report band moved across
the ladder while the intensity was not what moved it; and the mix drifts the table upward by
construction.

Six rules follow: a session ends at a stated share so sign-ins recur; the account that signs in is
chosen from the rig's own registry by what it holds; the players are wider than the seats with the
depth a campaign parameter; DR-6's circulation rule is amended to the accounts a run plays over its
window, with the census stocking all of them; the initial state is the campaign's stated figure and
the warm start levels to it in both directions; and deletion runs at a stated share with its pick
unbiased.

## Decisions

- **[DR-8](../decisions/dr-8-the-population-a-campaign-plays.md).** The six rules above, each with
  the alternative it was chosen over.
- **DR-6 is amended rather than superseded.** Its measured finding stands — work handed to accounts
  nobody signs in as starves a run — and only its reading of "occupies" changes: an account between
  sessions waits its turn instead of being absent.
- **The choice rule reads the rig's registry, not a rendered report.** Taking the same fact out of
  the report's answer would measure the SUT to learn what the rig already holds and tie a choice
  rule to a page's markup.
- **Deletion is not raised to compensation.** Five percent would describe a business nobody runs;
  the specification is explicit that a real deletion is rare. The share is raised, stated, and sent
  to the desk as this project's deviation.
- **The pick of a deleted task stays unbiased.** Steering it at the hot set would manufacture
  contention no population produces, which is the false finding this project is required not to
  produce.

## Open questions

- The three numbers the decision deliberately leaves open: the share a session ends at, the depth of
  the rotation, and the volume the campaign states. They are the implementing item's, measured
  against what the stand can establish in a warm start of acceptable length.
- DR-7's derived criterion has no reference point until this lands: its rule stands, but the quiet
  run it derives from cannot be a campaign's coldest one.
- Two questions for the next letter, neither of them this project's to answer: the seeded volume,
  and the database's buffer pool — the second decides whether a wider working set reaches the disk
  at all.

## Next steps

1. LR-13: implement the six rules, with the three open numbers chosen and stated.
2. The intensity guard as a line of the run report, carried from the probe session.
3. A letter to the desk: the amended pool composition, the deletion share, and the buffer pool
   question.
4. LR-11 after all of it, with the criterion's reference point re-chosen.

## References

- Branch: `docs/dr_8-the-population-a-campaign-plays`
- Added: `docs/decisions/dr-8-the-population-a-campaign-plays.md`
- Documents changed: `docs/roadmap.md` (LR-13 registered before LR-11; LR-11 carries why it waits),
  `docs/brief.md` (the profile discipline's circulation rule and the stated initial state)
