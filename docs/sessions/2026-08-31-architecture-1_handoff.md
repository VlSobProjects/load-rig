# Session note

## Type

`architecture`

## Goal

Settle, before any stand window is spent on it, which calibration lever answers the headroom the
first measured baseline found, and record the choice as a decision record LR-11 executes against.

## Completion criteria

1. The measured headroom is stated as the problem it actually is, per band, with the reason the
   loosest band is loose named rather than assumed.
2. At least two alternatives are weighed with their cost, including the one this project cannot
   turn alone, and the rejected ones are rejected for a stated reason.
3. The decision states what the stepped search is judged by, how those figures come to exist, and
   what makes a step count at all.
4. The decision record is registered, and the roadmap's LR-11 row carries what the search now is.
5. Nothing in the rig's code changes in this session.

## Scope boundaries

No stepped run, no calibration run and no profile file: this session decides the criterion, and
the item that climbs executes against it. The comfort figures of the specification are not edited
and the file that holds them is not touched.

The resources and the seeded volume are named as the other project's levers and left where they
are; the letter that asks about them is a documentation session of its own, together with the
fifth answer the baseline owes.

## Status

`completed`

## Result

All five criteria are met. [DR-7](../decisions/dr-7-the-search-has-its-own-criterion.md) is drafted,
the roadmap's LR-11 row is rewritten around it, and the brief's service-levels statement carries the
one thing the decision makes inaccurate there — that a capture's campaign is now part of what the
capture means.

The headroom is not uniform and its shape is the finding. Signing in has the least, at fourteenfold,
and its cost is password hashing rather than the business the profile drives. Running a report has
seventyfold, and it has it because the measured stand held under six hundred tasks: the report scans
that table in memory. So the band that would end an intensity-scaled search first is the one least
connected to the fault surface, and the ordering itself is a property of a stand whose volume the
SUT project has not yet chosen.

Three failure modes were identified for a search judged by the chosen figures, and together they
decided the lever. Such a search terminates past the knee, where the waits cross the hard ceiling
almost at once, which makes the terminating step an overload capture. Under genuine saturation this
stack is as likely to give at the connection pool, whose signature is the induced fault of another
variant, so the maximum would be indistinguishable from a planted defect. And the generator holds
one physical core against the application's two, so the search may end on the injector instead —
the starved-injector variant produced by accident.

## Decisions

- **[DR-7](../decisions/dr-7-the-search-has-its-own-criterion.md) — the search is judged by a
  criterion derived from the baseline.** The lever turned is the figures, in a derived form: the
  search is a campaign of its own with a service-levels file of its own; each band's level is the
  same multiple of that band's measured percentile on the clean baseline of the same stand; the
  multiple is fixed by a calibration run that is spent rather than published; the hard ceiling stays
  the specification's and is not a criterion of the search; and a step counts only if it drove the
  intensity it intended and judged its bands on samples enough to decide one.
- **The resources lever is not turned, and the reason is not only ownership.** Its only admissible
  form is a narrower core set: lowering the quota instead would raise the throttled-period counter,
  which is precisely the condition under which the calibration rule declares a capture an artefact
  of the limit. It also buys about a factor of two against a headroom of tens, and it moves every
  capture of the campaign to a new core map.
- **The volume of data is named as the third lever behind the headroom** and left with the SUT
  project's seeding item, because the report's seventyfold headroom is a fact about an almost empty
  table rather than about the application.
- **The decision costs no code.** The service levels are already chosen per run by their own key and
  loaded strictly, so a second campaign is a second file and a run key.

## Open questions

- The multiple is a hypothesis at fivefold until the calibration run moves it. What the calibration
  run must show is where the response curve leaves its flat part and which side gives first, and
  whether the injector's single physical core reaches that point at all.
- The search scales the intensity and, through the census, the population the stand holds, so a
  later step meets a larger table than the one before it. The shape is held and the volume is not.
  Each step publishes the volume it ran against, so the confound is visible; whether the search
  should instead be run against a fixed volume is a question for the seeding item.
- Whether the application's core set may be narrowed for a later campaign, and what seeded volume
  the report is meant to scan, are questions for the desk and are not asked in this session.

## Next steps

1. LR-11, in two parts: the calibration run that fixes the multiple, then the stepped campaign
   against the derived file, each step a capture judged under the two guards.
2. A documentation session delivering the fifth answer — the measured baseline — to the SUT project
   through the desk, carrying the two questions above with it.
3. LR-12: the morning and evening profile variants.

## References

- Branch: `docs/dr_7-the-search-has-its-own-criterion`
- Added: `docs/decisions/dr-7-the-search-has-its-own-criterion.md`
- Documents changed: `docs/roadmap.md` (LR-11 rewritten; LR-6 marked done, which its own branch
  left behind), `docs/brief.md` (the service-levels statement)
