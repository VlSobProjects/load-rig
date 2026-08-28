# Implementation rules

These rules apply to every code change.

## 1. Before changing code
Read the code and the tests that surround the change.
Understand how the piece is used before editing it.
Before changing any code, give a short explanation and wait for confirmation or questions.

## 2. Size of a change
Keep a change small enough to review in one pass.
Preserve existing behaviour unless the task explicitly requires changing it.
Do not refactor code that the task does not require.

## 3. Style
Follow the conventions already present in the code.
Prefer explicit and simple code over clever code.
The load model reads as the specification it implements: scenario steps, mixes and parameters are
named after the business facts they encode, and every number that came from an exchange document
is a named constant or configuration value, never a literal in the middle of a plan.
Comment only what is not obvious from the code.
Do not commit commented out code or dead code.

## 4. Automated checks
The compiler and the test suite (`./gradlew build`) are the mandatory checks.
No linter or formatter is mandated yet; adopting one is a recorded decision, not a habit.

## 5. The command line is the source of truth
Verification means running Gradle from the command line.
IDE panels are a hint and may be stale; they never replace a command line run.

## 6. Tests
The registries and every piece of pure logic are covered by JUnit tests that need no running
stack. The transport and the profiles are proven by documented runs against the stand; a run's
evidence is its artifacts, and the session note records what was run.
Run the tests before merging into `development`, and report what was run.

## 7. Failures are explicit
A run that cannot proceed fails loudly with the reason; the rig never papers over a refused
token, an unexpected answer shape or a starved registry with a silent retry.
A capture full of failures the rig itself caused has measured the script, not the system.

## 8. What must not leak into a run
The constraints of `docs/brief.md` are code-level obligations: the operational endpoints are
never driven, the population equilibrium is checked rather than trusted, and a defect variant is
applied only by the run that asks for it.

## 9. Dependencies
Prefer the JDK and what jmeter-java-dsl already brings.
A new dependency must earn its place: it has to solve a real problem better than a small local
implementation, be maintained, and have an acceptable licence.
It requires explicit approval and is declared in `build.gradle`, test tooling separately from the
runtime.

## 10. Documentation of a change
A change that makes `README.md`, `docs/brief.md` or `docs/roadmap.md` inaccurate carries the
correction in the same branch. The rule and its scope live with the other merge preconditions in
`docs/processes/git-workflow.md`.
