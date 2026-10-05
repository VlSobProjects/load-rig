# DR 5 — A rig for this SUT, not a universal one: the adaptation ledger

**Status:** Draft

## Context

Every structural choice so far — the load model as typed code, the profile as numbers over a
code-owned vocabulary (DR-2), the scenarios as classes on a step kit (DR-3), the transition
table mirrored as the rig's own fact — fixes the SUT's current business process in this
repository's type system. That is a real coupling, and the question it raises has to be
answered on the record rather than case by case: is this rig an answer to the SUT as it stands,
or an instrument meant to survive the SUT changing under it?

The question is not hypothetical. Proven operator practice on corporate-scale rigs answers it
the other way: the profile is a list of load groups, each with its own intensity and a unique
key, defaults per group in a separate file overridden by the profile, scenarios as reusable
fragments, validation reporting per group. Under a living release cycle — the system under test
reshaped every sprint, the business model itself moving — that shape is the cheaper one, because
absorbing routine change in data spares rewriting a rig in code, and the invariants a
data-driven profile cannot hold structurally are held by the operator who reads the results.

This project's context differs in three load-bearing ways:

- **The SUT is frozen by specification.** It is a demonstration stand; changes arrive as
  documents through the exchange desk and become code deliberately. There is no release stream
  eroding the model month by month, and no development cycle of the SUT to keep pace with.
- **The captures are read by a machine.** An ordinary load-test report is filtered by an
  engineer; this rig's artifacts are datasets for an automatic triage. A false finding is not
  noise in a report but a poisoned dataset, so the profile discipline — the equilibrium, the
  think times, the skew — must be structural, not procedural. Those constraints are the SUT
  specification's own anti-requirements: a universal rig pointed at this task would have to
  encode them too.
- **The first-order task is to run the tests and hand over the data.** Universality that does
  not bring the first capture closer is speculative construction, and DR-3 already names the
  rig's version of that failure mode.

## Alternatives

**1. A universal data-driven rig.** Load groups with independent intensities in the profile,
defaults with overrides, scenario fragments reused across groups, per-group validation. Wins
under SUT churn: a new activity or a new weighting is a data change, no rebuild, no code
review. The costs, named honestly: independent group intensities are an open workload model,
while this specification states a closed one — a population with think times and an
equilibrium — and three of its four false-finding profiles are exactly what an open model makes
easy; the invariants tying groups together (creations balanced by settlements) live in the
operator's arithmetic or in a validator that must reconstruct the business relations from group
naming conventions; defaults-plus-overrides is the silent-default failure DR-2 refuses by
construction; and a session-selection filter in the profile is behaviour in data, the drift
DR-2's behaviour boundary exists to stop.

**2. A rig specific to this SUT, typed, with the coupling quarantined.** The business model is
code and the compiler holds it; the profile states numbers only; the SUT-facing layer is kept
deliberately thin and declarative — the surface as diffable constants, the correlation as an
enumerable dictionary, the scenarios as fixed-skeleton classes — so that change lands as a
bounded rewrite of a named layer. Wins here: the capture discipline is structural, and the
generation contract of DR-3 gets the types its gates need. Costs, named honestly: every change
of the mix's *structure* (a new weighted branch, a new scenario shape) is a code change with a
session behind it; a vocabulary change breaks old profile files loudly and they are updated in
lockstep — the price of refusing silent defaults; and under a hypothetical living SUT this
whole trade flips back toward alternative 1.

## Decision

Alternative 2, stated as what this rig is: **an answer to the current architecture and business
processes of the SUT, not a universal solution.** The choice is the context's, not a claim that
the data-driven shape is wrong — under a living release cycle it is the better bargain, and
this record says so.

The coupling is to behaviour, never to implementation: the rig mirrors the transition table's
behaviour under its own names, reads no database schema, and drives no operational endpoint. A
SUT reimplemented with identical behaviour does not touch this rig.

What the rig absorbs, and at what price — the adaptation ledger:

| Change in the SUT or the model | What is edited | Price |
| --- | --- | --- |
| Numbers: mix, skew, think times, volumes, populations | a profile file | none in code, no rebuild |
| A new profile variant over the same vocabulary | a new profile file | none in code |
| New weights or parameters of an existing scenario | a vocabulary key and a typed parameter | small: code and profile in lockstep |
| Addresses, form fields, page marks, the list's own columns and status words, same behaviour | the surface, the correlation dictionary and the reading of the list page; the conformance walk and the captured answers prove them | small, localized by construction |
| A new or reshaped business flow | a scenario class on the step kit, a name in the closed registry | a session |
| A changed transition table or status set | the mirrored table and its tests | a session |
| A changed transport model — an API instead of rendered pages, tokens instead of form sessions | the transport layer whole; DR-1's answer-shape rule and the scraped-token mechanics fall | **the boundary: past it the rig is rewritten, not adapted** |
| A different SUT altogether | surface, correlation, scenarios and profiles anew; the vocabulary machinery, the step kit, the registry patterns and the run harness survive | a new rig on the same rails |

Below the boundary, adaptation is a deliberate rewrite of the named thin layer, priced at a
session or less. At and past the boundary this project does not pretend to adapt: serious
change in the SUT means rewriting the coupled logic, and this record makes that the honest,
expected answer rather than a failure of the design.

## Consequences

- No speculative universality is built: no group scheduler, no override files, no filter
  expressions in profiles. A request for one of these is a request to supersede this record.
- DR-3 is read in this light: it does not promise a rig that survives SUT change automatically;
  it keeps the SUT-coupled layer thin and diffable, which is what makes today's manual rewrite
  cheap and a future regeneration pipeline possible at all. The two records point the same way.
- The exchange desk is the change protocol: a SUT change reaches this rig as a document, its
  impact is priced against the ledger above, and the work is a session on a branch — never a
  quiet accommodation inside unrelated work.
- A reader of a capture may take the rig's model as the SUT's current business process, because
  keeping the two identical is this record's standing obligation and the conformance walk's
  subject.
