# Git workflow

This document defines the branch structure and the rules for working with git in this repository.

## Long lived branches

- `main` — the stable line. It holds work that is known to work and is the line that gets tagged.
- `development` — the integration line. Task branches are merged here.

Never commit directly to `main`.
Direct commits to `development` are allowed only for opening or editing a Draft decision record
together with its registry row, other decision status changes, and trivial corrections such as
typos.

## Branch categories

Every branch is created from `development` and lives in a category folder:

- `feature/` — new functionality
- `fix/` — defect fixes
- `refactor/` — structural change without behaviour change
- `docs/` — documentation and rules
- `test/` — test only changes
- `chore/` — tooling, dependencies, configuration
- `poc/` — experiments

## Naming

- Format: `<category>/<short-name>`
- Decision-driven work: `<category>/dr_<number>-<short-name>`.
- Use lowercase ASCII and hyphens inside the short name.
- The name states what the branch does, not who works on it.

## Working rules

- One branch per task. If the task splits, create a new branch instead of widening the current
  one.
- Branch from `development`, not from another task branch, unless the work genuinely depends on
  it.
- Delete the branch after it is merged, except for a `poc/` branch that is deliberately kept as a
  reference implementation.
- Keep the branch focused: unrelated changes belong to their own branch and category.

## Commit messages

A commit message is written in English and states why the change was made.
What changed is already in the diff.

Form:

```
<type>: <what the commit does, one line>

<body: the reason, the alternative that was rejected, the consequence a reader cannot see in the diff>

Verified: <the commands that were run and their result>
```

- `<type>` is the branch category of the work in its commit form: `feat`, `fix`, `refactor`,
  `docs`, `test`, `chore`, `poc`. A `feature/` branch commits as `feat:`.
- The subject is one line, lower case after the type, without a trailing period, at most 80
  characters. It names what the commit does, not which files it touched.
- The body is wrapped at 100 characters and written as prose. Use a list only when the change has
  several independent parts.
- `Verified:` is required by every commit that touches code, and names what was actually run, for
  example `Verified: ./gradlew build, BUILD SUCCESSFUL, 4 tests passed.`
  A commit that touches no code carries no such line.
- A commit whose subject already says everything needs no body: registering a session, a registry
  row, a typo.

A multi line message is written to a file and committed with `git commit -F <file>`, to keep the
shell's quoting out of the message.

The message is shown to the user before the commit is made: see
`docs/rules/general-rules.md` §12.

## Merging

- Task branches are merged into `development`.
- A merge commit states what the branch delivers:
  `Merge <branch>: <what the branch delivers, one line>`.
  It carries no body; the reasoning is in the commits of the branch itself.
- `development` is merged into `main` when the state is stable and worth marking as such.
- A `poc/` branch is never merged into `development`.
- Before merging into `development`, `./gradlew build` must pass.
- A branch that makes `README.md`, `docs/brief.md` or `docs/roadmap.md` inaccurate carries the
  correction in the same branch.
- When merging into `development`, update every registry the branch touched: the decision
  registry (`docs/decisions/README.md`) and the sessions registry (`docs/sessions/README.md`).

## Working tree hygiene

Every file in the working tree must be in one of two states: tracked by git, or ignored.
A file that is neither committed nor ignored is an unresolved question, not a normal state.

Check this regularly, and always before finishing a task:
- inspect the working tree for untracked and uncommitted files,
- for each one, decide whether it belongs in the repository or in `.gitignore`,
- when the answer is not obvious, raise the question with the user instead of deciding silently,
- do not finish a task or session with uncommitted tracked changes unless the user explicitly
  accepts ending uncommitted.

Once a remote exists, also check for commits that were never pushed and raise them the same way.

### What belongs in `.gitignore`
Propose adding anything that is useful only on the local machine:
- build output and tool caches,
- IDE and editor settings that are personal rather than shared,
- local helper scripts and scratch files,
- run artifacts that a capture consumes from outside the repository,
- the `exchange/` correspondence desk.

Never commit secrets, credentials or local configuration containing them.
