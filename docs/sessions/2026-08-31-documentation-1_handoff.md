# Session note

## Type

`documentation`

## Goal

Deliver the fifth answer the SUT specification asked for — the first measured baseline against the
service levels — through the exchange desk, and ask with it the two questions the measurement
raised that only the other project can settle.

## Completion criteria

1. The letter states the baseline as a measurement: the run's conditions, what the stand held, the
   realized percentile per band against the figure it was held to, and the half of the verdict the
   rig cannot reach.
2. It states plainly what the headroom does to the assumed ceiling of twenty virtual users, and
   under what criterion the maximum will be reported, so that the number is never quoted without
   it.
3. Each question carries the assumption this project holds while it is unanswered, so neither side
   waits on the other.
4. The desk registry carries the row, and what the letter changes on this side is written into
   `docs/` rather than left in the letter.

## Scope boundaries

No stand window and no code: the measurement being delivered was taken by the session that built
the ledger. No new decision record — DR-7 is the decision this letter reports, not one it makes.

The credential, the seeding's composition and the questions already asked before the seeding are
not reopened here.

## Status

`completed`

## Result

All four criteria are met. The letter went to the desk, the registry row records it and marks the
specification's fifth question answered, and the brief carries what the letter changes here.

- **The answer is a measurement, not an estimate.** The day profile at ten virtual users, the stand
  it ran against including the volume it was measured to hold, the six bands with their realized
  percentile by nearest rank, the sample count each was judged on and the worst wait of the window.
  The figures are read from the capture's own artefacts.
- **The ceiling hypothesis is answered in the plainest available form.** Between fourteen and
  seventy times inside the figures, so twenty virtual users is not a maximum and is not near one.
  Two observations shape it: the tightest band is signing in, whose cost is password hashing and
  which has least to do with the business the profile drives, and the loosest is the report, which
  is loose because it scanned a table of under six hundred rows.
- **The criterion of the coming maximum is stated before the number exists**, so the other project
  never meets the figure without it: the search is a campaign of its own judged by levels derived
  from this baseline, and what it will report is a lower bound of what the chosen figures would
  give.
- **The half-verdict is asked back rather than claimed.** Whether the log stayed quiet and the
  throttled-period counter flat over that window is theirs to read, and the letter proposes
  coordinating the next baseline so both halves cover one window.

## Decisions

- **The two levers this project does not own leave as questions, not as requests.** The core set is
  asked about as an option for a later campaign, with the one condition that only the set may move —
  a bound quota raises the throttled-period counter and the calibration rule then reads the capture
  as an artefact of the limit. The seeded volume is asked as a pair with the item that reduces the
  report to an indexed query, because the two together decide whether the report stays the loosest
  band or becomes the first to break.
- **The assumption held while they are unanswered is stated in the letter:** the campaign runs under
  the map as written, and a maximum is reported as a maximum at the volume it was found at, with
  every step publishing that volume.
- **The baseline will be re-measured once, after the seeding lands**, rather than twice around it.

## Open questions

- Whether the other project's collectors can still answer for the window of 2026-08-31. If they
  cannot, the baseline's other half is closed only by a coordinated re-run.
- Both questions the letter asks remain open by construction; they are carried on the desk registry
  and in the brief's open points.

## Next steps

1. LR-11, in two parts: the calibration run that fixes the multiple, then the stepped campaign
   against the derived file.
2. LR-12: the morning and evening profile variants.
3. Re-measure the baseline after the seeding item lands, and re-ask nothing that this letter
   already asked.

## References

- Branch: `docs/fifth-answer-first-measured-baseline`
- Desk: the letter of 2026-08-31 and its registry row, both outside git
- Documents changed: `docs/brief.md` (the fifth answer delivered; two open points added, one
  amended)
