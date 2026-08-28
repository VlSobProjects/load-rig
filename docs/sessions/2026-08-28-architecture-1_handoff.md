# Session 2026-08-28-architecture-1

## Type
`architecture`

## Goal
Fix, as decision records, the two patterns every later piece of the rig must hold: how a load
profile is expressed, and what makes the rig a target the planned actualization pipeline can
generate against.

## Completion criteria
- DR-2 states where the boundary runs between the code and the profile, with the alternatives
  and the binding rules that keep the boundary from drifting.
- DR-3 states the rig's side of the actualization contract — the generation target, the pattern
  that constrains generated code and the gates that refuse it — with the alternatives.
- Both records are `Draft`, to be accepted by the work items that prove them.

## Scope boundaries
No code. This session implements neither the profile loader nor the step kit nor any pipeline.
The actualization scenario itself belongs to the workflow-engine project and to a later stage;
only the rig's side of that contract is decided here. The roadmap is not reshaped: the step kit
is placed inside the existing LR-2 and LR-3 rather than as an item of its own.

## Status
`completed`

## Result
Both records drafted.

- **DR-2, the load profile: external data over a code-owned vocabulary.** The code defines the
  scenarios, the steps and the invariants; the profile states the numbers, the composition and
  the parameter values, refers to scenarios by name out of a closed registry, is validated
  strictly with no silent defaults, and the profile file a run is given is the load-profile
  description the capture carries.
- **DR-3, the rig as a generation target.** Scenario code is the generation target, and the
  pattern is enforced by types: a thin step kit whose signatures make a step's content
  assertion, stable name and extractions non-optional; the surface stays declarative data;
  correlation rules are enumerable; the gate chain is the build, the conformance walk and
  external verification; the identifiers facing outward are a contract.

The two records meet in one place: the closed scenario registry that DR-2's profile names and
DR-3's generated code registers into.

## Decisions
- DR-2 drafted: [the load profile: external data over a code-owned vocabulary](../decisions/dr-2-profile-over-a-code-owned-vocabulary.md).
- DR-3 drafted: [the rig as a generation target](../decisions/dr-3-the-rig-as-a-generation-target.md).
- The step kit is born inside LR-2 and LR-3 as the refactoring those items force anyway, not as
  a roadmap item of its own — recorded in DR-3's consequences.
- Verification against the SUT's database stays outside the rig: it belongs to an external
  verifier beside the actualization scenario, and the rig owes it only stable identifiers —
  recorded in DR-3.

## Open questions
- The JSON parser for the profile: whether the DSL already carries one transitively or a
  dependency is added — settled with dependency approval when LR-3 implements DR-2.
- The ownership of the external verifier that reads all sources — exchange-desk correspondence
  for the stage that builds the actualization scenario.

## Next steps
1. The account pool documentation session, positioned before the registries, as handed off by
   the previous session.
2. LR-2, extracting the step kit and the correlation dictionary from the transport walk under
   DR-3's constraints.
3. LR-3, implementing DR-2: the profile schema, the loader, the strict validation and the
   equilibrium check.

## References
- Branch: `docs/dr_2-3-profile-and-generation-target`.
- Decision records: DR-2 `Draft`, DR-3 `Draft`.
- No other documents changed.
