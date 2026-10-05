# Session note

## Type

`documentation`

## Goal

Make the repository ready to be read by a stranger before it is published: answer the JobSearch
project's request of 2026-09-22 proposal by proposal against the code, apply what is accepted, and
run the checks that request sets before publication.

## Completion criteria

1. Each of the proposals P1 to P7, and the second request for an excerpt of the capture artifacts,
   carries a verdict - accept, accept changed or reject - with one reason, and every statement the
   README gains is true of the code at the head of `development` rather than of a roadmap row.
2. `README.md` carries what was accepted and nothing else, and the licence is at the repository
   root.
3. The measured statements in `docs/` are read one by one and each is confirmed, or named, as an
   observation of a stated run rather than a claim about what the system carries.
4. The checks before publication that can be run from a command line are run and reported one by
   one: the build on a clean clone, the profile validation, the tracked files, the whole history
   searched for credentials, and the README's diff.
5. The answer is on the desk, naming the commit and the verdict per item.

## Scope boundaries

No code, no profile, no decision record and no roadmap row is changed, and no stand window is
spent: the fragment the README gains is cut from a run already on disk. The checks that need the
forge - the rendered first screen, the links as the forge resolves them, the repository's
description and topics - are not this session's; they are made when the repository is public.

## Status

`completed`

## Result

Every proposal is accepted, four of them changed, and none is rejected.

**P1, the opening, accepted changed.** The idea is the project's own and was stated nowhere in the
README. Five statements of the proposed text followed the roadmap rather than the code and now
follow the code:

- The accounts a run signs in with are not the pool. The pool is the explicit configuration and
  what is provisioned; a run signs in with the first accounts of each role, as many as its seats
  multiplied by the campaign's depth (DR-8). The bullet says both.
- Sessions are picked by state, as proposed, and they also end at a share the profile states, with
  the account that signs in next chosen by what the registries hold about it. The bullet says so.
- The task registry mirrors the rows of the transition table the scenarios drive, not the whole
  table: two rows of the closed table are deliberately absent, and the published system under test
  lets a reader count them.
- The session registry is not tested against the transition table; the task registry is. Both
  need no stand: no test opens a connection, and the one test that names an address assembles a
  plan without running it.
- The thesis is attributed to the system under test alone, which a reader can open and whose
  calibration rule states it, and not to a project that is not published.

The warm start does create only the difference at this head: the levelling in both directions that
DR-8 decides is the part of LR-13 not yet built. What comes next is named as the roadmap names it,
the calibration work, with no item number.

**P2, the heading, P3, the link to the system under test, P4, the author, accepted as proposed.**

**P5, the licence, accepted changed.** The file is the one the system under test carries. The
document map is closed, so it gains the row.

**P6, the password, accepted changed.** The property is `loadrig.provisioning.password`. The same
search that finds its default finds the three passwords a fresh database of the system under test
seeds, which that project publishes; the paragraph names both kinds.

**P7, the figures in `docs/`, accepted with no change.** Every measured statement is tied to a run
it names: the first baseline at ten virtual users, the calibration probe, or a verification run of
a scratch profile. None states what the system carries; where a maximum is mentioned it is to say
that none follows. Four sentences are the ones a reader could lift out of their context, and each
is protected by what stands beside it: the brief's two statements of the baseline's margin, which
conclude only that twenty virtual users is not a maximum; DR-7's expectation that the maximum
lies an order further, which is stated as an expectation in the cost of a rejected alternative and
was never measured; and the open question of 2026-09-04 about the connection pool reaching its
ceiling, which is a reading from the run DR-9 records as disqualified.

**The excerpt of the capture artifacts, accepted changed.** The README gains a section of its own
with a fragment of a description and of the report written beside it, captioned as an uncalibrated
run whose numbers are not a baseline. The service levels are cut from both fragments, so the
README still publishes no response time. The run is neither a capture of the calibration probe,
which DR-7 spends rather than publishes, nor the run of 2026-09-03 that the SUT project asked to
be held out of every dataset. The description it wrote is the one this head computes for the same
profile: the census and the rates were recomputed by the profile validation and agree.

## Decisions

- **The README follows the code where the roadmap row and the code differ.** A roadmap row states
  what an item delivers; the opening of a published README is checked by a stranger against the
  source.
- **The excerpt is verbatim, its unrounded rates included.** Two of the target rates are written
  with the noise of binary fractions. A fragment tidied by hand would no longer be a fragment of
  the artifact.
- **Session notes are not corrected.** The note of the calibration probe says the generator held
  its sessions on one physical core; LR-14 states that nothing enforces the core map yet. The
  roadmap row is the current statement and the note stays the record of what was believed that
  evening.

## Open questions

- Whether the description should round the rates it publishes. It is a change to code and to what
  a capture's consumer reads, so it is not made here.
- The brief's open point on the generator's cores says that at ten virtual users they are nowhere
  near a limit, while LR-14 says the core map is not enforced on the generator yet. The statement
  is true of an unpinned generator only; whether the brief should say so waits for LR-14, which
  settles it by measurement.

## Next steps

1. The checks that need the forge, once the repository is public: the rendered first screen, every
   link of it, the description and the topics.
2. The answer carried to the JobSearch desk by hand, and the publication date confirmed there.
3. LR-13's remaining part, then LR-14 to LR-17 - the calibration work the README's last opening
   sentence points at.

## References

- Branch: `docs/readme-idea-first`
- Changed: `README.md`, `LICENSE`, `docs/rules/documentation-rules.md`
- Answered: the JobSearch project's request of 2026-09-22, on the desk
- Read against: [DR-6](../decisions/dr-6-the-run-establishes-its-own-population.md),
  [DR-7](../decisions/dr-7-the-search-has-its-own-criterion.md),
  [DR-8](../decisions/dr-8-the-population-a-campaign-plays.md),
  [DR-9](../decisions/dr-9-the-rig-reads-its-own-run.md)
