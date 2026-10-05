# Session note

## Type

`documentation`

## Goal

Give the desk a way to tell every project something that needs neither an answer nor a record,
without a letter to each of them, and say in the rules when a letter is written at all.

## Completion criteria

1. The desk carries a kind of letter for that, excluded from what counts as waiting, and the first
   such letter is written as a draft.
2. This project's rules state it, and state that reading is never confirmed by another letter.
3. No project is sent a letter about it.

## Scope boundaries

The eight rules in force are not reworded and no other project's rules are touched. Nothing is
sent to a project to announce the new kind: that would be the flood it exists to prevent.

## Status

`completed`

## Result

All three criteria are met.

The desk has a kind `info`: a letter for every project, with no addressee, that asks for no answer
and no record and is never counted as waiting. A view `Info` shows the released ones, and the view
of what waits leaves them out. The first, `EX-15` of 2026-10-05, tells what the first day showed
about reading the desk within the limits of its workspace; it is a draft until the user releases
it.

The ninth rule says when a letter is written: only when it asks for something or changes
something. Reading is confirmed in the letter's own row, an answer is not answered, and what every
project should merely know is one letter of kind `info`.

What a session must know about the desk is in the descriptions of its fields, which a session is
given when it opens the desk; the description of the database itself is not.

## Decisions

- **One letter with no addressee, rather than a letter to each project.** The eighth rule gives a
  letter one addressee so that its status and the place it lands mean one conversation. A letter
  that has neither needs no addressee, and the price of that rule - one row and one release for
  each project - buys nothing for it. Five identical notices were drafted that way on the same day
  and deleted by the user as noise.
- **No addressee, rather than every project as addressee.** A letter addressed to every project
  would be counted as waiting by each of them for ever, and would break the rule of one addressee
  in every project that states it. With none, both rules stay true as written and no project has
  to amend anything.
- **No guarantee that it is read, and none claimed.** A letter nobody has to acknowledge cannot be
  known to have been read; that is what asking for no record means. What a project must act on
  stays a letter to that project.
- **No round of letters to announce it.** The other projects learn of the kind from the desk's
  field descriptions and from the letter itself, and may state it in their own rules with their
  next change.

## Open questions

Whether the other projects should state the ninth rule in their own documents. Nothing breaks
while they do not: their sessions meet the kind on the desk.

## Next steps

1. The user releases `EX-15`, or deletes it.
2. The answers of the two projects that have not yet joined the desk.

## References

- Branch: `docs/desk-info-letters`
- Changed: `docs/processes/exchange-desk.md`
- On the desk: `EX-15` of 2026-10-05, reading the desk within the limits of its workspace
