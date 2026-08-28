# General rules

These rules are binding for every task in this repository.
They are intentionally short. Details live in the linked documents.

## 1. Language
All artifacts committed to this repository must be written in English:
documentation, code, identifiers, comments, commit messages, branch names, decision records,
session notes. This is not negotiable.
The conversation language with the user is free and does not affect this rule.

## 2. Branches
Never commit directly to `main`.
Every change is made in a branch created from `development`.
Leave no file that is neither tracked by git nor ignored.
Branch categories, naming, lifecycle, merge rules and working tree hygiene: see
`docs/processes/git-workflow.md`.

## 3. Design discussions
Design is discussed in critical mode, not in agreement mode.
Minimum requirements: state at least two alternatives, name the trade-offs, and state the cost of
the change.
A choice that shapes the rig — a tool, a model of load, a coordination pattern — is recorded as a
decision record. Details: see `docs/decisions/README.md`.

## 4. Session scope
Every session declares its type, its goal and its completion criteria in its session file before
work starts.
Keep the scope as narrow as possible; split large tasks into several sessions instead of widening
one.
If the scope grows during the session, stop and propose a split.
Every session ends with a completed session note containing the next steps.
Details: see `docs/sessions/README.md`.

## 5. Document creation
Documents are created only inside the approved documentation structure.
Creating a new kind of document, or a document outside the structure, requires explicit approval
first.
The structure may change, but only as a deliberate, justified decision.
Generating ad hoc documents, summaries or alternative formats is forbidden.
Details: see `docs/rules/documentation-rules.md`.

## 6. No scope creep
Change only what the task requires.
Do not touch unrelated files and do not fix unrelated problems without asking first.

## 7. Verification
Before reporting a task as done, run the build and the tests from the command line:
`./gradlew build`. Never claim success that was not verified.
State explicitly what was executed and what was not.
A load run against the SUT stack is verification of its own kind: report the command, the stand
state and what the artifacts show.

## 8. Ask instead of guessing
When the task is ambiguous or a decision has several reasonable outcomes, ask a short, specific
question. Do not silently choose for the user.

## 9. Dependencies
Keep external dependencies minimal.
A new dependency requires justification and explicit approval.

## 10. Information economy
Spend research effort where it pays off.
Do not run long code searches for information the user can provide immediately, and do not ask
trivial questions that a quick look at the code answers.
For facts that change over time — library versions, releases, current APIs — verify them from
documentation or the internet instead of relying on memory.
When unsure whether to search or ask, ask.

## 11. The exchange desk is not a source of truth
Correspondence with the other projects lives in `exchange/`, deliberately outside git.
When an answer lands or a specification is agreed, what it changes is written into `docs/` — the
brief, the roadmap or a decision record — and the exchange row is marked accordingly.
A binding number from a specification becomes explicit configuration in this repository, never an
implicit constant.

## 12. Commit confirmation
A commit is never made without the user seeing its message first.
Present the complete message, subject and body, and wait: the user confirms it, corrects it, or
makes the commit themselves.
The message is presented as a file, so that it can be read, copied and extended without being
retyped. This covers every commit, a one line registry commit included.
A merge commit is the single exception. When the user asks for the merge, its subject is written
from the template in `docs/processes/git-workflow.md` and the merge is made at once, because a
templated subject leaves nothing to review. A merge message that departs from the template is
presented like any other.
The form of the message is defined in `docs/processes/git-workflow.md`.
