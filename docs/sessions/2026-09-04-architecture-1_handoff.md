# Session note

## Type

`architecture`

## Goal

Settle, before any of it is built, why this project reads the system side of its own run at all,
what that reading may and may not touch, and what the calibration stage therefore has to build.

## Completion criteria

1. A decision record states the purpose of the rig's own analysis, at least two rejected
   alternatives with their costs, and the rule that keeps it independent of the automatic path.
2. The paragraph of `docs/brief.md` that gives the capture extraction away entirely is corrected,
   because it no longer describes the project.
3. The work the decision opens is registered as roadmap items in the order it must be done, and
   every item that now depends on them says so.

## Scope boundaries

No code and no stand window. No answer to the SUT project's letter of 2026-09-03 — it is written
in the next session, on a boundary this one has already fixed.

The conditions of a valid run — the generator's packaging, its cores, a closed host — are roadmap
work under this record and not decisions of their own; the reasoning for a declared `cpuset` over
a hand-set affinity mask is carried by the item that does it.

## Status

`completed`

## Result

All three criteria are met. DR-9 is drafted, the brief's boundary paragraph is rewritten, and
LR-14 to LR-17 are registered between LR-13 and LR-11, with LR-11's own dependency corrected to
name them.

The record's substance is that the rig's analysis exists for two reasons of unequal weight. The
practical one is that a rig whose runs are declared admissible by the owners of the system under
test is not finished, and that has now happened twice by correspondence. The deciding one is that
the workflow-engine's automatic verdict has to be debugged against a protocol made here by hand,
and two readings of one run are evidence only while they are independent.

That second reason is what shapes the decision rather than merely motivating it. It rejects the
cheapest alternative — taking the readings from the triage that already fetches them — not on
practical grounds but because it would make the reference and the subject the same instrument. And
it is why the protocol lives beside the capture and never inside it: the capture is what the
automatic path consumes, so the independence rule is made structural instead of remembered.

## Decisions

DR-9, in seven parts: a closed list of what is read; readings taken over the measured window and
not over the run; every figure stated against the limit it is read against; the application's log
read here too, so the verdict is whole; the protocol beside the capture and never inside it; the
protocol distinct from the run report, which stays the injector's own statement inside the capture;
and the dashboard's `uid` and panel numbers held as configuration and as a contract.

The narrowest of those rules exists because of this project's own error: on 2026-09-03 a processor
figure was read against an assumed quota of two cores where the stack states four, and a wrong
conclusion was published from it inside the day.

## Open questions

Whether the connection pool reaching its ceiling on its own at the intensity of 2026-09-03
collides with the injector-side variant that induces a saturated pool deliberately. The SUT project
raised it in its letter of 2026-09-03; it is a question about the campaign's design, it is not
settled here, and it belongs with LR-11 rather than with this record.

## Next steps

1. The answer to the SUT project's letter of 2026-09-03: its baseline window confirmed against this
   project's own clock, the run of 2026-09-03 held out of every dataset, the panel contract
   confirmed as this project's to hold, and the intensity of the next climb answered as a date
   rather than a number — after the conditions of that letter's section 5 are built.
2. LR-13, then LR-14 to LR-17 in that order.

## References

- Branch: `docs/dr_9-the-rigs-own-protocol`
- Decision record: [DR 9](../decisions/dr-9-the-rig-reads-its-own-run.md)
- Changed: `docs/brief.md`, `docs/roadmap.md`, the decision registry
- Received: the SUT project's answer of 2026-09-03, which supplies most of this record's context
