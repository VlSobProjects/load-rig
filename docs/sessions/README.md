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
