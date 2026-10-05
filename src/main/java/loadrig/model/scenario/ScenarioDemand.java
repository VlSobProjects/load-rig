package loadrig.model.scenario;

import java.util.EnumMap;
import java.util.Map;
import loadrig.model.SutSurface.Transition;
import loadrig.model.profile.LoadProfile;
import loadrig.model.profile.ScenarioName;
import loadrig.model.step.StepKind;

/**
 * What the scenarios as coded ask of the stand per minute under one profile: the rate of every
 * step kind and of every transition, at the profile's populations and think time.
 *
 * <p>It replaces the specification's weighting of about two and a half transitions per settlement,
 * which was an approximation of numbers the scenarios state exactly: the weights are code, so the
 * settlements a window realizes are the approvals plus the acknowledgements and nothing has to be
 * assumed about them. Every rate a capture publishes and every stock the census asks for is
 * computed here, from the same tables the plan is built from, so the description of a run and the
 * run cannot describe different mixes.
 *
 * <p>One reading underlies all of it, the same one the mix-survival check makes: a virtual user
 * takes one step per think time, whatever the step is.
 *
 * <p>The sessions given up stand beside the step kinds rather than among them: giving one up is
 * no row of the mix and takes no share from the business steps, but it is an intensity the run
 * applies and therefore one the capture must state. It is also the rate of the sign-ins, because
 * in a steady state an account that gave its session up signs in again - and, read per seat, the
 * length of one stint, which is the horizon the census stocks a bucket against.
 */
public record ScenarioDemand(
        double stepsPerMinute,
        double sessionsGivenUpPerMinute,
        double seatStintMinutes,
        Map<StepKind, Double> perKind,
        Map<Transition, Double> perTransition,
        int steadyWindowMinutes) {

    public ScenarioDemand {
        perKind = Map.copyOf(perKind);
        perTransition = Map.copyOf(perTransition);
    }

    public static ScenarioDemand of(LoadProfile profile) {
        double stepsPerUser = 60.0 / profile.thinkTime().meanSeconds();
        Map<ScenarioName, Map<StepKind, Integer>> steps = Scenarios.stepWeights();
        Map<ScenarioName, Map<Transition, Integer>> transitions = Scenarios.transitionWeights();
        EnumMap<StepKind, Double> perKind = new EnumMap<>(StepKind.class);
        EnumMap<Transition, Double> perTransition = new EnumMap<>(Transition.class);
        for (ScenarioName name : ScenarioName.values()) {
            int population = profile.scenarioPopulation().get(name);
            if (population == 0) {
                continue;
            }
            Map<StepKind, Integer> table = steps.get(name);
            double total = table.values().stream().mapToInt(Integer::intValue).sum();
            double scenarioSteps = population * stepsPerUser;
            table.forEach((kind, weight) ->
                    perKind.merge(kind, scenarioSteps * weight / total, Double::sum));
            transitions.get(name).forEach((transition, weight) ->
                    perTransition.merge(transition, scenarioSteps * weight / total, Double::sum));
        }
        double iterationsPerMinute = profile.virtualUsers() * stepsPerUser;
        return new ScenarioDemand(iterationsPerMinute,
                iterationsPerMinute * profile.endingASessionPercent() / 100.0,
                seatStintMinutes(stepsPerUser, profile.endingASessionPercent()),
                perKind, perTransition, profile.steadyWindowMinutes());
    }

    /**
     * How long one seat is held by the same account before that account gives the session up: the
     * iterations a stint lasts on average - one in every {@code endingASessionPercent} of them
     * ends the stint - taken at the rate a seat performs them, which is one per think time.
     *
     * <p>It is the horizon a bucket must survive without leaving its seat, and therefore the
     * horizon the census stocks against: the rotation does not turn over between two steps, so an
     * account holding a seat is drawn from at the seat's rate however wide the rotation is.
     */
    private static double seatStintMinutes(double stepsPerUser, int endingASessionPercent) {
        return 100.0 / endingASessionPercent / stepsPerUser;
    }

    /** The rate of one step kind; a kind no scenario takes is asked for at a rate of zero. */
    public double rateOf(StepKind kind) {
        return perKind.getOrDefault(kind, 0.0);
    }

    /** The rate of one transition; one no scenario applies is asked for at a rate of zero. */
    public double rateOf(Transition transition) {
        return perTransition.getOrDefault(transition, 0.0);
    }

    public double creationsPerMinute() {
        return rateOf(StepKind.CREATING_A_TASK);
    }

    public double deletionsPerMinute() {
        return rateOf(StepKind.DELETING_A_TASK);
    }

    public double notesPerMinute() {
        return rateOf(StepKind.DISCUSSION);
    }

    /** A task is settled when its work is approved or its refusal accepted, and only then. */
    public double settlementsPerMinute() {
        return rateOf(Transition.APPROVE) + rateOf(Transition.ACCEPT_THE_REFUSAL);
    }

    /**
     * How many rows the window adds to the task table: creations less deletions, and nothing else,
     * because a transition moves a task and never removes one. It is a fact of the capture, read
     * by its reader against the volume the stand was measured to hold - noise on a seeded stand
     * and the whole table on an empty one, which is a property of the stand and not of the mix.
     */
    public double tableGrowthOverWindow() {
        return (creationsPerMinute() - deletionsPerMinute()) * steadyWindowMinutes;
    }
}
