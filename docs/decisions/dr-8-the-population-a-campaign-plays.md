# DR 8 — The population a campaign plays: sessions that end, players beyond the seats, a stand levelled to a stated state

**Status:** Draft

## Context

The calibration probe of the stepped search drove the day profile's shape at two, four and eight
times the baseline and found no knee: five bands of six answer faster under eighty virtual users
than under ten. The realized intensity held within 2.5 % of target at every step, so the generator
was not the limit either. What the probe found instead were four properties of the load model, and
they are the subject of this record.

**Nothing in the load model ends a session.** A virtual user takes up a session and keeps it: a
sign-in happens only when no signed-in session of the role is free, which after the ramp is never.
Twenty sign-in samples at ten users and one hundred and sixty at eighty are two requests per user
and no more. So the specification's day profile, whose sign-ins are spread through the window, is
not realized; the band's figure is a cold-start figure measured on the ramp; and the population the
stack sees is a fixed set of people who never arrive and never leave.

**The set of people is therefore as wide as a step's seats and no wider.** Every list, every task
opened, every report subject is drawn from the accounts seated at that moment. A working set that
narrow sits in whatever cache holds it, and the report — the wide read the fault surface rests on —
asks about one of a handful of workers.

**The stand is whatever the previous run left.** The probe's steps started at 591, 661, 778 and
1063 tasks, because each window adds creations beyond deletions and the warm start only ever
creates the difference. The one band that moved across the ladder moved for that reason: the report
went from 36 ms to 61 ms as the table grew, not as the intensity rose.

**The mix drifts the table upward by construction.** Creations are five percent of the mix and
deletions one, so a window adds roughly five rows a minute per ten users. Nothing in the model
removes them, and nothing states what the stand should hold.

Two facts bound what any of this can buy, and they are stated here so that a later reader does not
credit this record with more than it delivers. The report **scans every task in memory today** — the
SUT project's item that reduces it to an indexed query has not run — so widening the set of report
subjects buys the application's processor and memory, not the database's disk. And whether a wider
working set reaches the disk at all is decided by the database's buffer pool, whose size this
project does not know; it is asked through the desk with the seeded volume.

## Alternatives

### How the population stops being synthetic

**1. Keep the seating and widen only the pool.** The accounts exist, more of them hold history, and
nothing in the model changes. Cost: measured and rejected once already. A pool wider than the seats
sends a manager's creations to people nobody signs in as, the transitions starve for want of open
tasks and the table grows with work never touched — the finding that produced DR-6's circulation
rule. Reports on such an account scan an empty history, which is cheap and representative of
nothing.

**2. One account per virtual user, held for the run.** The simplest model, and the one the session
registry was deliberately built not to be: sign-ins become a per-iteration fixture rather than a
share of the mix, the discussion loses its several distinct people on one task, and the population
still never turns over. It trades the registry's whole reason for existing against nothing this
record needs.

**3. Sessions that end, and an account chosen by state to replace them.** A session is given up at a
stated rate, the account that signs in next is chosen by what the rig knows about it, and the set of
players is deliberately wider than the seats a step holds. Cost: the census must stock every account
in the rotation rather than the seated few, which multiplies it by the rotation's depth and makes
the warm start longer; and the choice rule is one more thing the rig maintains. Chosen.

### What the stand holds when a window opens

**1. Whatever the previous run left, published with the capture.** Today's rule. It is honest — the
volume is measured and travels in the description — but the probe showed it is not enough: a ladder
whose steps run against 591 and then 1063 tasks has varied two things at once, and the band that
moved is the one the fault surface rests on.

**2. Levelled up only, as the warm start does now.** It brings the census about but removes nothing
a window added, so the drift accumulates across a campaign exactly as it did across the probe.

**3. Levelled in both directions to a stated initial state.** The campaign states how many accounts
play and how many tasks the stand holds; the warm start creates what is missing and removes what is
in excess, so every step of a campaign opens on the same stand. Cost: a long first warm start when
the stated volume is far above what the stand holds, and deletion as an administrative act beside
the creating one. Chosen.

### How much the window itself deletes

**1. Deletion compensating creation exactly.** The table would hold its size within the window as
well as across steps. Cost: it raises deletion from one percent of the mix to five, and the
specification is explicit that deletion is rare on purpose — this application has no archive, so
deletion is the only way out and a real one is rare. A profile that deletes as often as it creates
describes a business nobody runs.

**2. Deletion left at one percent, with the levelling doing everything.** The stand stays comparable
and the mix stays as specified, but the window then barely exercises a delete meeting a concurrent
read: the act whose refusal the rig already tolerates, and the one place the model produces
cascading writes.

**3. A stated share above today's and below compensation, with the pick unbiased.** Chosen. The
share is a number the campaign states and the desk is told about, and the task deleted is drawn as
it is drawn now — not steered toward the hot set.

## Decision

Six rules. Together they are what LR-13 implements.

**A session ends and another begins.** Giving up a session is an act of the model with a stated
share, so sign-ins recur through the window and the band that measures them measures a steady state
rather than a ramp. The share is small: this is a working day, not a shift change.

**Which account signs in is decided by state, from the rig's own registry.** The registry already
holds what an account has open, what it created and what it has acted on, so the choice rule is
written over that — the depth of an account's backlog, how many acts it has taken in the run, how
long since its last one. Reading the same fact out of a report's rendered answer was considered and
refused: it would measure the SUT to learn what the rig already knows, and couple a choice rule to
a page's markup.

**The players are wider than the seats, and the depth is a campaign parameter.** A step seats as
many sessions as its populations state; the campaign states how many accounts those seats rotate
through. The ratio is the instrument: it decides how wide the working set is, and therefore how
little of it a cache can hold.

**DR-6's circulation rule is amended, not dropped.** It said the run's work circulates among the
accounts the run occupies, against the measured failure of handing work to accounts nobody signs in
as. It now reads: the work circulates among the accounts the run **plays over its window**, and an
account between sessions is waiting its turn rather than absent. The census follows the same
change — it stocks every account of the rotation, which is what makes the working set exceed the
seats.

**The initial state is a fact the campaign states, and the warm start levels to it in both
directions.** How many accounts play and how many tasks the stand holds are stated before the
campaign and hold for every step of it; the warm start creates what is missing and removes what is
in excess, as stand administration outside the result log. The stated volume is chosen deliberately
and not inherited from what a stand happens to hold: a volume small enough to sit in any cache
makes the report the cheapest act in the profile, which is the opposite of what the fault surface
needs. When the SUT project's seeding item chooses a volume, that figure becomes this one and the
warm start is not rewritten.

**Deletion runs at a stated share, unbiased.** Above the specification's one percent, below
compensation, stated in the profile and sent through the desk as this project's deviation. The task
deleted is drawn among those fitting, never steered toward the hot set: a profile that deletes what
several sessions are writing notes on manufactures contention no population produces, and
manufactured contention is the false finding this project is required not to produce. Whatever
contention appears when the act runs at an honest rate is the system's property and is worth
capturing.

## Consequences

- The census grows with the rotation's depth, and with it the first warm start of a campaign. The
  cost is paid once per campaign and corrected in minutes afterwards, because the levelling only
  moves the difference.
- The sign-in band becomes a steady-state figure and stops being the cold-start artefact the probe
  found. Every figure measured before this lands — the first baseline included — is a figure of a
  model in which nobody signs in twice, and the campaign that judges a search must be re-measured
  on the model that replaces it.
- DR-7's derived criterion loses its reference point until then. Its rule stands — one multiple of
  what the same stand answered when it was quiet — but the quiet reference cannot be the coldest
  run of a campaign, and which run it is is settled when the density work lands.
- More accounts play, so the history the seeding item can give each one is thinner. That is the
  trade the desk is told about together with the composition, and it is the reason the player count
  is a campaign's stated number rather than a constant that grows whenever a step needs seats.
- The tolerated concurrent delete stops being a rarity. Its noise floor rises with the deletion
  share, and the ledger that counts it earns its place: losses beyond what the profile intends
  still end a run, and the share is what "intends" now means.
- Widening the players does not, by itself, take the database to its disk. Today it takes the
  application's processor, because the report scans the table in memory; the disk story waits on
  the SUT project's indexed report and on a buffer pool this project has asked about but does not
  set.
