# Decision records

A decision record (DR) captures a choice that shapes the rig — a tool, a load-model pattern, a
coordination mechanism — with its alternatives, trade-offs and consequences. It is the lighter
sibling of the workflow-engine project's ADR process: the same discipline, without the separate
POC machinery, because the rig is smaller and its decisions are usually proven by the next work
item.

## When a DR is required

- A choice between tools, frameworks or protocols.
- A pattern every later piece of code will follow (how virtual users coordinate, how a profile
  is expressed, where run artifacts land).
- Reversing or superseding an earlier DR.

Small implementation choices stay in the session notes.

## Format

`dr-<number>-<short-name>.md`, numbered in creation order. Each record states:

- **Status:** `Draft` → `Accepted` → `Superseded by DR <n>`.
- **Context:** the problem and the constraints, by role.
- **Alternatives:** at least two, with trade-offs and costs.
- **Decision:** what was chosen and why.
- **Consequences:** what this binds, including what becomes harder.

An accepted DR is changed only by superseding it.

## Registry

The registry is the only index. Add the row when the record lands on `development`.

| # | Title | Status | Sessions |
| --- | --- | --- | --- |
| 1 | [One answer shape per capture: the fragment mode](dr-1-one-answer-shape-per-capture.md) | Accepted | 2026-08-28-implementation-1 |
| 2 | [The load profile: external data over a code-owned vocabulary](dr-2-profile-over-a-code-owned-vocabulary.md) | Accepted | 2026-08-28-architecture-1, 2026-08-29-implementation-3 |
| 3 | [The rig as a generation target](dr-3-the-rig-as-a-generation-target.md) | Draft | 2026-08-28-architecture-1 |
| 4 | [Registry concurrency: one monitor, valid at this rig's scale only](dr-4-registry-concurrency-at-this-scale.md) | Draft | 2026-08-29-implementation-2 |
| 5 | [A rig for this SUT, not a universal one: the adaptation ledger](dr-5-a-rig-for-this-sut-not-a-universal-one.md) | Draft | 2026-08-29-implementation-3 |
