# load-rig

**A virtual user is not a user of the system.** In a typical JMeter script a thread *is* a person:
as many threads, as many identities, and the state of the application drifts as the run goes on.
This rig keeps the two apart on three levels.

- **Accounts** ([LR-9](docs/roadmap.md)). The account pool is explicit configuration derived from
  the scenarios — how many accounts, and how many of them managers, because a discussion needs a
  manager who is not the author of the task. It is provisioned through the administrator screens,
  each fresh account walked through its forced password change and every member proven by a
  sign-in. Provisioning is idempotent. A run signs in with a stated part of the pool: the seats its
  scenarios hold at once, multiplied by the depth of rotation the campaign states.
- **Sessions and tasks** ([LR-2](docs/roadmap.md)). Application sessions are decoupled from virtual
  users and picked by state; a session also ends, at a share the profile states, and the account
  that signs in next is chosen by what the registries hold about it. The task registry mirrors the
  rows of the application's transition table that the scenarios drive, so a step finds a task it is
  allowed to act on. Both registries are unit-tested with no stand running, the task registry
  against that table: the load model is testable on its own.
- **Population** ([LR-10](docs/roadmap.md)). The population a profile needs is computed per bucket
  from the scenarios' weights and established *before* the window: the warm start reads what the
  stand already holds and creates only the difference. That is stand administration, not load, so
  the result log holds the window alone — no sample of a capture is spent building the population.

The thesis is the one the
[system under test](https://github.com/VlSobProjects/spring-sut-task-tracker) is built on: a result
is worth nothing until what it was obtained on is fixed. For a load rig that is the population and
the identities.

Two things a reviewer can check in minutes:

- **No JMX file in the repository.** The load model is Java through jmeter-java-dsl, reviewed in
  diffs and versioned like the rest of the code.
- **A profile is validated without load.** `./gradlew validateProfile` prints the rates a profile
  implies and the census a warm start would have to establish, and refuses a profile whose
  population no warm start could bring about — before a stand window is spent on it.

There are no response times and no capacity figures here on purpose: the calibration work in
[`docs/roadmap.md`](docs/roadmap.md) comes first.

## Where it sits

The load rig of a three-project ecosystem: it drives the [system under test](https://github.com/VlSobProjects/spring-sut-task-tracker) — a task-tracker web
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

The accounts it creates get one fixed password, `loadrig123!`, overridable with
`-Dloadrig.provisioning.password=<password>`; it exists only on the local stand the rig
provisions. The built-in accounts of a fresh database - the administrator the provisioning works
through, and all three in the transport smoke run - are signed in with the development defaults
the system under test seeds and publishes (`admin123!`, `manager123!`, `worker123!`). None of
them is a secret.

```bash
./gradlew provisionPool                          # the defaults: a stack on the capture host
```

## What a run leaves

Fragments of two of the three artifacts of one run: a five-minute window of a scratch profile with
the day profile's mix and populations. **The run is uncalibrated and its numbers are not a
baseline**: they show what the artifacts state, and nothing about the system under test. The
service levels are cut from both fragments for the same reason - no response time is published
before calibration.

What the run was meant to apply, written before the load starts - `load-profile.json`:

```jsonc
{
  "profile" : "rotation",
  "runStamp" : "20260831-231527",
  // ...
  "targetRatesPerMinute" : {
    "steps" : 120.0,
    "lookingAtAList" : 44.4,
    "openingOneTask" : 26.4,
    "movingATask" : 15.600000000000001,
    "discussion" : 14.399999999999999,
    "runningAReport" : 12.0,
    "creatingATask" : 6.0,
    "deletingATask" : 1.2,
    "settlements" : 6.0,
    "sessionsGivenUp" : 2.4
  },
  "population" : {
    "census" : 357,
    "censusBuckets" : 63,
    "tasksOnTheStandAtStart" : 1877,
    "readFromTheStand" : 500,
    "createdByTheWarmStart" : 241,
    "tableGrowthOverWindow" : 24.0
  },
  // ...
}
```

What it realized - `run-report.txt`:

```text
the run rotation-20260831-231527 is complete: profile "rotation", SUT version 1.0.0
  10 virtual users, ramp 30 s, steady window 5 min
  the rotation: 30 account(s) play, 3 per seat
    15 worker account(s) play 5 seat(s)
    12 manager account(s) play 4 seat(s)
    3 administrator account(s) play 1 seat(s)
  warm start: a census of 357 task(s) over 63 bucket(s); the stand held 1877 task(s), 500 were read and 241 created
  795 samples, 0 of them failed
  refusals: the application refused nothing
  ...
  starvation ledger:
    the manager approves finished work skipped 1 time(s)
  artifacts: load-profile.json, samples.jtl, run-report.txt
```

The ledger is what joins the two: where the realized mix fell short of the intended one, the
report names the step and the count instead of leaving the drift to be found in the log.

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

## Author

Vladimir Sobolev. Issues and questions: through this repository.
LinkedIn: https://www.linkedin.com/in/vladimir-sobolev-58290a233/
