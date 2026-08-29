# Sessions

A session is one narrow piece of work with a declared goal and a declared end.
Every session produces exactly one note in this directory.

## Session types

Closed list. A session has exactly one type.

| Type | Used for |
| --- | --- |
| `architecture` | Design choices captured as decision records |
| `implementation` | Code changes |
| `documentation` | Documentation and rules |
| `investigation` | Research that changes no code |
| `maintenance` | Dependencies, tooling, repository hygiene |

## File naming

`YYYY-MM-DD-<type>-<n>_handoff.md`

- The date is the day the session started.
- `<type>` is the session type from the list above.
- `<n>` counts the sessions of that type on that day, starting at `1`, and is always present.
- Example: `2026-08-29-implementation-1_handoff.md`

The file name carries no description on purpose. Do not infer the content of a session from its
name. To find a session, read the registry below.

## Lifecycle

1. At the start, create the session file with its type, goal and completion criteria.
2. During the session, keep the scope inside those criteria.
3. At the end, complete the file with the result, the decisions and the next steps.
4. Close the session with a git commit that includes the session note and related changes. If a
   session intentionally ends without a commit, state that explicitly in the session note with
   the reason.

A session that is interrupted still leaves its file, marked `interrupted`.

## Where it lives

- The session file is written in the working branch and lands with the work it describes.
- A session that produces no code still uses a branch, normally under `docs/`.
- The registry row below is added on `development` when the branch is merged.

## Content limits

- Record facts, decisions and next steps. Do not retell the course of the work.
- No code dumps, no command output, no logs.
- No local paths and no machine-specific facts: see `docs/rules/documentation-rules.md` §8.
- If the scope grew during the session, record the split and leave the rest to the next steps.

## Registry

The registry is the only index of sessions. The file names carry no description, so a session
that is missing from the registry is effectively lost. Add the row when the branch is merged.

- A row is one sentence; the facts are in the note.
- `DR` lists the decision records the session worked on, or `—`.
- `Status` is `completed` or `interrupted`. An `interrupted` session must be picked up or
  explicitly closed before related work continues.

| Date | Type | Summary | DR | Status | Branch | Link |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-08-28 | implementation | The transport skeleton: one linear walk that plays every role against the running stack, with the answer shape, the scraped token and the identities read out of the answers | DR-1 | completed | `feature/lr1-transport-skeleton` | [note](2026-08-28-implementation-1_handoff.md) |
| 2026-08-28 | architecture | The two patterns fixed before the code hardens around them: the profile as external data over a code-owned vocabulary, and the rig as a generation target whose pattern is enforced by types | DR-2, DR-3 | completed | `docs/dr_2-3-profile-and-generation-target` | [note](2026-08-28-architecture-1_handoff.md) |
| 2026-08-29 | documentation | The account pool registered as LR-9, an item of its own placed before the registries, which cannot be exercised on the three accounts of a fresh database | — | completed | `docs/account-pool-roadmap-row` | [note](2026-08-29-documentation-1_handoff.md) |
| 2026-08-29 | implementation | The account pool: twenty-five accounts sized to the ceiling of the load model, provisioned idempotently by the rig through the administrator screens and proven by sign-in | — | completed | `feature/lr9-account-pool` | [note](2026-08-29-implementation-1_handoff.md) |
| 2026-08-29 | implementation | The session and task registries: the transition table mirrored as the rig's own fact, tasks leased exclusively for moves and read freely for the discussion, sessions decoupled from virtual users and acquired by state | — | completed | `feature/lr2-session-and-task-registries` | [note](2026-08-29-implementation-2_handoff.md) |
| 2026-08-29 | implementation | The profile vocabulary and the day profile: external JSON over typed code loaded strictly with no defaults, the intensities computed rather than stated, and the population equilibrium checked before any load | DR-2 | completed | `feature/lr3-profile-vocabulary` | [note](2026-08-29-implementation-3_handoff.md) |
| 2026-08-29 | implementation | The step kit and the scenarios behind the closed names: DR-3's obligations taken by signatures, the correlation dictionary beside the surface, the transport context travelling with the session, and the mix checked to survive the populations | DR-3 | completed | `feature/lr3-scenario-wiring` | [note](2026-08-29-implementation-4_handoff.md) |
| 2026-08-29 | documentation | The LR-4 answers delivered through the exchange desk: the pool's composition, the computed mix survival, the hot-set convergence and the profile file owning the numbers, with the baseline refused until the calibrated run and the seeded-hot-tasks question asked back | — | completed | `docs/lr4-answers-to-sut` | [note](2026-08-29-documentation-2_handoff.md) |
| 2026-08-29 | implementation | The run harness laying each capture into a directory of its own — the description with computed targets written before the load, the run report beside the log — and the four-run conformance walk that fixed the cookie store, the required transition messages and the returned-task visibility, registering the warm start as LR-10 | — | completed | `feature/lr5-run-harness` | [note](2026-08-29-implementation-5_handoff.md) |
| 2026-08-29 | documentation | The SUT's answer carried into the brief and the roadmap: the closed list of refusal codes with the one split only the rig can make, the initial state passing whole to the SUT's seeding item, and the warm start rewritten on the premise that the measured stand held no history at all | — | completed | `docs/sut-answers-after-first-runs` | [note](2026-08-29-documentation-3_handoff.md) |
| 2026-08-29 | documentation | The answer back through the desk: what the rig takes as settled and where each fact landed, its own correction about the message a reassignment requires, and four questions the seeding must settle — each carrying the assumption held while it is unanswered | — | completed | `docs/reply-to-the-sut-answer` | [note](2026-08-29-documentation-4_handoff.md) |
