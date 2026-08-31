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

Neither does validating a load profile: it parses the file, prints the rates the profile implies
and the census a warm start would have to establish before the window, bucket by bucket, and
refuses a profile whose population no warm start could bring about — all without applying any
load. It reads the campaign in the same breath - the depth its seats rotate through and the
service levels its captures are judged by - and prints both, so a broken profile and a broken
campaign are found before a stand window is spent on either.

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

A profile run is a capture's injector side: it loads the stated profile file, holds the
invariants, assembles the plan over the account pool, brings the stand to the census the profile
needs and drives the stack. The warm start comes before the window and outside the test plan: it
reads what the stand already holds from the list page ordered by due date and creates only the
difference, so the mix holds from the first minute and no sample of the capture is spent
establishing the population. Each run lays its artifacts into a directory of its own under the
results root, named by the profile and the run's UTC stamp: `load-profile.json` — what the run was
meant to apply, with the computed target rates, the census, the measured volume of the stand, the
rotation that played the window, the service levels it is judged by and the SUT version, written
before the load starts; `samples.jtl`
— the result log; `run-report.txt` — what the run realized, with the starvation ledger and the
wait each service-level band realized against its figure. A run with refused samples exits
non-zero and leaves its artifacts in place; a run that breaches a service level does not, because
a breach is a finding about the system and the injector-side defect variants are meant to produce
one.

The warm start acts as the accounts the run signs in with, so the pool is provisioned first.

```bash
./gradlew runProfile                                 # the repository's day profile
./gradlew runProfile -Dloadrig.profile=<file>        # any profile file
./gradlew runProfile -Dloadrig.campaign=<file>       # the campaign; the repository's otherwise
./gradlew runProfile -Dloadrig.sut.version=<version> # record what is under test; unknown otherwise
```

A campaign is what holds across the runs a capture is read against, as opposed to a profile, which
is what one run applies: `profiles/campaign.json` states it, and every run of the campaign reads
that one file.

It states the depth of the rotation. A run seats as many sessions as its populations state, but a
session ends at a share the profile states and the account that signs in next is chosen by what
the rig knows about it, so over a window the seats are played by more people than sit in them at
once. How many more is the campaign's number — the instrument that decides how wide the working
set is, and therefore how little of it a cache can hold. The accounts a run plays are the first
accounts of each role in the pool, as many as the seats multiplied by that depth; the census
stocks all of them, every step that names a person draws from them, and the run's session registry
admits nobody else. The rotation travels in the capture's description, because two windows of one
intensity over rotations of different depths are not comparable and nothing in the result log
would say which was which.

It states the service levels too: the criterion a capture is judged by and one of the four
calibration levers of a demonstration stand, chosen rather than measured, held across the baseline
and the faulted run alike, and recorded with every capture. The file states a 95th percentile per
band of act and a hard ceiling over everything, and a run states, band by band, what it realized
against them. Most bands are the SUT specification's own rows; a band for an act
the load model performs which that table does not name is this project's own statement, derived
the way a load analyst derives one and sent to the SUT project as a fact of the campaign rather
than as a question. Signing out is the first of them. That verdict is the injector's half: the application's log staying
quiet and the throttled-period counter staying flat are read on the stand, and the report says so
rather than implying a verdict the rig cannot reach.

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
| `profiles/` | The load profiles, one file per variant, and the campaign a run belongs to — its rotation and its service levels: external JSON over the code-owned vocabulary |
| `src/main/java/loadrig/registry` | The session registry and the task registry shared by the virtual users |
| `src/main/java/loadrig/run` | The run harness: assembling a profile, executing it, leaving the artifacts |
| `docs/` | Rules, brief, roadmap, decisions, sessions — see `docs/README.md` |
| `exchange/` | The correspondence desk with the other projects; deliberately outside git |

## Constraints the rig must honour

They come from the SUT project's specifications and are binding; the brief carries the details.

- The generator runs on the cores the core map gives it and takes neither the system's cores nor
  the collectors' cores.
- The load is shaped like the business, never like an attack: the mix, the skew and the think
  times are part of the model, and the population of work the steps act on holds inside a capture
  window — established before it opens rather than waited for.
- The operational endpoints of the stack are never driven.
- The JTL result log is the canonical injector export of a capture; live push of raw samples into
  a time-series store is deliberately absent.
