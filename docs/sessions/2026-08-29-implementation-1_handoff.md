# Session 2026-08-29-implementation-1

## Type
`implementation`

## Goal
Settle the size and the composition of the account pool from the profile, hold both as explicit
configuration, and deliver the provisioning that brings a running stack's pool to that state
through the administrator screens LR-1 proved.

## Completion criteria
- The count of accounts and the count of managers among them are derived from the profile, the
  derivation is recorded, and both figures are explicit configuration — ready to go out with the
  LR-4 answer, not sent in this session.
- A provisioning entry point brings a stack to the configured pool: it creates the missing
  accounts through the administrator screens, walks each fresh account's forced password change
  once, and fails loudly on any refusal, the rendered-200 refusals included.
- Provisioning an already-provisioned stack changes nothing and reports so: the pool survives
  being asked for twice.
- `./gradlew build` passes from the command line, and a provisioning run against the running
  stack is reported with what it created and what it found already present.

## Scope boundaries
This session does not touch the session and task registries (LR-2), does not send the exchange
answer (LR-4), does not size the seeded task volume (the SUT's seeding item), and does not
change the walk of LR-1 beyond what reuse requires.

## Status
`completed`

## Result
Every criterion is met.

Delivered:

- the composition of the pool as the load model's own fact: twenty-five accounts — one
  administrator, eight managers, sixteen workers — with the derivation from the profile stated
  where the constants live, and the three accounts of a fresh database deliberately outside;
- the provisioning entry point, a documented command of its own, which creates the missing
  members through the administrator screens, walks each fresh account's forced password change
  and ends by proving every member's sign-in with the configured password;
- a new page mark on the surface, the password-change form a fresh account is held at, verified
  against the running stack rather than assumed.

Verified from the command line: `./gradlew build` passes, nineteen unit tests, none of which
needs a stack. The provisioning ran twice against the running stack: the first run created all
twenty-five members and proved each sign-in; the second created nothing, found all twenty-five
present and proved them again — the pool survives being asked for twice.

## Decisions
- **The pool is sized to the ceiling, not the baseline: twenty-five accounts.** One provisioned
  pool serves a whole campaign, the stepped run of LR-6 included, so the stand does not change
  between the runs a capture is compared against. A third of the working accounts are managers,
  because roughly a third of the step mix is a manager's work, and that carries the discussion
  scenario's demand for managers who are not the creator. A larger pool was rejected: every
  account beyond what the load can occupy thins the per-worker history the report is priced on.
- **Provisioning runs on the JDK's own HTTP client, not as a load plan.** It is stand
  administration, not load: its control flow is conditional per account, it leaves no result log
  and sends no answer-shape header, because none of its requests belongs to a capture. What it
  shares with the load model is the surface only, per DR-3.
- **Presence is proven, not looked up.** Every member is made to sign in with the configured
  password instead of being searched for on the users screen, which keeps the provisioning
  independent of how that screen lists or pages its rows. An account that exists under another
  password surfaces as a refused creation and stops the run loudly; a member left held at the
  password change by an interrupted provisioning is completed and proven.
- **The built-in accounts stay outside the pool.** The built-in administrator is the tool the
  pool is provisioned with, not a participant of the load.

## Open questions
- The whole pool shares one configured password. Enough for a stand the rig owns; if a capture
  is ever taken on a stand other people use, per-account secrets become configuration work.
- Whether the seeded history the SUT's seeding item produces refers to the pool by these names
  is the naming half of the LR-4 answer; the names are the contract this session fixed on the
  rig's side.

## Next steps
1. LR-2, the session and task registries, exercised on this pool.
2. The LR-4 answer through the exchange desk: the counts and the names of the pool, with the
   derivation, as the accounts half of what the SUT project asked back.

## References
- Branch: `feature/lr9-account-pool`.
- Decision records: none opened; the choices above are implementation choices inside DR-2 and
  DR-3.
- Documents changed: `README.md` — the provisioning is a documented command now.
