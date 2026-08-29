/**
 * The profile vocabulary: the types that fix what a load profile may state. A profile is an
 * external JSON file that scales, weights and parameterizes what the code defines - the mix,
 * the skew, the think time, the ramp, the window, the populations - and never defines
 * behaviour; the moment a profile wants a branch, the branch is a scenario and is born in code
 * (DR-2's behaviour boundary). Loading is strict: an unknown key, a missing key, a scenario
 * name outside the closed list and a violated invariant each refuse the run loudly before any
 * load is applied.
 */
package loadrig.model.profile;
