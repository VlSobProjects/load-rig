# Session note

## Type

`documentation`

## Goal

State, as this project's rule, a correspondence desk that is one shared database instead of a
folder per project mirrored by hand, and prepare that database.

## Completion criteria

1. The desk exists as one database with the fields the rules rely on, and is empty.
2. The rules of this repository say where correspondence lives, who releases a letter, what a
   session may read and what it may not do with a letter, in one process document that every
   other mention points to.
3. Every live mention of `exchange/` as the desk is corrected, and the folder is named as what it
   becomes: the correspondence written before the desk was shared.
4. Nothing is carried over and no letter is written: the move itself is the user's decision, taken
   with the other projects.

## Scope boundaries

No letter of the old desk is migrated and none is edited. No rule of another project is touched:
each adopts the desk in its own repository. No decision record: the desk is how the projects
talk, not a choice that shapes the rig.

## Status

`completed`

## Result

All four criteria are met.

The desk is a database named `Exchange desk`, private to the user until it is placed. A letter is
one row: its status, the project that wrote it and the projects it addresses, its kind, the date of
the letter apart from the day of its release, the letters it answers, and where the recipient wrote
what it changes. It carries an identifier of its own, which is how a document or a commit cites a
letter now that there is no file name to cite. Two views sit on it: the letters by status, and the
drafts waiting to be released.

The rules are in `docs/processes/exchange-desk.md`, and the eleventh general rule, the document
map, the documentation index, the git workflow, the two agent entrypoints, the README's layout and
the ignore file point to it or are corrected to agree with it.

## Decisions

- **One copy of a letter, because the hand copy has already failed.** The fifth answer to the SUT
  project sat for three days marked sent and never carried, and on the day of this session a
  letter again waited on a copy. Mirroring also grows with every pair of projects that correspond.
- **A shared folder was the alternative and is the fallback.** It removes the copy as well, needs
  no network and no connector, and any agent can read it. It was not chosen because it offers no
  place for the release to happen: a status a person moves is what replaces the hand that used to
  carry the letter.
- **The release stays with a person.** The hand copy was also the only moment at which the user
  read what one project's session said to another's. A shared desk removes that moment unless a
  rule keeps it, so a session writes drafts only and reads only what was released.
- **A letter is a request and not an instruction.** With one copy, text written by one session
  reaches another with nothing in between. A session acts on a letter because the user made it the
  session's goal.
- **A sent letter is frozen.** The copy on the other desk used to be the snapshot of what was
  sent; with one copy the rule has to say it, and a correction becomes a new letter.
- **A session may count what waits, and no more.** With the hand that carried a letter gone,
  nothing tells a project that a letter addressed to it was released. The user is that channel
  already, by releasing it; what can go wrong is a released letter forgotten. So a session may say
  how many released letters have not landed, and opens one when asked. A notice from one session
  to another was the alternative and is the channel the release rule exists to prevent. Raised by
  the JobSearch project, the first to adopt the rule after this one.
- **A letter carries nothing that every project may not read.** One copy is read by every
  session, and what a letter changes lands in repositories that are public. Raised by the same
  project, which now keeps personal context out of its letters.
- **The address stays out of the repository.** The repository is public and the desk is not; the
  rules name the database and a letter's identifier, and a session finds the desk by its name.
- **History is not migrated.** The old letters are cited by date and subject across the session
  notes and the decision records, and those citations stay true only if the letters stay put.

## Open questions

- When the desk opens. The rule lands here first, but a desk one project writes to and the others
  do not read is not a desk: the other projects adopt the same rule before the first letter is
  written there.
- Where the database lives in the user's workspace; it was created private and unplaced.
- Whether every agent that works in these projects can reach the desk. A project worked by an
  agent without the connector is cut off, and for that project the shared folder is the answer.

## Next steps

1. The other projects adopt the rule, each in its own repository.
2. The first letter on the desk is the notice that it is open, addressed to every project.

## References

- Branch: `docs/shared-exchange-desk`
- New: [`docs/processes/exchange-desk.md`](../processes/exchange-desk.md)
- Changed: `docs/rules/general-rules.md` §11, `docs/rules/documentation-rules.md` §1,
  `docs/README.md`, `docs/processes/git-workflow.md`, `CLAUDE.md`, `AGENTS.md`, `README.md`,
  `.gitignore`
