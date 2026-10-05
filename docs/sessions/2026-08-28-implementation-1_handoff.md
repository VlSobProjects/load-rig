# Session 2026-08-28-implementation-1

## Type
`implementation`

## Goal
Prove the transport skeleton of roadmap item LR-1: drive the SUT the way a browser does — form
login with a scraped CSRF token, one held answer shape, task identity read out of the returned
HTML — and walk each of the three roles once against the running stack.

## Completion criteria
- A test plan built from the DSL walks, in one thread, the four legs of the transport: a manager
  creating and settling, a worker acting on the task, a manager reporting, an administrator
  browsing unscoped, creating a user and deleting the task.
- The CSRF token is scraped from the page each POST is made from; a refused token fails the run
  loudly instead of being retried.
- The answer shape is fixed for the whole run by a single named constant, per DR-1.
- The task identity used by every step after a creation is extracted from the answer of that
  creation, not guessed.
- The forced password change of a freshly created account is walked once, because without it such
  an account cannot reach any other screen.
- Every value that came from an exchange document or from the stack's own deployment facts is a
  named constant or a configuration value.
- `./gradlew build` passes from the command line, and a smoke run of a few virtual users against
  the running stack leaves a JTL log.

## Scope boundaries
This session does not touch: the session and task registries (LR-2), the step mix, the skew and
the think times (LR-3), the account pool and its provisioning, the run harness and the capture
artifact layout (LR-5), the defect variants (LR-7). The walk is deliberately linear and
self-sufficient per virtual user, so that it settles nothing that LR-2 has to decide.

## Status
`completed`

## Result
Every criterion is met. The walk runs in six legs rather than the four the criteria named: the
fresh account's password change is a leg of its own, and a sixth leg was added during the session
so that the walk takes back the account it provisioned — see the decisions below.

Delivered:

- the HTTP surface of the application as one named place: the addresses, the form fields and the
  closed lists of the scope and the sort key, each verified against the running stack rather than
  copied from prose;
- the walk itself, assembled from the DSL, with the token scraped per sign-in, the answer shape of
  DR-1, the identity of a task and of a provisioned account read out of the answers that produced
  them, and the walk's own tag making one run's rows unmistakable for another's;
- the configuration a run states on the command line, with defaults that match a stack brought up
  from nothing;
- the smoke run entry point, which leaves a JTL log and fails loudly when any step was refused.

Verified from the command line: `./gradlew build` passes, six unit tests, none of which needs a
stack. The smoke run drove the stack with two virtual users, one walk each: 72 records, no refused
step, and the stand was left as the walk found it — no task and no account outlived the run.

The result log was read rather than trusted: one record per user-visible step, and no sample's
start stamp falls inside the sample before it in its thread, which is what start-stamped records
look like.

## Decisions
- **DR-1, one answer shape per capture: the fragment mode.** Drafted in this branch. The header
  goes on the requests the page's script library would make and on no others: the sign-in, the
  sign-out, the account forms and the report are ordinary form posts in the interface, and a
  browser sends no such header on them.
- **Three refusals the application answers with a rendered page and a 200** — a refused sign-in,
  a duplicate account and a rejected password change — are asserted explicitly. A rig reading only
  the status walks past all three; the first version of this walk did, and a run reported success
  while two accounts had not been created.
- **The timestamp semantics of the JTL is set as a property of the machine**, not through the
  engine. The injector reads it once, when the class that stamps a sample is first loaded, so a
  value handed to the engine arrives too late. This was found by reading a log the rig had just
  written: the stamps were the moments the answers came back, which is exactly the reading the
  brief forbids.
- **The result log carries one record per user-visible step.** A redirect the application answers
  with is part of the step a person performed; its own record beside the step's would make every
  rate computed from the log count the same act twice.
- **The walk closes what it opens.** It provisions an account, proves it usable and takes it away
  again, and its task is deleted by the administrator. Without that a second run against the same
  database is refused the names it asks for — which is how the leftover was found.
- **The injector states its own language** instead of taking it from the machine a run starts on.
- Implementation choices, recorded here rather than as records of their own: the transport
  configuration is system properties with defaults, and the smoke run is a program started by its
  own command rather than a test, so that the build stays independent of a running stack.

## Open questions
- The rig sends no `Accept-Language`, and the application serves an anonymous visitor the language
  of that header. A browser sends one. Whether the rig should, and which value a capture is taken
  under, is a question for the profile work.
- The generator was not pinned to the cores the core map allots it for this smoke run. For a run
  of a few seconds at two virtual users this changes nothing; for a capture it is binding, and the
  pinning belongs to the run harness.
- Where the artifacts of a capture live permanently. The smoke writes beside the repository,
  outside version control; the host agreement has not settled the question.
- **A question owed to the SUT project:** the forced password change is a legitimate step that
  writes to the database, and nothing states how often it happens in a working day. It is not in
  the proposed step mix at all. The rig cannot invent the figure; it goes out with the answers of
  LR-4.

## Next steps
1. The account pool: the roadmap row for it, in a documentation session of its own, positioned
   before the registries — the session registry cannot be exercised on three accounts.
2. The provisioning of that pool, and the count of accounts and of managers it settles, which is
   half of what LR-4 owes the SUT project.
3. LR-2, the session and task registries.

## References
- Branch: `feature/lr1-transport-skeleton`.
- Decision record: DR-1, one answer shape per capture, `Draft`.
- Documents changed: `README.md` — the smoke run is a documented command now.
