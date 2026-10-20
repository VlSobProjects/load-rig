# DR 10 — The SUT owns the initial state: a named stand profile applied on request

**Status:** Accepted. The SUT project took the proposal in EX-31 (2026-10-19) as its item `RM-3`;
the rules below state what that answer settled.

## Context

A second rig, load-rig-locust, reproduces this project's day profile on the same stand, and the two
rigs exist to test the same thing and compare their results (EX-25, 2026-10-19). Such a comparison
means something only if both rigs open their windows on the same initial state: the same
accounts in the same roles, the same volume, and the same tasks in the same buckets.

Today that state has no single owner, and the one this project is building is not shareable:

- The accounts are provisioned by this rig's own walk through the administrator screens: eighty
  of them, sized to the top step of the search.
- The tasks are brought about by this rig's warm start, which computes a census per bucket from
  this rig's own weights and intensities and creates the difference (DR-6). DR-8 extends it to
  level the stand in both directions to a state the campaign states; that part is not built yet.
- The census is therefore a function of this rig's model, recomputed for every run and scaled
  with every step of a search. A stand prepared by it is not a state another rig can rely on:
  whatever the other rig leaves, this rig's next warm start levels away.

The brief already says the initial state belongs to the SUT project's seeding item once that item
lands, because two owners of the initial state are two states nobody reproduces. What a second rig
adds is that the brief's rule now has two readers, and the seeding has to be something that can be
asked for again before every run, not a database seeded once.

## Alternatives

**1. The SUT project owns the pool and applies named stand profiles on request.** The SUT project
holds the accounts and a script that sets the stand to a named profile: accounts by role, the
volume, the tasks by status and assignee, the fall of due dates. Any rig asks for a profile before
a run, and asking again gives the same stand. Cost: this project waits on another project's work
item, which has not run; the profile has to cover the demand of every rig that plays on it; the
time a reset takes becomes part of a campaign's cost and is not known yet.

**2. A shared campaign file, and each rig levels the stand itself.** Both rigs read one stated
initial state and each brings it about with its own warm start. Nothing waits on the SUT project.
Cost: two implementations of one levelling, and the comparison then rests on their agreeing, which
nothing checks. It is the brief's two owners of the initial state under another name.

**3. This rig's warm start stays the leveller, and the other rig plays on what it leaves.** Nothing
changes here. Cost: the other rig depends on this rig having run just before it, the census is not
a state but a per-run figure, and this project would carry a coordination duty for a comparison
that is the other project's goal.

## Decision

Alternative 1, under five rules.

**The initial state is a named stand profile, owned and applied by the SUT project.** A profile
names the accounts and their roles, the volume, the tasks by status with the creator and the
assignee of each, and the fall of due dates and creation times, all measured from the moment it is
applied. Applying it is a step of the stand's pipeline, not an act of a rig, and it runs on its own
against a stand that is up: the application stops, the database is filled to the profile, and
both are started again. Applying the same profile twice gives the same stand, identities included;
a changed profile takes a new name.

**A profile's content is agreed through the desk, from the demand each rig states.** This project
states what its model needs at the top step of a campaign: the accounts of the rotation by role,
and the census per bucket and per account. The pool's composition stays this project's statement
and the profile's input, as the brief already has it. A rig's demand is a lower bound: a profile
covers the demand of every rig that plays on it, and the SUT project chooses the volume against the
database's buffer pool and reaches it with tasks outside every census. A change to a profile
reaches every such rig through the SUT project.

**Every step of a campaign opens on the profile.** DR-8's rule that the steps of one campaign differ
in intensity alone stands; what changes is who brings the state about. The campaign runner has the
stand set to the campaign's profile before each step instead of levelling the stand itself.
Applying a profile always restarts the database and the application, with the buffer pool neither
saved nor reloaded, and drops every session, so every step opens cold and signs in anew: the
settling interval before a measured window is sized for a cold application and a cold buffer
pool, and no rig drives the stand while a profile is applied.

**The warm start reads and checks, and no longer creates.** Once profiles exist the warm start reads
the stand from the list page as it does now, registers what it finds, and refuses the run when the
census is not met, instead of creating the difference. Until then it stays as it is: it creates
the missing part and removes nothing. DR-8's levelling in both directions is not built here.

**A capture names the profile its stand was set to.** The stand is the source of that fact: the
data generator records the name in the database, and the application reports it in its log and
in its scrape. The load-profile description carries the name the campaign asked for, beside the
volume the warm start measured, and the run's own readings check it against what the stand
reports, so that two captures of two rigs can be shown to have started from the same state rather
than assumed to.

This record amends two rules of earlier records: DR-6's warm start that creates the difference
becomes transitional, and DR-8's warm start that levels the stand in both directions is replaced by
the profile.

## Consequences

- The comparison between the two rigs becomes checkable: same profile name, same measured volume.
  The same profile is open to any other rig on the stand without anything further from this
  project.
- This project stops growing a second administration path. The provisioning walk and the creating
  half of the warm start retire when the profiles land; until then they stay, and must remain a
  no-op against a stand set to a profile.
- This project depends on the SUT project's seeding work for the calibration campaign. The stepped
  search cannot open on a stated state before the profiles exist, or else runs on the transitional
  warm start and says so in its captures.
- A reset before every step costs the stand time this project does not control. The campaign's
  duration now includes it; the SUT project measures it when the profile is built and publishes it
  with the profile.
- Every step opens on a cold application and a cold buffer pool by construction. A campaign pays a
  longer settling interval for it, and in exchange no step inherits the warmth of the one before.
- The warm start will read a stand holding more tasks than its census, because the volume is
  chosen beyond every rig's demand. Reading the list page over that volume is the warm start's
  cost, met when the profiles land.
- The census becomes a statement of demand sent to another project instead of a figure computed and
  used in one run. A change to the weights, the populations or the rotation's depth that raises it
  now means a changed profile, which goes through the desk before the campaign that needs it.
- Every number this record depends on — the volume, the composition, the reset time — belongs to the
  profile, and is written into this repository as configuration when the SUT project states it.
