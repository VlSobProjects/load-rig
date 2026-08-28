# Roadmap

The destination is stated in [`brief.md`](brief.md): produce the injector side of every capture
the `load-test-triage` scenario needs, answer the SUT specification's questions, and hold the
constraints the exchange documents bind this project to.

Each item is a unit of work with its own session. An item's number is its identity, not its
place; the order of this document is the order of the work. A row is rewritten, not appended to.

| # | Item | State |
| --- | --- | --- |
| LR-1 | **The transport skeleton.** Form login with CSRF scraped per page, the fragment answer shape held for a whole run, task id extraction from the returned HTML, one small walk per role — proven by a smoke run at a few virtual users against the running stack | pending |
| LR-2 | **The session and task registries.** SUT sessions decoupled from virtual users and picked by state; the task registry mirroring the SUT's transition table so a step finds a task it may act on; unit-tested against that table without a running stack | pending |
| LR-3 | **The day profile.** The step mix, the hot-set skew, the per-step think times and the population equilibrium of the specification, with the equilibrium invariant checked by the rig rather than trusted | pending |
| LR-4 | **The answers owed to the SUT project.** Accounts and managers count, mix survival, the convergence mechanism, the intensities and skew as explicit configuration — delivered through the exchange desk | pending |
| LR-5 | **The run harness and the capture artifacts.** A documented command per run; the JTL configuration with explicit timestamp semantics; the load-profile description authored per variant; the artifact layout a capture consumes | pending |
| LR-6 | **Calibration support.** The stepped maximum-finding run over the same profile shape; the morning and evening profile variants; the first measured baseline against the service levels | pending |
| LR-7 | **The injector-side defect variants.** The flooding thread group, the starved injector and the injector dying mid-run, each applied deliberately per variant | pending |
| LR-8 | **The Gatling transport poc.** The LR-1 scenarios on Gatling's Java DSL in a `poc/` branch: per-sample export viability and cross-user coordination under the async model, measured against the same stand | pending |
