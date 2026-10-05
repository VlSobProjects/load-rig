# Session 2026-08-29-documentation-1

## Type
`documentation`

## Goal
Register the account pool as a roadmap item of its own, positioned before the registries, so that
LR-2 starts on a pool the profile can actually run on instead of the fresh database's three
accounts.

## Completion criteria
- The roadmap carries a row for the account pool: the sizing of the pool — how many accounts and
  how many of them are managers — as explicit configuration, and its provisioning against the
  stack.
- The row stands before LR-2 in the order of the document, and the item's number follows the
  roadmap's own rule that a number is an identity, not a place.
- No other roadmap row changes.
- The session note is completed and the branch is ready to merge into `development`.

## Scope boundaries
This session does not size the pool, does not implement its provisioning, does not touch the
content of LR-2 or LR-4, and does not answer anything through the exchange desk. It adds one row
and nothing else.

## Status
`completed`

## Result
The roadmap carries LR-9, the account pool, placed between LR-1 and LR-2: the sizing of the pool
— accounts and managers — as explicit configuration, and its provisioning against the stack
through the administrator screens LR-1 proved. No other row changed.

## Decisions
- **The item is numbered LR-9 and placed before LR-2.** The roadmap's own rule decides both
  halves: a number is an identity, so the new item takes the next free one, and the order of the
  document is the order of the work, so the row stands where the work belongs — the session
  registry cannot be exercised on the three accounts of a fresh database.
- **The pool is an item of its own rather than a clause of LR-2 or LR-4.** Folding it into LR-2
  would make the registries' session depend on a running stack, which their row explicitly
  refuses; folding it into LR-4 would put the work after the registries that need its result. The
  cost is one more row and one more session.

## Open questions
None raised. What the pool must decide — the counts, the provisioning mechanics, whether the rig
or the SUT's seeding item creates the accounts — belongs to the LR-9 session itself.

## Next steps
1. LR-9: size the pool and provision it — the counts of accounts and of managers it settles are
   half of what LR-4 owes the SUT project.
2. LR-2, the session and task registries, exercised on that pool.

## References
- Branch: `docs/account-pool-roadmap-row`.
- Documents changed: `docs/roadmap.md` — the LR-9 row.
