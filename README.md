# load-rig

The load rig of a three-project ecosystem: it drives the system under test — a task-tracker web
application observed as a Docker stack — with a business-shaped load, and produces the
injector-side artifacts of a capture: the load-profile description and the JTL result log. The
captures feed the `load-test-triage` scenario of the workflow-engine project, which analyzes a
completed run and delivers a triage verdict with confirmed evidence.

What this project is for, the facts it received from the SUT project and the questions it owes
back are stated in [`docs/brief.md`](docs/brief.md). The ordered work is in
[`docs/roadmap.md`](docs/roadmap.md).

## Stack

- **JMeter 5.6.x driven as code** through [jmeter-java-dsl](https://github.com/abstracta/jmeter-java-dsl):
  the load model is Java, reviewed and versioned; no JMX files exist in this repository.
- **Java 21** (Gradle toolchain), **Gradle** with the frozen wrapper.
- Coordination between virtual users — the session registry and the task registry — is plain Java
  inside the injector JVM. Deliberately no external store and no time-series database: the capture
  host carries nothing beyond what the core map allots to the generator.

## Build and test

```bash
./gradlew build          # compile and run the unit tests
./gradlew test           # tests only
```

The unit tests need no running SUT.

Neither does validating a load profile: it parses the file, checks the population-equilibrium
invariant and prints the intensities the profile implies, without applying any load. A broken
profile is found before a stand window is spent on it.

```bash
./gradlew validateProfile                            # the repository's day profile
./gradlew validateProfile -Dloadrig.profile=<file>   # any profile file
```

The transport smoke run does: it drives a running stack with a few virtual users, walks every
element of the transport once and leaves a JTL result log under `results/`, outside version
control.

```bash
./gradlew run                                    # the defaults: a stack on the capture host
./gradlew run -Dloadrig.smoke.virtualUsers=4     # a run states its parameters on the command line
```

It is not a capture and carries no load profile: it proves that the rig drives the application.
The run harness and the capture artifacts are later work items (`docs/roadmap.md`).

Provisioning the account pool also needs a running stack: it brings the stack to the configured
pool through the administrator screens, walks each fresh account's forced password change and
proves every member's sign-in. It is idempotent - a stack already provisioned is only proven.

```bash
./gradlew provisionPool                          # the defaults: a stack on the capture host
```

## Layout

| Path | What it holds |
| --- | --- |
| `src/main/java/loadrig/model` | The business scenarios, the step mix, the profile vocabulary |
| `profiles/` | The load profiles: external JSON over the code-owned vocabulary, one file per variant |
| `src/main/java/loadrig/registry` | The session registry and the task registry shared by the virtual users |
| `src/main/java/loadrig/run` | The run harness: assembling a profile, executing it, leaving the artifacts |
| `docs/` | Rules, brief, roadmap, decisions, sessions — see `docs/README.md` |
| `exchange/` | The correspondence desk with the other projects; deliberately outside git |

## Constraints the rig must honour

They come from the SUT project's specifications and are binding; the brief carries the details.

- The generator runs on the cores the core map gives it and takes neither the system's cores nor
  the collectors' cores.
- The load is shaped like the business, never like an attack: the mix, the skew and the think
  times are part of the model, and the population stays in equilibrium inside a capture window.
- The operational endpoints of the stack are never driven.
- The JTL result log is the canonical injector export of a capture; live push of raw samples into
  a time-series store is deliberately absent.
