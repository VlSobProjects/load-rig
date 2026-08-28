# DR 3 — The rig as a generation target

**Status:** Draft

## Context

Actualization — keeping a load script true to a system that keeps changing — is a standing cost
of load testing, and on large systems one of the largest: endpoints move, request and answer
formats change, scenarios are added and reshaped with every release of the system under test.
Full automation is out of reach when the business logic itself changes, but a large fraction of
the routine drift can be absorbed automatically.

A later stage of this project's chain builds exactly that: an actualization scenario on the
workflow-engine, a pipeline that regenerates the affected parts of the rig when the SUT changes,
with an LLM writing the change and deterministic gates deciding whether it holds. That scenario
is not built here and not now. What is decided now is the rig's side of the contract, because
every architectural decision from this point on either keeps that door open or closes it, and
retrofitting the contract later means rewriting the scenarios.

One economic fact frames the alternatives. Before the LLM, a code generator was a program
written and maintained per pattern, which is why file processing over unified script-assembly
components was usually the better bargain. With an LLM the generator is no longer written; what
is written is the rails that constrain the generated code and the gates that refuse it.

## Alternatives

**1. The generation target is data.** An intermediate representation of a scenario, interpreted
by the code; the pipeline emits the representation. Narrow and safe, because a schema validates
it. Cost: new logic is inexpressible. The hard fraction of actualization — the scenarios whose
logic actually changed — stays manual, and the representation itself drifts toward the external
DSL that DR-2 rejects.

**2. The generation target is scenario code, patterned by convention.** Java classes following
a documented shape, held by review and optionally by structural analysis. The full range of
logic is expressible. Cost: a convention holds until the first hard case; nothing refuses a
generated scenario that quietly steps around the pattern.

**3. The generation target is scenario code, patterned by types.** The same Java classes, but
assembled from a thin step kit of the rig whose signatures make the obligations non-optional: a
step cannot be built without its content assertion, its stable name and its extractions, and
the kit's inputs are the surface and the correlation dictionary, so a literal address has
nowhere to go. The compiler is the pattern check. Cost: a second layer over the DSL, with the
standing risk of growing into a DSL over a DSL.

## Decision

Alternative 3. The pattern is stated as constraints, and every later architectural decision
holds them:

- **The surface stays data.** The surface classes hold addresses, form fields and page marks as
  constants and trivial builders, never behaviour. The surface is the artifact a future
  pipeline diffs against a fresh observation of the SUT — a traffic dump, a documentation
  export — and a surface with logic in it cannot be diffed.
- **Correlation is enumerable.** The rules that read identities and tokens out of answers live
  in a named dictionary beside the surface, not inline in scenario logic, so that each rule can
  be verified — and regenerated — on its own.
- **A scenario is one class with a fixed skeleton, registered in a closed registry.** The same
  registry DR-2's profile refers to by name.
- **Steps are built only from the step kit.** Scenario code holds business order and business
  logic; everything a step owes — the assertion on the content of its answer, the stable name,
  the extractions, one result record per user-visible step — is taken by the kit's signatures,
  not remembered by the author.
- **The gates are the other half of the contract.** `./gradlew build` first: the compiler holds
  the pattern and the unit tests hold the registries and the invariants without a stack. The
  conformance walk against the running stack second: because every step asserts the content of
  its answer, a green walk states that the surface and the correlation are current. External
  verification third, outside the rig.
- **Stable identifiers face outward.** The step names in the result log, the run tag and the
  profile carried into the capture are a contract: they are what an external verifier joins on,
  and they do not change casually.
- **The rig never reads the SUT's database.** Verification across all sources — the result log,
  the database, the service logs — is an external function beside the actualization scenario,
  in the workflow-engine's domain. The rig owes it exactly the identifiers above, and nothing
  else.

## Consequences

- The step kit is born inside LR-2 and LR-3, as the refactoring those items force anyway; the
  private step methods of the transport walk are its prototype. It is not built speculatively
  ahead of them, and it earns no roadmap item of its own.
- The named failure mode of the kit is the DSL over the DSL. The boundary: the kit bundles what
  belongs to one step — sampler, assertion, extraction, name — and everything the underlying
  DSL already expresses well passes through untouched.
- Scenario code becomes more ceremonious: a quick one-off step must still state its name and
  its assertion. Accepted, because a step without an assertion is exactly how a false capture
  begins — the transport session already found three refusals hidden behind a rendered page
  and a 200.
- Structural checks by an architecture-test library are an optional second tier behind the
  types; adding one is a dependency and needs the usual approval.
- What the actualization scenario will need from the other projects — traffic observed by the
  SUT's own tests, a documentation export of the surface — is exchange-desk correspondence for
  the stage that builds it, not this record's subject.
