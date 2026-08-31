# Session note

## Type

`implementation`

## Goal

Fix the multiple the stepped search will be judged by, as DR-7 requires: grow the account pool to
the campaign's top step, then spend a calibration run of two or three steps to find where the
response curve leaves its flat part and which side gives first.

## Completion criteria

1. The pool carries the seats a proportionally scaled day profile needs at the campaign's top
   step, and the seating refuses nothing up to it.
2. The probe drives those steps against the stand and each step's realized intensity is checked
   against its target, because a step short of it measured the generator.
3. The multiple is fixed from what the probe shows, or the hypothesis behind it is corrected on
   the record.
4. The probe's captures are not published as evidence: they are spent, as DR-7 says.
5. `./gradlew build` passes from the command line.

## Scope boundaries

No campaign and no derived service-levels file: this session buys the number that file is written
from. The profiles the probe drives are scratch files outside the repository, because how a
campaign expresses its steps is not decided here.

## Status

`completed`

## Result

The pool grew, the probe ran, and it answered a different question than the one it was sent to
answer. The multiple is not fixed, and the reason it is not is the session's finding.

**The pool.** Sized to the top step of the search rather than to twenty virtual users: forty
workers, thirty-two managers and eight administrators, which are the seats a day profile scaled
eight times needs at once. What decides the counts is the seats a scaled profile holds, not a share
of the mix. Twenty was the wrong ceiling to size against twice over — the search has to climb far
past it, and a proportional double already asked for a second administrator the pool did not hold,
which the seating check refused before any window was spent. Provisioning against the stand created
fifty-five accounts, proved every sign-in and left the twenty-five already present untouched.

**A defect of the rig, found by the third step.** The warm start refused to read a stand holding
more than a thousand tasks: the list page states its count with the application's own digit
grouping — `1,063 in total` — and the expression that reads the paging state accepted digits alone.
Written against a stand of a few hundred, it waited for the thousandth task. This is the fourth row
of DR-5's adaptation ledger, priced there as small and localized, and it cost exactly that: one
expression, one parse and a test on both shapes of the answer.

**The ladder.** Each step is the day profile's shape with the populations scaled, a sixty-second
ramp and a four-minute window. The 95th percentile by nearest rank, in milliseconds:

| Step | Virtual users | Samples | List | Task | Action | Note | Sign-in | Report | Stand at start |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| baseline | 10 | 1501 | 39 | 38 | 58 | 43 | 144 | 43 | 591 |
| x2 | 20 | 1343 | 37 | 34 | 56 | 33 | 131 | 36 | 661 |
| x4 | 40 | 2683 | 35 | 30 | 51 | 30 | 95 | 36 | 778 |
| x8 | 80 | 5282 | 31 | 22 | 47 | 26 | 119 | 61 | 1063 |

No step failed a sample, none was refused beyond one tolerated concurrent delete, and the worst
wait of the highest step was 181 ms against a hard ceiling of ten seconds.

**The realized intensity held at every step**, which is the guard DR-7 attaches to a step before
its percentiles may be read. Against the targets the description states, per kind: every kind
within 2.6 % at the baseline and within 2.5 % at eight times it, the whole mix 1.2 % and 0.5 %
under target respectively. The only larger deviation is the baseline's deletions at 7.6 % under,
which is eleven samples and the noise of a small count. The generator held eighty sessions on its
one physical core without shortfall.

**What the probe actually established.**

- There is no knee below eighty virtual users. The curve is not flat but falling: five bands of six
  answer faster under eight times the load than at the baseline. The stack is idle at these
  intensities and the cost of a request is not what a saturated system's is.
- The premise of deriving the criterion from the clean baseline is therefore unsound as it stands.
  The baseline is the coldest point of a campaign — its sign-in band is 144 ms against 95 ms at four
  times the load — so a multiple of it measures the injector's warm-up as much as the system's
  degradation. A derived criterion needs a warm reference point, not the first minutes of a run.
- The only band that moves is the report, and it moves with the volume of the table rather than
  with the intensity: 36 ms to 61 ms as the stand grew from 778 to 1063 tasks. That is the confound
  DR-7 named, measured.
- The sign-in band is measured on the ramp alone. Nothing in the load model signs a session out, so
  after the ramp no account signs in again: twenty sign-in samples at ten users and one hundred and
  sixty at eighty are two requests per user and no more. The day profile's spread sign-ins are not
  realized, and the band's figure is a cold-start figure.

## Decisions

- **The pool is sized to the top step of the search, once for a campaign.** Growing it between
  steps would change the stand between the runs a capture is compared across. Accounts beyond a
  step's seats cost that step nothing, since the seating takes the first accounts of each role.
- **The names of the first twenty-five are unchanged**, so the composition already agreed with the
  SUT project is a prefix of the new one and nothing settled moves. The amendment travels through
  the desk when the campaign's top step is settled.
- **The paging state is read through the grouping the page renders it under.** The separator is part
  of what the list renders, so it is part of what is read.
- **The multiple stays unfixed, deliberately.** Fixing one from a probe that never left the flat
  part would be inventing the number the calibration run exists to measure.

## Open questions

- Where the derived criterion's reference point is, now that the baseline is known to be the cold
  end of the curve.
- DR-7's intensity guard was computed by hand from the result log here. It belongs in the run
  report, as realized steps per minute against the target per kind.
- Whether the search is worth climbing at all before the profile is denser — the population that
  plays, the volume the stand holds and the sign-ins that recur are all being decided by the next
  item.

## Next steps

1. LR-13 and its decision record: the profile's density — sessions that end and recur, a player set
   wider than a step's seats, the census over the rotation, an exact initial state the warm start
   levels to in both directions, and deletion at a stated share.
2. The intensity guard as a line of the run report.
3. LR-11 after both, with the reference point of the derived criterion re-chosen.

## References

- Branch: `feature/lr11-calibration-probe`
- Changed: `AccountPool` (the counts and what decides them), `TaskListReading` (the paging state
  read through its grouping) and its test
- Verified: `./gradlew build` from the command line, 136 unit tests, no failures; four runs against
  the standing stack on 2026-08-31 at SUT version 1.0.0, whose figures are above and whose captures
  are spent rather than published
