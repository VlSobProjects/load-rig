# DR 4 — Registry concurrency: one monitor, valid at this rig's scale only

**Status:** Draft

## Context

The registries of LR-2 are the coordination state every virtual-user thread of one injector JVM
goes through: a step acquires a session and leases a task before it acts. Their acquiring
operations are compound picks — scan the entries, take the first fit — under invariants whose
silent violation corrupts a capture: one holder per session, one mover per task, a stale lease
refused. A race here produces refusals the rig itself caused, which rule 7 of the implementation
rules exists to exclude.

Lock contention in a load generator is not an ordinary inefficiency. Threads queuing on a
coordination lock add an invisible component to every think time and sink the achieved intensity
below the target — which is exactly the signature of the starved-injector defect variant, read
into a capture without having been induced. So the concurrency design of the registries is part
of the load model, and its boundary of validity has to be written down, not assumed.

The scale this rig is specified for: a ceiling of twenty virtual users, think times of three to
seven seconds drawn per step, roughly two to four requests per second across the whole rig, a
pool of twenty-five accounts. A registry critical section is a scan of tens of in-memory entries
— microseconds — so the monitor is uncontended at several orders of magnitude below its
capacity.

## Alternatives

**1. One monitor per registry.** Every method synchronized on the instance; every compound pick
and every two-structure invariant is trivially atomic, and correctness is reviewable in one
file. Cost: all virtual-user threads serialize on one lock per registry. This is sound only
while the critical section stays microseconds and the request rate stays far below the lock's
capacity — it is a scale-scoped choice, not a universal pattern.

**2. Per-session atomic state, starvation onto queues.** Each session carries its own atomic
in-use state; a pick iterates and flips the first free entry by compare-and-swap, so threads
never gather on a global lock. Starvation stops being an exception and becomes a queue, and the
queue depth is exported as a metric — the direct measure of coordination pressure. This is the
proven pattern for corporate-scale generators with large concurrent virtual-user populations
asking for sessions at high rates. Cost: a retry protocol whose invariants are spread across the
code instead of held by one monitor — here across the role map, the state map and the lease
checks — harder to prove in review and by unit test; and a queueing policy the profile must own,
because a queue hides the pressure a loud failure would announce unless its depth is watched.

**3. A global ReentrantLock.** The monitor's semantics with tryLock, fairness and conditions
available. Buys nothing today: the registries deliberately do not wait — starvation is a loud
exception, and the waiting policy belongs to the profile — so there is no use for conditions or
timed acquisition until such a policy exists.

## Decision

Alternative 1, with its boundary stated as part of the decision. The registries of this rig run
behind one monitor each because at this rig's specified scale the contention is negligible and
the provable invariants are worth more than an unmeasurable speed-up. This is explicitly not a
universal answer for load generation: at corporate scale — hundreds or thousands of concurrent
virtual users contending for sessions — threads gathering on these methods would distort the
load model directly, and the design of alternative 2 (atomic per-session state, queues for
scarcity, queue-depth metrics) is the known working answer there.

Named triggers that supersede this record:

- a waiting policy for scarce sessions or tasks appears (LR-3 or later): waiting means queues,
  and queues mean alternative 2's accompaniment — depth metrics — designed as a decision of its
  own, not patched into the monitor;
- the load model leaves its specified scale — virtual users beyond the specification's ceiling
  by orders of magnitude, or think times dropped, which the specification itself forbids as a
  false-finding profile;
- the injector moves to virtual threads while on Java 21, where a monitor pins its carrier
  thread; the pinning is resolved only in later JDKs.

## Consequences

- The registries stay plain synchronized Java, and their unit tests stay simple; no lock-free
  protocol has to be proven in review.
- Contention on a registry monitor observed in any run is a defect of the rig, never a property
  of the load: the model says it must not exist, so its appearance means the model's boundary
  was crossed.
- A future scaled-up rig does not extend these registries; it supersedes this record first and
  redesigns the coordination with per-session atomic state and measured queues.
