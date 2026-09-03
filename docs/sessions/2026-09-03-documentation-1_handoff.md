# Session note

## Type

`documentation`

## Goal

Deliver to the SUT project what it has not received: the first measured baseline, written on
2026-08-31 and never carried across the desk, together with a supplement stating what the
calibration ladder of that same evening established about the assumed ceiling of twenty virtual
users.

## Completion criteria

1. The baseline letter reaches the other desk unedited, its date being the date of the
   measurement rather than of the delivery.
2. The supplement states the ladder as a measurement — the four steps, the guard that lets their
   percentiles be read, and the worst wait of each — and states plainly that its runs are a spent
   probe and not captures, as DR-7 requires.
3. It answers the ceiling of twenty by measurement rather than by the baseline's argument from
   headroom, and gives the reason no maximum is reported yet.
4. The desk registry carries both rows, and the baseline's row records that it was delivered late.

## Scope boundaries

No new measurement and no stand window: this session publishes figures already taken. No new
question to the SUT project — the two asked on 2026-08-31 stand as they were asked, and the
seeded volume is sharpened by the ladder rather than re-asked.

No decision record: DR-7 already holds the rule this letter obeys, and LR-11 and LR-13 already
hold the schedule it states.

## Status

`completed`

## Result

All four criteria are met.

The finding that opened the session is worth recording on its own: the fifth of the five answers
the SUT specification asked for was written on 2026-08-31, marked sent in the desk registry, and
was never on the other desk. The desk is mirrored by hand and the copy was not made. The registry
row now records the delivery date separately from the date of the letter, so that the two cannot
be confused again.

The supplement publishes what the calibration probe established and none of its captures. The
ladder answers the ceiling of twenty as a measurement — no knee below eighty virtual users, five
of six bands faster under eight times the load than at the baseline, the worst wait of the top
step 181 ms against a ten-second ceiling — and answers the specification's own open point about
whether two cores and 4 GiB carry the intended profile. It states why no maximum follows from it:
the profile is too thin for any intensity to load the stand, which is what LR-13 is for.

The one band that moves with the volume rather than with the load is stated as the measured form
of the confound DR-7 named, and sharpens the seeded-volume question rather than repeating it.

## Decisions

The probe's figures are published while its captures are not. DR-7 spends the probe rather than
publishing it, and that rule is about evidence in a dataset, not about knowledge: withholding a
measured answer to a question the other project asked, on the ground that its capture is not
evidence, would be an odd reading of a rule meant to keep captures honest. The letter says which
of the two it is offering.

The baseline letter goes unedited, including the old name of the correspondent in its header. The
rename notice of 2026-09-03 asks for exactly that: existing documents keep the name they were
written under.

## Open questions

None new. The seeded volume and the core map stay as asked on 2026-08-31.

## Next steps

1. LR-13, which the supplement names as the reason the search cannot climb yet.
2. LR-11 after it, with the derived criterion's reference point re-chosen — the baseline is known
   to be the cold end of the curve.

## References

- Branch: `docs/ladder-supplement-to-the-baseline`
- Sent: the supplement of 2026-09-03 and, late, the baseline of 2026-08-31
- Figures read from the four run reports of 2026-08-31 and consolidated in
  [the calibration probe's note](2026-08-31-implementation-2_handoff.md)
