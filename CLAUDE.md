# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this
repository.

## Project rules routing

This repository uses task-scoped documentation. `AGENTS.md` is the entrypoint. Read only what the
current task needs:

- Always: `docs/rules/general-rules.md`
- Implementation work: `docs/rules/implementation-rules.md`
- Design choices: `docs/brief.md` first, `docs/roadmap.md` second, then `docs/decisions/README.md`
- Documentation work: `docs/rules/documentation-rules.md`
- Git and branches: `docs/processes/git-workflow.md`
- Session notes: `docs/sessions/README.md` — every new session follows the lifecycle protocol
  there; every session note copies the structure of `docs/sessions/template.md`

Do not read unrelated docs, and create documents only inside the map defined in
`docs/rules/documentation-rules.md`. Ad hoc documents and summaries are forbidden.

## Non-negotiable rules (from docs/rules/)

- All committed artifacts are in English: code, comments, commits, branch names, docs. The
  conversation language with the user is free.
- Never commit to `main`. Branch from `development` as `<category>/<short-name>`
  (`feature/`, `fix/`, `refactor/`, `docs/`, `test/`, `chore/`, `poc/`); decision-driven work
  uses `<category>/dr_<number>-<short-name>`. Task branches merge into `development`.
- No scope creep: change only what the task requires.
- Never write a local path, user name or host name into a committed document; name the role of
  the thing instead.
- Before changing code, give a short explanation and wait for confirmation.
- Never commit without showing the full commit message first, presented as a file. Exception:
  a merge whose subject follows the template is made at once when the user asks for the merge.
- Design is discussed in critical mode: at least two alternatives, the trade-offs, the cost.
  Shaping choices are recorded in `docs/decisions/`.
- Working tree hygiene: every file is either tracked or ignored.
- Verification means command-line runs of `./gradlew build`; report what was actually run. Never
  claim unverified success.
- New dependencies require justification and explicit approval.
- `exchange/` is the correspondence desk with the other projects, deliberately outside git and
  never a source of truth: what an answer changes is written into `docs/`.

## Commands

Java 21 (Gradle toolchain), Gradle via the frozen wrapper.

```
./gradlew build          # compile and run all tests
./gradlew test           # tests only
./gradlew test --tests loadrig.DslWiringTest   # one test class
```

The unit tests need no running SUT stack. Load runs against the stack are documented per run
harness command as they appear (`docs/roadmap.md` LR-5).

## Architecture

The rig drives the SUT stack (a separate project: a task-tracker web application observed as a
Docker stack) with a business-shaped load and produces the injector side of a capture: the
load-profile description and the JTL result log, consumed by the workflow-engine project's
`load-test-triage` scenario. `docs/brief.md` is the full statement.

- The load model is code: JMeter 5.6.x through `jmeter-java-dsl`, no JMX files.
- `loadrig.model` — scenarios, step mix, intensity profiles; every number from an exchange
  specification is a named constant or configuration value.
- `loadrig.registry` — the session registry (SUT sessions decoupled from virtual users, picked
  by state) and the task registry (mirrors the SUT's task transition table); plain Java in the
  injector JVM, unit-tested without a stack, deliberately no external store.
- `loadrig.run` — run harness and capture artifacts; JTL timestamp semantics set explicitly.
- Binding constraints (core map, profile discipline, what the rig must never do) are listed in
  `docs/brief.md` and are code-level obligations.
