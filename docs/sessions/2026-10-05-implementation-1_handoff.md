# Session note

## Type

`implementation`

## Goal

Make the load-profile description state its computed figures without the noise of the arithmetic
that produced them, and cut the README's fragment again from a run that wrote them so.

## Completion criteria

1. The target rates and the table's growth are written with a stated number of decimals, held by
   one named constant, and a unit test fails if a description carries a figure beyond it.
2. Nothing the run computes from the demand changes: the census, the plan and the profile
   validation read the same numbers as before.
3. The README's fragment is a verbatim cut of a run made by the corrected code, and its caption is
   still true of it.
4. `./gradlew build` passes from the command line.

## Scope boundaries

The demand itself is not rounded and the description gains or loses no field: the names and the
types a capture's reader joins on stay what they were. The run that feeds the fragment is not a
capture and not a step of any campaign; no figure of it is read against anything.

## Status

`completed`

## Result

All four criteria are met.

The description rounds what it publishes and nothing else does. The ten target rates and the
table's growth pass through one rule on their way into the artifact - three decimals, half up -
and the demand they come from is untouched, so the census of the day profile and the rates the
profile validation prints are the ones they were. The descriptions on disk were read first: the
noise sat in two of the rates and, once, in the table's growth, which is why the growth is rounded
with the rates although it is not one.

Two tests hold it. One writes the day profile's description and refuses any rate, and the growth,
spelled with more decimals than the constant states; the other holds the rule itself on the two
figures that were wrong, on a rate that does not terminate and on a half. Both were seen to fail
with the rounding taken out.

Verified from the command line: `./gradlew build` passes with 155 unit tests and no stack asked.
One window against the standing stack on 2026-10-05, SUT version 1.0.0, a scratch profile with
the day profile's mix and populations over five minutes: the stand held 3079 tasks, the warm start
read 440 and created 272 against a census of 357; 811 samples, none failed, nothing refused, and
every gate found its pick. The description it wrote states 15.6 and 14.4 where the earlier runs
stated the noise, and no figure in it runs past three decimals. The host was not closed and the
generator not pinned, so the run is what the README calls it, uncalibrated, and none of its waits
is recorded here.

The README's fragment is cut from that run, line for line; each line of it was checked against the
two files. Its ledger is the empty one, so the sentence under it now says what a ledger is for and
that this run skipped nothing.

## Decisions

- **The statement is rounded, not the demand.** Rounding at the source would move the census by
  the stock a rounded rate implies and make a capture incomparable with the ones before it, for
  the sake of how a number is spelled.
- **The rule lives in the code that builds the description, not in a serializer.** A record that
  held one value and wrote another would be two statements of one fact.
- **Three decimals.** A thousandth of a step per minute is below anything an achieved intensity is
  compared at; for a rate that does not terminate, the seven operations may sum to the steps only
  within a few thousandths, which is the price and is stated here.
- **The fragment was cut again rather than edited.** The earlier fragment was true of its run and
  would have stayed so; correcting two numbers by hand would have made it a fragment of nothing.
  The fresh run's ledger names no skip, and the fragment shows that rather than a run chosen for
  a fuller one.

## Open questions

None.

## Next steps

1. The answer on the JobSearch desk names the run the fragment is cut from; it is corrected to the
   run of 2026-10-05 before it is carried.
2. The checks before publication that need the forge, unchanged from the session before this one.

## References

- Branch: `fix/rounded-published-rates`
- Changed: `LoadProfileDescription` and its test, `README.md`
- Follows: [the session that readied the repository for publication](2026-10-05-documentation-1_handoff.md),
  whose open question this answers
