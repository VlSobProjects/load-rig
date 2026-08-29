# Session 2026-08-29-implementation-2

## Type
`implementation`

## Goal
Deliver the coordination state of one injector JVM: the task transition table mirrored as code,
the task registry that hands a step a task it may act on, and the session registry that
decouples SUT sessions from virtual users — unit-tested against the table without a stack.

## Completion criteria
- The SUT's task transition table is code in `loadrig.registry`: the statuses, the transitions,
  and who may apply which from which status, with the derivation from the scenario
  specification stated where the table lives.
- The task registry hands a step a task by transition and acting account, exclusively — no two
  sessions move one task — applies the outcome of the transition, and holds hot-set membership
  for the discussion scenario's convergence. An illegal transition is refused loudly.
- The session registry decouples SUT sessions from virtual users: a session is acquired by
  state — a role, one named account such as the creator of the task being settled, a manager
  other than named accounts — and is never held twice at once.
- Starvation is loud: a registry with nothing that fits states the reason and stops, with no
  silent retry.
- `./gradlew build` passes from the command line, with every piece of the new logic covered by
  unit tests that need no running stack.

## Scope boundaries
This session does not weight the picking (the hot-set skew and the mix are LR-3, the profile),
does not wire the registries into the transport walk or any scenario, does not start the step
kit of DR-3 (the registries do not force it), and does not send the LR-4 answer.

## Status
`completed`

## Result
Every criterion is met.

Delivered, all of it plain Java in `loadrig.registry` with no external store:

- the transition table as the rig's own fact: six statuses — the specification names `OPEN`,
  `APPROVED` and `ACKNOWLEDGED`; the three between them are the rig's names for the states the
  scenarios describe — and the eight transitions of the surface, each with the statuses it moves
  from, where it lands and whose relation to the task it demands, with the derivation stated
  where the table lives;
- the task registry: a step leases a task by transition and acting account, exclusively, and
  records the outcome, which follows the table; reads need no lease, because several sessions on
  one hot task is the discussion scenario; hot-set membership is a flag that counts only while
  the task is open, which is the specification's definition of a hot task;
- the session registry: sessions belong to pool accounts and live across iterations; a step
  acquires by state — an account to sign in with, a free session of a role, one named account,
  a manager other than named ones — and one session is never held twice at once;
- loud starvation: both registries refuse with the reason — who asked and for what — and a
  wiring mistake (a stranger account, a stale lease, an assignee on a non-hand-out) is an
  illegal-argument or illegal-state refusal, distinct from starvation on purpose.

Verified from the command line: `./gradlew build` passes, forty-nine unit tests of which thirty
are the new registry tests, none needing a stack. One test walks a task through all eight
transitions and all six statuses via registry operations alone. No run against the stack was
made: the registries are deliberately stack-free.

## Decisions
- **The intermediate statuses carry the rig's own names.** `COMPLETED`, `RETURNED`, `REFUSED`
  are not spellings read from the SUT: the rig never reads its database, so the registry mirrors
  the behaviour of the transition table, not a schema. The alternative — asking the SUT project
  for its enum — was rejected as a dependency on an implementation detail the capture never sees.
- **Relations decide, not roles.** The permission check works on creator and assignee, because a
  manager assigned a task by another manager finishes it as its assignee. The administrator's
  right to delete is the one role-owned right in the table.
- **Moving needs a lease, reading does not.** Two sessions never move one task, so a refused
  transition in a capture is the system's answer and not the rig racing itself; concurrent reads
  of one hot task are the discussion, the profile's one deliberate concurrency.
- **The registries do not schedule.** Which task or session is handed out is first fit; how
  often a step asks for hot rather than cold, and whether a starved step may wait, is the
  profile's decision (LR-3). Starvation here is an exception, never a silent retry.
- **One session per account, encoded by the key.** The session registry keys its state by
  username, so two live sessions of one account are structurally impossible. This encodes the
  specification's anti-requirement - virtual users must not share an account, or the run
  benchmarks lock contention and every report asks about the same worker - rather than assuming
  the SUT forbids concurrent sign-ins, which it probably does not. A load model that wanted
  several sessions per account would key by a session id, not by name; that boundary sits with
  the scale boundary of DR-4.
- **The step kit of DR-3 was not started.** The registries forced no scenario refactoring, and
  the kit is born of LR-3's, not speculatively.

## Open questions
- Whether the seeded history the SUT's seeding item produces can pre-fill the task registry —
  and in what form the rig learns the seeded tasks' identities — belongs to the LR-4/LR-5
  conversation; the registry accepts pre-registration already.
- The transport context of a session - the cookie store and the CSRF token of the last page
  the session loaded - is deliberately not the registry's: the lease carries the username only.
  Today the transport walk holds both per thread (the DSL's cookie element and the per-thread
  token variable), which is right while one thread is one session. LR-3 breaks that equality:
  a session outlives the iteration and passes between threads, so the context must travel with
  the session, joined by username - valid exactly because of the one-session-per-account
  invariant. Where it lives is LR-3's first wiring decision: a transport-side map by username,
  or a typed attachment held by the session registry, attached at sign-in and dropped at
  sign-out. The post-session review leaned to the attachment, because it makes the sign-out and
  the dropped context one act instead of two structures kept consistent by discipline.
- First-fit picking is deterministic and can favour early accounts under contention; whether
  LR-3 needs fairness or randomization in the pick is left to the profile work.

## Next steps
1. LR-3, the day profile: the step mix, the skew, the think times and the equilibrium invariant,
   wiring the registries into scheduled scenarios.
2. The LR-4 answer through the exchange desk: the accounts half is ready since LR-9; the
   convergence half can now state the mechanism — the hot set and the session registry's
   manager-other-than acquisition.

## References
- Branch: `feature/lr2-session-and-task-registries`.
- Decision records: DR-4, opened by the post-session review - the one-monitor concurrency of
  the registries scoped to this rig's scale, with the corporate-scale alternative and the
  supersession triggers named. The remaining choices above are implementation choices inside
  DR-2 and DR-3.
- Documents changed: `docs/roadmap.md` — LR-2 marked done.
