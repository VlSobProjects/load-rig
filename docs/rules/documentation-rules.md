# Documentation rules

These rules apply to every documentation change.

## 1. The document map is closed
Only the documents listed below may exist:

| Location | Purpose |
| --- | --- |
| `README.md` | What the project is, what it is for, how to build and run it |
| `AGENTS.md` | Agent entrypoint, routing only |
| `CLAUDE.md` | Claude Code entrypoint; routes like `AGENTS.md`, plus a condensed non-negotiable-rules/commands summary (stated exception to §4, kept in sync with the documents it summarizes) |
| `LICENSE` | The licence the repository is published under |
| `docs/README.md` | Documentation index |
| `docs/rules/` | General and task specific rules |
| `docs/brief.md` | What the rig is for, the binding facts received, the questions owed back |
| `docs/roadmap.md` | The ordered work items and their state |
| `docs/decisions/` | Decision records and their registry |
| `docs/sessions/` | Session notes and the session registry |
| `docs/processes/` | Workflows such as git |

`exchange/` is the correspondence desk with the other projects and is deliberately outside git:
its documents change when the other side answers. It is not part of this map and is never a
source of truth — see `docs/rules/general-rules.md` §11.

Creating a document outside this map, or a new kind of document, requires explicit approval
first. The map may change, but only as a deliberate decision.

## 2. Why versus what
A decision record states why a choice was made, with its alternatives and consequences.
`README.md` and `docs/brief.md` state what is, without repeating rationale.

## 3. One fact in one place
State a fact in a single document and link to it from elsewhere.
Do not copy content between documents.
A number received from another project lives in the exchange document and in the configuration
that encodes it; documents name it by role.

## 4. Entrypoints stay thin
`AGENTS.md` routes to the relevant documents by task type and carries no rule content of its own.
`CLAUDE.md` routes the same way but is exempt: it may carry a condensed
non-negotiable-rules/commands summary, kept in sync with the documents it draws from; it does not
replace them as the source of truth.

## 5. Format
- Markdown, English, short declarative sentences.
- One topic per document, with a clear title.
- Prefer a list or a table over prose when the content is a set of rules or states.
- Do not pad documents with restated context or summaries of other documents.

## 6. Update or delete
A document that no longer matches reality is corrected or removed.
Do not keep superseded documentation as ad hoc `archive`, `old` or versioned copies.
The historical record is the decision-record history and the session notes.

## 7. No side documents
Do not write reports, summaries, plans, analyses or work logs into the repository.
Session outcomes belong in `docs/sessions/` in the defined format, and nowhere else.
Ephemeral working notes stay outside the repository.

## 8. No machine-specific facts
Committed documentation states facts about the project, not about the machine it was written on.
Never write a local path, a user name, a host name or a personal directory into a document; name
the role of the thing instead. A fact that is true only on one machine lives outside the
repository. An illustrative path in an example is not a machine-specific fact: it names no one
and exists nowhere.
