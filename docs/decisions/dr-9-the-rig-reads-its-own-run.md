# DR 9 — The rig reads its own run: a protocol made here, independent of the automated path

**Status:** Draft

## Context

A run of this rig currently answers half of the question it is asked. The result log states what
the injector saw — the samples, the bands, the realized percentiles — and the run report says in
its own words that the other half is not its to reach: the application's log staying quiet and the
throttled-period counter staying flat are read on the stand.

Twice now they have been read there by hand and sent back as correspondence. The SUT project's
answer of 2026-09-03 closed the baseline of 2026-08-31 that way, and disqualified the run of
2026-09-03 the same way. That letter says two further things that decide this record. It says the
project would rather have both halves read over the same window by coordination than reconstructed
after the fact. And it warns that its store holds the windows only until the next reset, which is a
retention this project does not control and cannot plan against.

So the first constraint is plain: **a load rig whose runs are declared admissible by the owners of
the system under test is not finished.** The judgement is the load test's own, and the instrument
that makes it belongs here.

The second constraint decides the shape rather than the existence of the work, and it is the
stronger one.

The workflow-engine project's `load-test-triage` scenario produces a fully automatic verdict over a
completed capture, through a deterministic workflow driven by an LLM. That path is under
construction and has to be debugged against something. The something is a protocol made in this
project — by hand and by instrument, the way a load analyst produces one on any load-testing
engagement. The two paths are meant to reach the same verdict from the same run, and **that
agreement is evidence only if the two are independent.** The automatic path knows nothing of this
project's readings, and must continue to know nothing of them.

A third piece of context is small and belongs here because it is the failure this record prevents.
On 2026-09-03 this project read the application's CPU over a window, compared it against a quota of
two cores, and reported that the application had saturated. The quota is four. The reading was ad
hoc, the limit was assumed rather than read, and a conclusion was published from it inside the day.
An instrument that states the limit beside the figure does not make that mistake.

**What the documents say today.** `docs/brief.md` states that load generation is the whole scope
and that the capture extraction belongs to the other two projects. That sentence was written when
the only reader of the stack's metrics was the automatic path, and it is amended by this record —
narrowly, and in a direction this record has to be careful about, which the alternatives below
state.

## Alternatives

**1. Keep asking the SUT project, run by run.**

What happens today, and it works: the answers have been accurate, prompt and more generous than
asked. The cost is not accuracy, it is structure. Every run's admissibility waits on a letter, so a
campaign of several steps waits on several. The evidence lives in a store this project neither owns
nor can size. And the owner of the system under test becomes the judge of whether the load test
counts — an inversion that no load-testing engagement accepts, however good the relations are.

There is a real thing lost by leaving it: their reading is an outside check, and two parties reading
the same window independently is worth something. That is recovered as coordination rather than as
dependency — both sides read the same window, and disagreement is a finding.

**2. Take the readings from the workflow-engine's triage, which already reads that API.**

It removes the duplication: one instrument, one set of numbers, no second client of Prometheus, and
the boundary in the brief stays as written. It is the cheapest of the three by a wide margin.

It is rejected for the reason the whole protocol exists. The automatic path is the thing being
debugged; the protocol is what it is debugged against. Sharing the instrument makes the reference
and the subject the same object, and an agreement between them stops being evidence of anything.
This is not a practical objection that a careful implementation could answer — it is the purpose of
the exercise, and this alternative destroys it.

**3. The rig reads the stack itself and assembles a protocol beside the capture.** Chosen; stated
below.

Its cost is real and is not hidden. This project becomes a client of two APIs it does not own,
under a stack whose compose file it does not write. Both can change under it, and both are one more
thing that must be reachable from wherever the generator runs. It roughly doubles the surface of a
project that until now did one thing.

## Decision

The rig reads the system side of its own run and assembles a protocol from it. Seven parts.

**1. What is read is a closed, stated list.** Both halves of the SUT's calibration rule — the
throttled-period counters of the application and the database, and the application's log over the
window. Then the resources against their stated limits: processor use per container against its
quota, memory against its limit. Then the connection pool: active, idle and waiting. Then the
request rate the stack actually saw. Closed, because a protocol whose contents vary from run to run
cannot be compared across the runs of a campaign, and comparison is what a campaign is for.

**2. Readings are taken over the measured window, not over the run.** The SUT's letter of
2026-09-03 established why, by looking: over the whole of 2026-08-31 the throttled-period counter
rose above zero in exactly two minutes, and both were the first minute of a run — 1.12 cores against
a quota of four with 1.1 % of periods throttled, a sub-second burst of class loading and pool
filling rather than saturation. A step discarded on the calibration rule for a warm-up burst would
be discarded for the wrong reason, and the stepped search places many windows.

**3. Every figure states the limit it is read against.** A processor figure without its quota, a
memory figure without its ceiling, a pool figure without its size are not readings. This is the
narrowest rule in this record and it is here because this project has already published a wrong
conclusion for want of it.

**4. The application's log is read here too, so that the verdict is whole.** The path inside the
container and the service token in each line are a documented interface, fixed by the SUT's rename
notice of 2026-09-03; reading them is not reaching into the system's internals. Without this half
the protocol would still end by asking someone else a question, which is what this record exists to
stop.

**5. The protocol lives beside the capture and never inside it.** The capture stays exactly what it
is — the result log, the load-profile description, the run report — because that is what the
automatic path consumes. The protocol, its readings and its rendered panels go in a directory of
their own. This is the independence rule made structural rather than remembered: a consumer that
takes the capture whole cannot take this project's conclusions with it by accident.

**6. The protocol is not the run report.** The report is the injector's own statement about what it
realized and stays part of the capture. The protocol assembles the report, the readings and the
panels into the document a load-testing project hands over.

**7. The dashboard's `uid` and its panel numbers are configuration, held as the contract the SUT
project states them to be.** They are never literals in code, and a panel number is never reused
for a different panel — the same standing this project gives the core map.

## Consequences

**A run that leaves no protocol is not a finished run.** The obligation is the rig's, and a run
whose protocol could not be assembled says so.

**The protocol degrades, it never fails the run.** The load result stands on the result log alone.
A reading that could not be taken — a collector down, an API that moved, a renderer absent — is
recorded as not taken, with the reason. Losing a stand window because a graph could not be fetched
would be the worst trade this project could make.

**Nothing this project computes is offered to the workflow-engine as input.** This is a standing
obligation and the easiest one to break later by convenience, because at some point handing over a
ready reading will look like a kindness. It is not: it would retire the only independent check the
ecosystem has.

**The rig needs network reach to Prometheus, to Grafana and to the application's container.** That
is one more thing the generator's own packaging has to arrange, and it is a reason the packaging
comes first among the work items this record opens.

**The outside check becomes coordination rather than dependency.** The SUT project is asked for the
same window when it matters, and a disagreement between two independent readings of one window is a
finding worth having — which is what its own letter proposed.

**This record does not decide where the generator runs.** Containerizing it, pinning it to the cores
the map allots and closing the host are conditions of a valid run rather than choices about the
rig's shape; they are roadmap work under this record, and the reasoning for preferring a declared
`cpuset` over a hand-set affinity mask is stated in the item that does it.
