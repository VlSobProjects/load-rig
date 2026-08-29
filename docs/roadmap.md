# Roadmap

The destination is stated in [`brief.md`](brief.md): produce the injector side of every capture
the `load-test-triage` scenario needs, answer the SUT specification's questions, and hold the
constraints the exchange documents bind this project to.

Each item is a unit of work with its own session. An item's number is its identity, not its
place; the order of this document is the order of the work. A row is rewritten, not appended to.

| # | Item | State |
| --- | --- | --- |
| LR-1 | **The transport skeleton.** Form login with the CSRF token scraped per session — the fragments carry none, and a browser holds the token of the page it loaded — the fragment answer shape held for a whole run, the identity of a task and of a provisioned account read out of the answers that produced them, one walk per role, and the password change a fresh account is held at | done |
| LR-9 | **The account pool.** The accounts a run signs in with, decoupled from the fresh database's three: how many the profile needs and how many of them are managers — the discussion scenario needs managers who are not the creator of the task they comment on — held as explicit configuration and provisioned against the stack through the administrator screens LR-1 walked. Half of the LR-4 answer, and the ground the registries are exercised on. The walk retires when the SUT's seeding item takes the whole initial state; until then its idempotence is the guarantee that a seeded stand meets a no-op | done |
| LR-2 | **The session and task registries.** SUT sessions decoupled from virtual users and picked by state; the task registry mirroring the SUT's transition table so a step finds a task it may act on; unit-tested against that table without a running stack | done |
| LR-3 | **The day profile.** The step mix, the hot-set skew, the per-step think times and the population equilibrium of the specification, with the equilibrium invariant checked by the rig rather than trusted | done |
| LR-4 | **The answers owed to the SUT project.** Accounts and managers count, mix survival, the convergence mechanism, the intensities and skew as explicit configuration — delivered through the exchange desk | done |
| LR-5 | **The run harness and the capture artifacts.** A documented command per run; the JTL configuration with explicit timestamp semantics; the load-profile description authored per variant; the artifact layout a capture consumes | done |
| LR-10 | **The warm start and the scaled balance.** The first measured runs realized the transition mix four times under its target against a stand that held no history at all — the seeded volume is the SUT's own phase-2 item and had not run — so the population the profile needs was absent, not merely invisible to the task registry. The census the profile needs is computed rather than assumed; a set-up group reads what the stand already holds from the list page ordered by due date, the only inventory the application offers, and creates the difference before the window opens. Written that way it degenerates to pure creation on an empty stand and to pure reading on a seeded one, so the seeding landing costs it no rewrite — and the mix and the population balance hold from the first minute and keep holding as the profile scales up | pending |
| LR-6 | **Calibration support.** The stepped maximum-finding run over the same profile shape; the morning and evening profile variants; the first measured baseline against the service levels | pending |
| LR-7 | **The injector-side defect variants.** The flooding thread group, the starved injector and the injector dying mid-run, each applied deliberately per variant | pending |
| LR-8 | **The Gatling transport poc.** The LR-1 scenarios on Gatling's Java DSL in a `poc/` branch: per-sample export viability and cross-user coordination under the async model, measured against the same stand | pending |
