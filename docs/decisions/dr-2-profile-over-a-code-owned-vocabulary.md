# DR 2 — The load profile: external data over a code-owned vocabulary

**Status:** Draft

## Context

The brief already binds this project to explicit, versioned configuration for the intensities
and the skew, and to a load-profile description authored per variant as a capture artifact. What
it does not decide is where the boundary runs between the code and the profile: what a profile
may state, and what must stay code.

The pressures, by role:

- A profile changes per run and per variant; the code does not. A campaign must switch profiles
  on the capture host without a rebuild.
- Two scenarios may differ only in their parameters. Duplicating a scenario per parameter set
  multiplies code that must stay identical.
- The triage reads a capture against a baseline capture. A profile that was silently misread —
  a typo absorbed by a default — produces a false capture that poisons the datasets. That is
  worse than a crashed run, because nothing announces it.
- The load-profile description states what the run was meant to apply. If the description and
  the applied profile have separate sources, they can drift apart, and no reader of the capture
  can tell.

## Alternatives

**1. Profiles in code.** A class of named constants per variant, chosen by name at start.
Typed, refactorable, a wrong value fails at compilation, and the unit tests read the same
constants the run does. Cost: a profile change is a rebuild on the capture host; the profile
cannot travel into the capture as the file it was; and it contradicts the obligation the brief
already carries.

**2. An external declarative profile over a code-owned vocabulary.** The code defines the
scenarios, the steps and the invariants; the profile states the numbers and the composition:
the mix, the intensities, the ramp, the think times, the skew, the population of each group and
the parameter values of parameterized scenarios. The profile refers to scenarios by name out of
a closed registry the code owns. Cost: a parsing and validation layer, and errors that surface
at run start instead of at compilation.

**3. An external profile that defines the scenarios themselves.** The most flexible on paper.
Cost: it re-creates the problem of the JMX file, which this project rejected at its founding —
the logic drifts out of the code, out of the type system, out of the unit tests and out of
review. The load model stops being code.

## Decision

Alternative 2, under three binding rules.

**The behaviour boundary.** A profile may scale, weight and parameterize what the code defines;
it may never define behaviour. The moment a profile wants a branch, the branch is a scenario
and is born in code. This rule is the wall between alternative 2 and alternative 3, and the
drift from one to the other is this decision's named failure mode.

**Strict validation, no silent defaults.** An unknown key, a missing key, a scenario name
outside the registry, a violated invariant — each refuses the run loudly before any load is
applied. For a capture run no profile key has a default; defaults exist only for the smoke
entry point. The population-equilibrium invariant of the specification is checked against the
parsed profile before the run, not trusted. A validation command parses and checks a profile
without applying load, so a broken profile is found before a stand window is spent on it.

**The profile on the input is the description in the capture.** The load-profile description
artifact is the profile file the run was given, carried into the capture as received. One
source: the drift between what was said to be applied and what was applied becomes impossible
by construction.

## Consequences

- The code grows a closed scenario registry — names to factories — and the factory of a
  parameterized scenario takes a typed parameter object. DR-3 registers its generated scenarios
  into the same registry.
- The profile is JSON, the format the brief already fixes for the description artifact. The JDK
  parses no JSON; whether the DSL already carries a parser transitively or a dependency is
  added is settled with dependency approval when LR-3 implements this record.
- The loader and the invariants are unit-tested against profile files, without a stack.
- Intensity and mix numbers stop appearing in code entirely. A number from an exchange
  specification lives in a profile file, and the documents name it by role.
- Errors move from compilation to run start. The validation command is the compensation, and
  running it is part of preparing a campaign.
