# DR 7 — The maximum is searched for against the baseline, under a criterion of its own

**Status:** Draft

## Context

The first measured baseline of the day profile left every band of the service levels met by one to
two orders of magnitude. Per band, at the 95th percentile against the figure it was held to:

| band | measured | held to | headroom |
| --- | --- | --- | --- |
| signing in | 144 ms | 2000 ms | 14x |
| performing an action | 58 ms | 1000 ms | 17x |
| writing a note | 43 ms | 1000 ms | 23x |
| opening a list | 39 ms | 1000 ms | 26x |
| opening one task | 38 ms | 1000 ms | 26x |
| running a report | 43 ms | 3000 ms | 70x |

The stepped search of LR-11 is defined by the specification as the same profile shape with the
intensity raised proportionally, each step judged by the service levels, the first step that misses
them ending the search and the step before it standing as the maximum. Against this baseline that
definition does not survive contact with the stand, for three reasons.

**The order of the bands is a property of the stand's data, not of the application.** The loosest
band is the report, which is the wide read the entire fault surface rests on, and it is loosest
because the window ran against a stand holding under six hundred tasks — a table the report scans
in memory at no cost. The tightest is signing in, whose cost is password hashing and is nearly pure
processor. A search scaled by intensity alone would therefore be decided first by the band that has
least to do with the business the profile drives, on a stand whose volume the SUT project's seeding
item has not yet chosen.

**The criterion, at these figures, does not measure a degradation — it measures a cliff.** Below
saturation the waits stay in the tens of milliseconds; past it they cross the ten-second ceiling
almost at once. A step ladder judged by figures that far away terminates on the far side of the
knee, which makes the terminating capture an overload run — the one thing the item exists not to
produce, because a ceiling probe that is really an overload run answers a different question than
the one asked.

**Two of the three ways a real saturation would announce itself are already spoken for.** Under
genuine load this stack's first structural failure is as likely to be the connection pool, whose
signature — recognisable timeouts in the service log — is exactly the induced fault of another
variant; a maximum found that way is indistinguishable from a planted defect, which is the worst
outcome available to a triage dataset. And the generator holds one physical core against the
application's two under the host's core map, so the search may instead terminate on the injector,
which is the starved-injector variant produced by accident.

The specification names four levers. The scenarios cannot move: the method requires one shape, and
a run that changes the profile as it grows has measured two systems and can declare neither a
maximum. The intensity is not a lever here but the method itself. What remains is the resources,
the figures, and a fourth thing the lever table folds into the first — the volume of data the stand
holds, which is why the report's headroom reads as seventy, and which belongs to the SUT project's
seeding item rather than to this one.

## Alternatives

**1. Narrow what the stack is given, and keep the specification's figures.** The comfort figures
keep their human meaning, a breach stays a breach a person would recognise, and the maximum found
is the maximum of a smaller machine — an honest measurement.

Two costs, one of them decisive. The reduction must be made by narrowing the processor set, never
by lowering the quota: a quota that binds shows up as a non-zero throttled-period counter, and the
calibration rule reads that as a capture whose every latency is an artefact of the limit rather
than a property of the system. So the only admissible form of this lever is a narrower core set,
which lives in the other project's compose file: a letter through the desk, its release, and every
capture of the campaign moving to a new core map, with the baseline re-measured because a capture
is read against a baseline taken under the same levers. And it buys about a factor of two against a
headroom of tens: the assumed ceiling of twenty virtual users would still be nowhere near a
maximum.

**2. Keep every lever where it is and climb until the comfort figures break.** This is the strongest
form of the answer the SUT project asked for, and it refutes or confirms the hypothesis of twenty
in the plainest way.

Cost: six or more stand windows, each preceded by a warm start whose census scales with the
intensity; a terminating step that is by construction an overload capture; and a search that is
more likely to be ended by the injector or by the connection pool than by the criterion — in which
case it has measured the generator or reproduced a planted fault, and in neither case a maximum.
The honest expected result is already known well enough to state without spending the windows:
twenty is not the maximum, and it is short of it by an order.

**3. Give the search its own criterion, derived from the baseline.** The figures a step is judged
by become a stated multiple of what the same stand answered when it was quiet, so that the search
terminates while the response curve is still graded — inside the regime where a capture is a load
test at all.

Cost: the maximum found is a lower bound of the one the comfort figures would give, and it is no
longer a statement about what a person would tolerate. It also ends the convenience of one set of
service levels for the whole project: a reader now has to know which campaign a capture belongs to.

## Decision

Alternative 3. The lever turned is the fourth — the figures — and it is turned in a derived form,
under five rules.

**The search is a campaign of its own, with a service-levels file of its own.** The levels are
fixed before its first step, are identical across every step, and travel into each step's
description as read, exactly as the campaign discipline requires. The comfort figures of the
specification stay in force, unedited, for the campaign that produces the baseline and the fault
variants. Two campaigns, two files, neither adjusted inside its own campaign — which is what the
discipline forbids, rather than the existence of a second campaign.

**The figures are derived, not chosen.** Each band's level is the same multiple of that band's
measured 95th percentile on the clean baseline of the same stand, rounded up to the nearest fifty
milliseconds. One multiple for every band, so the criterion reads as a single sentence — the system
is N times slower than it is when quiet — and no band is privileged by taste. The working
hypothesis is fivefold, which against the baseline above gives 200 ms for opening a list, 200 ms
for opening one task, 300 ms for an action, 250 ms for a note, 750 ms for signing in and 250 ms for
a report.

**The multiple is fixed by a calibration run, before the campaign.** One or two steps, at
intensities several times the baseline, whose only purpose is to show where the curve leaves its
flat part and which side gives first — the stack or the injector. A calibration run is not a
capture of the campaign: it is not published as evidence and it is not read against anything. It
exists so that the multiple is a measurement of where the graded regime ends rather than a number
somebody liked.

**The hard ceiling is not a criterion of the search.** It stays what the specification set it to
and keeps its own meaning: past it a run has stopped measuring a usable system. A search that ever
reaches it has already gone a step too far.

**Two guards decide whether a step counts, before its percentiles are read.** A step whose achieved
intensity fell short of its target measured the generator and not the stack — the starved injector
by accident — and is repeated rather than judged. And a band ends the search only if it was judged
on a stated minimum number of samples: the baseline judged signing in on twenty samples, where the
95th percentile by nearest rank is a single value, and a maximum must not turn on one.

The resources and the seeded volume are not turned here. Both are the other project's to turn, and
both leave this project as questions through the desk rather than as changes: whether the
application's core set may be narrowed for a later campaign, and what volume the report is meant to
scan — the second being the reason the report's headroom reads as seventy, and the first being an
open point the host agreement already carries.

## Consequences

- No code changes. The service levels are already selected per run by their own key, and the file
  is loaded as strictly as a profile, so the search campaign is a second file and a run key. The
  vocabulary needs nothing new: a derived figure is a figure.
- What LR-11 reports is where the day profile's shape degrades by the stated factor, not where the
  stack breaks, and the capture and the fifth answer to the SUT project say so in those words. The
  hypothesis of twenty virtual users is answered as what it is — not a maximum under any of these
  criteria — and the measured number is never quoted without the criterion it was found under.
- The maximum found is a lower bound of the one the comfort figures would give. That is the point:
  a bound found before the knee is found in a regime where each step is still a load test, and a
  lower bound taken deliberately is worth more than an upper bound taken by accident.
- The criterion is bound to the stand it was derived on. If the core map, the seeded volume or the
  SUT version changes, the multiple stays and the milliseconds are recomputed from a fresh
  baseline. A campaign is never re-judged by a file it did not run under, and a capture already
  carries the file it ran under.
- The search varies the intensity and, through the census, the population the stand holds: a later
  step runs against a larger table than the one before it. The shape is held, the volume is not,
  and the maximum is therefore a maximum at the volume it was found at. Each step already publishes
  the volume the stand was measured to hold, so the confound is visible in the capture instead of
  hidden in it; removing it belongs to the seeding item, which fixes the volume for everyone.
- The project now has more than one set of service levels, and which campaign a capture belongs to
  becomes a question a reader can ask. The description carries the figures it was judged by, so the
  answer is always in the artefact — but the campaign is part of what a capture means, and the
  documents that describe a capture say so.
