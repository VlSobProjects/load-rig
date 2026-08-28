# DR 1 — One answer shape per capture: the fragment mode

**Status:** Draft

## Context

The system under test is server-rendered HTML with a script library that enhances it in place;
there is no REST surface and no JSON. The library marks its own requests with a request header,
and the application answers differently depending on whether that header is present:

- with it, a mutating step answers with the changed fragment alone;
- without it, the same step answers with a redirect, and the page that redirect leads to is
  fetched as a second request.

Measured on the running stack, one task creation costs one request and roughly 1.6 KB in the
first mode, against a redirect plus a full page of roughly 7.7 KB in the second. These are
different amounts of template work, different response sizes and a different number of requests
for one and the same business step.

The rig produces the injector side of captures that a triage reads, and a capture is read against
a baseline capture. Whatever is chosen here therefore has to hold for a whole capture and across
the captures that are compared with each other. The specification received from the SUT project
states the same requirement and names the fragment mode as the realistic choice.

## Alternatives

**1. The fragment mode, held for every request of every run.** What a browser with the script
library sends, so the load is shaped like the application's real traffic. The endpoint latencies
a capture carries are the ones a user experiences. Cost: the rig must set the header on every
request and must read fragments rather than pages, which makes the extraction of a task identity
a matter of the fragment's markup.

**2. The no-script mode, held for every request of every run.** Simpler to script: every step
answers a whole page, and the markup a step reads is the page's. Cost: it doubles the request
count of every mutating step and inflates the template work per step, so the percentiles a
capture carries describe a client population the application does not have. It also spends the
generator's allotted cores on parsing pages the rig does not need.

**3. A mix of the two, in some ratio meant to model a real client population.** Arguably the most
realistic of the three. Cost: it makes an endpoint's latency a function of the generator's own
coin toss, so a percentile becomes a statement about the mix rather than about the system, and
two captures are comparable only if the mix was identical and recorded. The ratio would be one
more unmeasured number in a project that already owes the SUT project four of them.

## Decision

The fragment mode, for every request of every run: the request header of the script library is
set by a single named constant of the rig and is never varied inside a run or between the runs of
one campaign.

It is chosen over the no-script mode because it is the traffic the application actually receives,
and over a mix because a capture whose latencies depend on an unrecorded ratio cannot serve as a
baseline for another capture.

## Consequences

- Every step of the load model reads a fragment. Task identity is extracted from the fragment of
  the answer that produced it, and the extraction is part of the transport rather than of the
  scenario.
- A refusal arrives as a `422` carrying its reason, because the application's own configuration
  lets that status be swapped into the page. The rig treats it as a loud failure: its steps are
  legal by the transition table, so a refusal means the script asked for something the
  application does not allow.
- The mode is a fact of the capture and belongs in the load-profile description authored per
  variant, so that a capture can be re-read later against the shape it was taken under.
- A future need to measure the no-script path — a client population that does not run the script
  library — is a separate campaign under a superseding record, never a variation inside a
  capture.
