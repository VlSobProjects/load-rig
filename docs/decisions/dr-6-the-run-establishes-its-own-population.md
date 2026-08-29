# DR 6 — The run establishes its own population: a computed census and a warm start

**Status:** Draft

## Context

The first measured runs realized the transition mix four times under its target. The cause is not
the transport and not the mix: the task registry starts a run knowing only the run's own
creations, and the stand it drove held no history at all — the seeded volume is the SUT project's
own phase-2 item and had not run. The population the profile needs was absent, not merely
invisible to the registry.

The same runs exposed a second thing. The profile states a seeded volume of fifty thousand rows.
Nothing measures it, the equilibrium check divides the window's drift by it, and every capture
publishes it. It is a statement about a stand, made by a file that cannot know one.

The arithmetic decides what has to be established. The scenarios' step weights are code, so the
per-transition rates are exact; at the day profile — ten users, a mean think time of five seconds,
twelve steps a minute each — they are:

| bucket | fed per minute | drawn per minute | balance |
| --- | --- | --- | --- |
| open | creations 6, hand-outs 0.6, reopens 0.6 | finishes 6, returns 1.2, refusals 1.2 | −1.2 |
| finished work | 6 | approvals 4.8 | +1.2 |
| returned questions | 1.2 | hand-outs 0.6 | +0.6 |
| refusals | 1.2 | acknowledgements 1.2 | 0 |
| settled work | 6 | reopens 0.6 | +5.4 |

Two facts follow. The balance the specification calls equilibrium is a property of the working
set — the tasks a step may actually act on — and not of the table: the table grows by creations
minus deletions, 4.8 rows a minute, whatever the buckets do. And the bucket that drains is the one
every worker draws from, which is exactly where the realized mix collapsed.

A third fact appeared only when a run was measured on a population that was otherwise sound. The
account pool is sized to the ceiling of the load model — twenty virtual users — while a day of ten
occupies five workers of the sixteen. The manager's creation drew its assignee from the whole pool,
so eleven of every sixteen created tasks went to people the run never signs in as: work created and
never touched. Against a draw of 1.68 open tasks a minute, an occupied worker was fed about 0.53,
and the run realized its transitions a third under target with every missing move recorded as a
starved gate. No census can cure that; it can only pay for the leak with a deeper stock, and the
leak grows with every account the pool gains.

The pressures, by role:

- A capture must be trustworthy from its first minute. A window whose opening minutes starve
  holds two regimes, and the triage cannot tell the second one from a system that degraded.
- The rig must not invent facts about the stand. Whatever it publishes about the history under
  the load has to be measured or computed, never assumed.
- The SUT project's seeding item will land and take the whole initial state. Whatever is built
  now must cost no rewrite then.
- The profile scales: the stepped calibration run drives the same shape at twenty users, and what
  is computed for ten must hold there without being restated.

## Alternatives

**1. A pre-roll before the measured window.** Drive the load for some minutes, then start
counting. Cost: the starvation is not removed but hidden — the pre-roll drives a mix the profile
does not state, and the population it builds is whatever the starved mix happened to produce. The
result log stops being the run: it holds samples nobody may read, and the moment the window opens
becomes a fact every reader must be told out of band. The rig would be publishing a capture that
cannot be read on its own.

**2. A manifest of seeded task identities from the SUT project.** The registry would be filled
from what the seeding wrote. Cost: it was asked through the exchange desk and answered — no user
of the application holds such a manifest, and the seeding cannot pre-register hot tasks. Building
on it would be building on a fact the other project has declined to produce.

**3. A stock stated by the profile.** The profile names how many tasks to create before the
window. Cost: it is the seeded volume's disease under a new name — a number nobody can derive,
nothing can check, and which silently stops fitting the moment a population, a weight or the think
time changes. It also cannot express what actually starves: a total says nothing about the worker
who has no open task assigned to them.

**4. A computed census, brought about by a warm start.** The census is computed per bucket from
the scenarios' coded weights and the profile's populations; a warm start reads what the stand
already holds from the list page ordered by due date, registers it as the run's own facts, and
creates and drives only the difference. Cost: the rig grows a second administration path beside
the pool provisioning, and the reading of the list page is one more rule held against the
application's markup.

Where the warm start runs is a choice of its own. **A set-up thread group inside the plan** is the
smaller change, but its requests land in the result log through the plan's writer: a capture would
open with a burst of creations at a rate no profile states, and every reader computing achieved
intensity would meet it first. **A plain-Java step beside the pool provisioning** keeps the result
log the window's alone, at the cost of a second transport that is not the load model's.

## Decision

Alternative 4, with the warm start as a plain-Java step, under four rules.

**The census is per bucket, never a total.** For every step that draws a task from the registry,
the census states the stock its bucket needs for the acting account: an open task assigned to that
worker, finished work created by that manager, a returned question, a refusal, settled work. Each
stock covers the drain the window causes where the flows do not balance, plus the slack the
randomness of the draw asks for — draws and feeds arrive at random moments, so a stock that covers
only the average deficit empties now and then anyway, and the slack grows as the square root of
what the window draws, the way the spread of a count of random arrivals does. The census is
computed from the same weights the plan is built from, so it cannot describe a run different from
the one that executes.

**The warm start is stand administration, not load.** It runs on the JDK's HTTP client beside the
pool provisioning, before the plan is executed; it writes no samples, and the result log holds the
window and nothing else. It reads the stand through the only inventory the application offers —
the list page ordered by due date, which is also where the hot set is — and it creates through the
same screens a person uses.

**The stand's volume is measured, never stated.** The list page reports how many tasks it is
paging over; the warm start reads that number, and the load-profile description publishes it
beside the census the run established. `seededVolume` leaves the profile vocabulary: a profile
states what the run applies, not what the stand contains.

**A run's work circulates among the accounts the run occupies.** Which accounts a profile's
populations seat is one fact with three readers — the seating check, the census, and every step
that names a person: the assignee of a creation, the worker a hand-out goes to, the subject of a
report. The alternative was to let the census pay for the leak with a deeper stock, which keeps in
the model a flow that leads nowhere: work created for people who are not in the run, a stand that
accumulates it, and an equilibrium that holds only because the window was paid for in advance.
Handing work to the people who are working is also the truer reading of the specification's ten
virtual users.

**The invariant is re-based on the working set.** What refuses a run is a census the warm start
cannot bring about — beyond a stated ceiling it is a seeding job, not a warm start, and the run
says so before the window instead of spending one. The table's growth over the window stays a
published fact of the capture, judged by its reader against the measured volume, because a growth
that is noise on a seeded stand is the whole table on an empty one, and neither is a reason to
refuse a run.

## Consequences

- The profile vocabulary loses a key, and every capture stops publishing a number nobody measured.
  Profiles in hand must drop it; the strict loader refuses it as unknown, so no file drifts past
  silently.
- The equilibrium check dissolves into two honest pieces: the demand the coded scenarios imply,
  computed exactly from their weights, and the census that demand requires. The specification's
  weighting of about two and a half transitions per settlement stops being a constant in the code —
  it was an approximation of numbers the weights state exactly.
- The rig reads the application's list markup, and a status it cannot name refuses the warm start
  loudly. This is one more place the rig is coupled to this SUT, and it belongs in the adaptation
  ledger of DR-5.
- On an empty stand the warm start creates the whole census; on a seeded one it reads and creates
  nothing. When the SUT project's seeding item lands, the warm start needs no rewrite — its
  reading path becomes the only one that runs, exactly as the provisioning walk becomes a no-op.
- A run costs the stand a warm start before every window. It is bounded by the census ceiling and
  it is idempotent in effect, not in the rows it writes: a second run against the same stand reads
  what the first one left and creates less.
- The rig now acts on tasks it did not create, including tasks left by earlier runs. That is what
  a warm start is for, and it is also what makes the concurrent-delete tolerance and the
  visibility rules load-bearing rather than theoretical.
- A pool larger than the profile's populations stops distorting the run: the accounts beyond the
  seats are provisioned and idle, exactly as a pool sized to the campaign's ceiling should be. The
  price is that a capture's load concentrates on as many people as the profile states, which is
  what stating them was for.
