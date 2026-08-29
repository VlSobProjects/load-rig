# Session 2026-08-29-implementation-3

## Type
`implementation`

## Goal
Implement DR-2's half of LR-3: the day profile as an external JSON file over a code-owned typed
vocabulary, loaded strictly, with the population-equilibrium invariant checked before any load
and a validation entry point that finds a broken profile without spending a stand window.

## Completion criteria
- A day-profile JSON file lives in the repository carrying every number of the specification's
  proposal — the step mix, the hot-set skew, the think-time bounds, the ramp, the steady window,
  the intensities and the population shares — and no such number remains a literal in code.
- The profile vocabulary is typed code: what a profile may state is fixed by the types, and a
  profile can scale, weight and parameterize but never define behaviour (DR-2's behaviour
  boundary).
- The closed scenario-name registry exists and the loader refuses a profile naming a scenario
  outside it. The factories behind the names are the next session's work; the closed list is
  this one's.
- Loading is strict: an unknown key, a missing key, a scenario name outside the registry and a
  violated invariant each refuse the run loudly before any load is applied; no profile key has
  a default on the capture path.
- The equilibrium invariant of the specification — creations minus settlements minus deletions
  over the window, small against the seeded volume — is checked against the parsed profile,
  not trusted.
- A validation entry point parses and checks a profile file without applying load.
- `jackson-databind` is declared explicitly in `build.gradle` (approved this session; already
  on the runtime classpath transitively through the DSL).
- `./gradlew build` passes from the command line; the loader and the invariants are unit-tested
  against profile files, with no running stack.
- DR-2 moves `Draft` → `Accepted`, with the parser choice recorded.

## Scope boundaries
This session does not build the step kit of DR-3, does not turn the transport walk into
scenario classes and does not register factories behind the scenario names, does not wire the
registries or the transport context into scheduled scenarios (all of that is the second LR-3
session), does not run against the stack, and does not send the LR-4 answer.

## Status
`completed`

## Result
Every criterion is met.

- The profile vocabulary is typed code in `loadrig.model.profile`: the profile root, the step
  mix of the specification's seven step kinds, the think-time bounds, the hot-set skew, the
  seeded volume and the population of every scenario, each validated so that a profile object
  that exists is a profile the run may trust. The closed scenario-name list exists (worker,
  manager, administrator, discussion); the factories behind the names are the next session's.
- `profiles/day.json` carries every number of the specification's day proposal; no mix, skew,
  think-time or window number remains in code.
- The loader is strict over jackson: an unknown key, a missing key (named by its path), a
  scenario name outside the closed list and a value outside its range each refuse the profile
  loudly; no key has a default.
- The equilibrium invariant is checked against computed intensities, with the refusal stating
  the numbers and the consequence. The day profile drifts by about fourteen rows per
  ten-minute window against a seeded volume of fifty thousand — the population holds.
- `./gradlew validateProfile` parses and checks a profile without applying load, prints the
  implied intensities, and refuses with a non-zero exit; verified against the day profile and
  against a deliberately drifting copy.
- DR-2 is Accepted, with the parser settlement recorded in the record.

Verified from the command line: `./gradlew build` passes with the new loader and equilibrium
tests (no stack needed), and `./gradlew validateProfile` was run against both a holding and a
refused profile.

## Decisions
- **Intensities are computed, never stated.** The creations, settlements, deletions and notes
  per minute follow from the mix, the users and the think time; a profile stating them too
  would be two sources of one fact, and the drift between them is invisible in a capture. The
  validation command prints the computed numbers; they are what the LR-4 answer will quote.
- **The equilibrium constants are code, not profile keys.** The transitions-per-settlement
  figure (about two and a half, the specification's own weighing of the transition table's
  paths) and the drift tolerance (a tenth of a percent of the seeded volume, the
  specification's own yardstick) live beside the check. A profile that could relax its own
  invariant would not have an invariant.
- **The seeded volume is a profile key.** The equilibrium is judged against it, so the profile
  must state it; fifty thousand is this project's provisional reading of the specification's
  "tens of thousands" until the SUT's seeding item decides, and the LR-4/LR-5 conversation
  carries it.
- **Every scenario's population is stated, zero explicitly.** A scenario a profile leaves out
  is a wiring mistake, not an empty group; the populations must sum to the virtual users. The
  day split — five workers, three managers, one administrator, one discussion user — is this
  project's proposal; whether one discussion user orchestrating several registry sessions
  produces the twelve-percent share is exactly the mix-survival question the next session
  confronts.
- **`jackson-databind` is declared explicitly at the DSL's own version** (approved this
  session): nothing new on the classpath, but the loader stops depending on a transitive
  accident of the DSL's next release.
- **The validation output is pinned to English**, the injector's own language, for the same
  reason the smoke run pins it: a number that reads differently on two hosts is one more
  difference between two captures that nobody wrote down.

## Open questions
- Whether the day populations and the one-discussion-user proposal survive the wiring of the
  scenarios is the next session's question, and the answer feeds LR-4's mix-survival answer.
- The seeded volume of fifty thousand is provisional until the SUT project's seeding item
  chooses its number against the buffer pool; when it lands, the profile file changes, not the
  code.
- The morning and evening profiles stay unauthored on purpose: the specification states them
  as a direction, and pinning them before the day profile has run would be inventing numbers
  on top of numbers (they are LR-6 work).

## Next steps
1. The second LR-3 session: the step kit of DR-3 out of the transport walk's private steps,
   the scenario classes behind the closed names, the registries and the transport context
   wired into scheduled thread groups driven by the loaded profile.
2. Then the LR-4 answer through the exchange desk, quoting the computed intensities and the
   day profile.

## References
- Branch: `feature/lr3-profile-vocabulary`.
- Decision records: DR-2, implemented and moved to Accepted by this session.
