# Brief: what the load rig is for

## Who is asking and why

Two customers, one chain. The **workflow-engine project** owns the `load-test-triage` scenario:
a fully automatic triage of a completed load-test run, analyzed from captured files. Its datasets
are real captures of a system under test running under a real load rig, with defect conditions
induced for real. The **SUT project** (`todo-webapp`) provides that system as an observed Docker
stack — the application, its MySQL database, Prometheus with three exporters — and has handed
this project two specifications through its exchange desk: the host and core map, and the
business scenarios with the load profile and service levels.

This project is the rig: it drives the stack with a business-shaped load and produces the
injector side of every capture. It also induces the injector-side defect variants of the
scenario's dataset map. Load generation is the whole scope: fault surfaces inside the SUT, the
capture extraction and the triage logic all belong to the other two projects.

## The artifacts a run leaves

- **The load-profile description** — what the run was meant to apply: operations, target rates,
  ramp, and the version of the SUT under test, read from the stand's information endpoint once
  before the window opens — outside the task surface and outside the result log, so that asking
  costs the capture no sample — and recorded as unknown only when the stand does not answer;
  authored per variant, JSON, written before the load starts so a dying injector still leaves
  it.
- **The JTL result log** — the canonical injector export, fixed by the workflow-engine decision
  of 2026-08-28: per-sample timestamped records from which achieved intensity over time, a flood
  of one operation and the moment the injector's own data stops are all computable. The
  timestamp semantics (`sampleresult.timestamp.start`) is set explicitly in the rig's
  configuration, never left to a default.
- **The run report** — what the run realized against what it intended: the sample and error
  counts and the starvation ledger's skip counts by step, so a realized mix that drifted from
  the profile names where. The harness's console statement, kept beside the capture for
  analysis.
- No live push of raw samples into a time-series store. The known pathology — unique
  nano-second timestamps killing the store's aggregation model, and spikes attributed to their
  completion time — is avoided by construction: raw samples go to the durable file only, and any
  future live channel carries client-side aggregates. A time-series stand is a deferred idea
  tied to a possible cloud deployment, recorded on the workflow-engine side.

## Binding facts received from the SUT project

Stated here by role; the numbers live in the exchange documents and become this project's
configuration, never implicit constants.

- **The core map.** The generator gets one pinned physical core pair and an explicit memory
  limit on the capture host; it must never take the system's cores or the collectors' cores. The
  calibration rule — the throttled-period counter flat over the window for the application and
  the database — decides whether a run counts at all.
- **The load model.** A clean baseline of ten virtual users with think times drawn per step; the
  ceiling of twenty is a hypothesis to be tested by a stepped run, not a measured maximum.
- **The scenarios.** Three walked role scenarios — worker, manager, administrator — and the
  assembled discussion scenario that converges several sessions on one hot task. The addresses,
  the session and CSRF mechanics and the one-answer-shape-per-capture rule are in the
  specification's appendix; a script can be written from it without reading the SUT's code.
- **The profile discipline.** The mix, the population equilibrium (creations balanced by
  settlements), the hot-set skew and the think times are part of the model; the four false
  findings a break-it profile would produce are listed in the specification and each one is a
  named anti-requirement of this project.
- **The refusal codes.** The task surface answers exactly five, and the list is closed: a request
  the surface never produced, an action the actor does not own, a name that matches nothing, an
  action the current status does not offer, and a required field left blank. A triage that
  classifies refused samples by their code may hold the list as exhaustive. Two of them bind this
  project further. The refusal of an unowned action and the refusal of a mutating request that
  carries no valid token wear the same code and are not distinguishable by it; only the rig knows
  which of its requests carried a token, so splitting the two is this project's obligation and
  belongs in its own ledger. And the first code answers only a request no screen produces — the
  scenarios follow the screens, so it can never be user behaviour in a clean capture, and its
  appearance is a defect of the rig or a deliberately induced fault.
- **The initial state.** The accounts and the seeded tasks both belong to the SUT project's
  seeding item: two owners of the initial state are two states nobody reproduces. Once it lands
  this project provisions nothing, its provisioning walk retires, and the seeded accounts are not
  held at a password change. Until then the walk stands and must remain a no-op against a seeded
  stand. The pool's composition is this project's statement and that item's input, settled through
  the desk before it runs. There is no manifest of task identities and there will be none — no
  user of the application holds one — so the rig learns the stand from the list page ordered by
  due date, which is the intended path and not a workaround.
- **The service levels.** Chosen, not measured, and a calibration lever: fixed before a
  campaign, recorded with every capture, held across the baseline and the faulted run alike.

## What this project must answer back

The SUT specification closes with five questions; answering them is roadmap work here, and the
answers go back through the exchange desk:

1. How many accounts the profile needs, and how many of them are managers.
2. Whether the proposed mix survives contact with a real script, and what it becomes if not.
3. How the rig converges several sessions on one task, so the discussion is a discussion.
4. The intensities and the skew as the rig's own explicit, versioned configuration.
5. The first measured baseline against the service levels — where the real percentiles land.

The first four were answered through the desk on 2026-08-29: the account pool and the mix-survival
check encode answers one and two, the task registry's hot set is answer three, and the profile
file is answer four. The question asked back — whether the seeded history can pre-register hot
tasks — was answered the same day: it cannot, and the rig reads the stand from the list instead.
The fifth is owed after the first calibrated run.

## The injector-side defect variants

Three of the scenario's dataset variants are induced here, not in the SUT:

- **The flooding thread group** — a lost think time floods one operation at the maximal rate;
  the injector's own data shows the flood while the service logs stay clean.
- **The starved injector** — the generator short of capacity: the system is healthy but the
  achieved intensity sinks below the target.
- **The injector dying mid-run** — a capped injector heap or a kill: the JTL records stop at a
  moment the service logs pass in silence.

Each is applied deliberately, per variant, and never leaks into a clean capture.

## Open points of the host agreement

Carried from the exchange documents; settled as the work meets them:

- Whether the allotted core pair and memory carry the intended profile at twenty virtual users.
- Where the generator writes its artifacts during a run, given that the database's disk work is
  part of the evidence.
- Whether the generator itself must be observed, and which project owns that collector.
- The ownership of the starved-host fault mechanism: proposed to live in this project, beside
  the injector, to be confirmed with the SUT project.
