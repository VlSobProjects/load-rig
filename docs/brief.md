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
  ramp, the population under the load — the census the profile needed, and the number of tasks the
  stand was measured to hold before the window — the service levels the run is judged by, as the
  campaign fixed them, because a capture judged against figures nobody wrote down cannot be re-read
  — and the version of the SUT under test, read from the stand's information endpoint once before
  the window opens — outside the task surface and outside the result log, so that asking costs the
  capture no sample — and recorded as unknown only when the stand does not answer; authored per
  variant, JSON, written before the load starts so a dying injector still leaves it.
- **The JTL result log** — the canonical injector export, fixed by the workflow-engine decision
  of 2026-08-28: per-sample timestamped records from which achieved intensity over time, a flood
  of one operation and the moment the injector's own data stops are all computable. The
  timestamp semantics (`sampleresult.timestamp.start`) is set explicitly in the rig's
  configuration, never left to a default.
- **The run report** — what the run realized against what it intended: the sample and error
  counts, the starvation ledger's skip counts by step, so a realized mix that drifted from
  the profile names where, and the wait each band of the service levels realized against the
  figure it was held to. The harness's console statement, kept beside the capture for
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
  named anti-requirement of this project. The equilibrium is a property of the working set — the
  tasks the steps may act on — and the rig holds it by establishing that population before the
  window, stocking each bucket for the drain the window causes, and keeping the run's work among
  the accounts the run plays over its window rather than the whole pool — a session ends and
  another begins, at a share of the iterations the profile states and with the account that signs
  in next chosen from the rig's own registry by what it holds, so those accounts are more than the
  seats a step holds at once, and an account between sessions is waiting its turn rather than
  absent (DR-8). What the window adds to the table
  itself is published as a fact of the capture; how much the stand holds when the window opens is
  the campaign's own stated figure, which the warm start levels to in both directions, so that the
  steps of one campaign differ in intensity alone.
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
  due date, which is the intended path and not a workaround. Until the seeding lands, a run brings
  about the population its own profile needs: the census is computed per bucket and a warm start
  reads what the stand holds and creates the difference, before the window and outside the result
  log (DR-6). It degenerates to pure reading on a seeded stand, so the seeding costs it no
  rewrite.
- **The service levels.** Chosen, not measured, and a calibration lever: fixed before a
  campaign, recorded with every capture, held across the baseline and the faulted run alike. Their
  bands are mostly the specification's rows and are not confined to them: the load model is this
  project's, and an act the model performs which that table does not name still needs a figure, or
  the run either leaves it unjudged or hides it inside a band measuring something else. Such a
  band is stated here — the way a load analyst states one, from how the act is used rather than
  from what the system's authors would price it at — and travels to the SUT project through the
  desk as a fact of the campaign rather than as a question. Signing out is the first of them. They
  are therefore the configuration of a campaign and not of a profile — one file every run of it
  reads — and each run times every sample the result log carries into the band of the act that
  produced it and states the percentile it realized against the figure, by a named percentile
  method. A breach is a finding published with the capture and never a failure of the run: the
  injector-side variants are meant to breach. And it is half a verdict by construction — the
  viability criteria are these levels together with the application's log staying quiet and the
  throttled-period counter staying flat, and those two are read on the stand, because the rig
  drives no operational endpoint. A campaign that searches for the maximum is judged by figures of
  its own, derived as one multiple of what the same stand answered when it was quiet, because the
  chosen figures are met by orders of magnitude and a search judged by them would terminate past
  the knee (DR-7); which campaign a capture belongs to is therefore part of what it means, and the
  description carries the figures it was judged by.

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
The fifth went back on 2026-08-31, measured rather than estimated: at ten virtual users every band
is met by between fourteen and seventy times, so the assumed ceiling of twenty is answered as no
maximum at all. Three things travel back with it — the half of the verdict the rig cannot read and
asks the other project to close over the same window, and the two questions below.

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

- Whether the allotted core pair and memory carry the intended profile at twenty virtual users. At
  ten they are nowhere near a limit; the stepped search takes the generator well past twenty, and a
  step whose achieved intensity fell short of its target is discarded rather than judged, so the
  answer arrives as a discarded step rather than as a quiet distortion.
- Whether the application's core set may be narrowed for a later campaign — asked 2026-08-31, and
  asked as an option rather than a request. Only the set may move: a quota that binds raises the
  throttled-period counter, which the calibration rule reads as a capture made of the limit.
- What volume the report will scan once the seeding item chooses one, and whether the item that
  reduces the report to an indexed query lands before or after it. The report's headroom is the
  one measured figure that is a statement about the database rather than about the application.
- Where the generator writes its artifacts during a run, given that the database's disk work is
  part of the evidence.
- Whether the generator itself must be observed, and which project owns that collector.
- The ownership of the starved-host fault mechanism: proposed to live in this project, beside
  the injector, to be confirmed with the SUT project.
