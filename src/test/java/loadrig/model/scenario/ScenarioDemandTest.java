package loadrig.model.scenario;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import loadrig.model.SutSurface.Transition;
import loadrig.model.profile.LoadProfile;
import loadrig.model.profile.ProfileLoader;
import loadrig.model.step.StepKind;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Holds the demand the coded scenarios imply against the day profile's own arithmetic: ten users
 * thinking five seconds on average take twelve steps a minute each, and every rate a capture
 * publishes and every stock the census asks for is a share of those hundred and twenty steps.
 *
 * <p>These are the numbers that replaced the specification's weighting of about two and a half
 * transitions per settlement, so they are checked one by one rather than trusted.
 */
class ScenarioDemandTest {

    private static final double PRECISION = 1e-9;

    private final ScenarioDemand day =
            ScenarioDemand.of(ProfileLoader.load(Path.of("profiles", "day.json")));

    @Test
    void theStepsAreTheUsersOverTheirThinkTime() {
        assertEquals(120, day.stepsPerMinute(), PRECISION,
                "ten users at a mean think time of five seconds take twelve steps a minute each");
    }

    @Test
    void theSessionsGivenUpAreTheStatedShareOfTheIterations() {
        assertEquals(2.4, day.sessionsGivenUpPerMinute(), PRECISION,
                "two of every hundred iterations end their session, and 120 iterations a minute"
                        + " therefore give up 2.4 of them - which is also the rate the sign-ins"
                        + " should arrive at");
    }

    @Test
    void everyStepKindGetsItsShareAndTogetherTheyAreEveryStep() {
        assertEquals(6, day.creationsPerMinute(), PRECISION);
        assertEquals(1.2, day.deletionsPerMinute(), PRECISION);
        assertEquals(14.4, day.notesPerMinute(), PRECISION);
        double everyKind = 0;
        for (StepKind kind : StepKind.values()) {
            everyKind += day.rateOf(kind);
        }
        assertEquals(day.stepsPerMinute(), everyKind, PRECISION,
                "a step is of exactly one kind, so the kinds sum to the steps");
    }

    @Test
    @DisplayName("each transition is asked for at the rate its scenario's weights give it")
    void everyTransitionGetsItsShare() {
        assertEquals(6, day.rateOf(Transition.FINISH), PRECISION);
        assertEquals(1.2, day.rateOf(Transition.RETURN_WITH_A_QUESTION), PRECISION);
        assertEquals(1.2, day.rateOf(Transition.REFUSE), PRECISION);
        assertEquals(4.8, day.rateOf(Transition.APPROVE), PRECISION);
        assertEquals(1.2, day.rateOf(Transition.ACCEPT_THE_REFUSAL), PRECISION);
        assertEquals(0.6, day.rateOf(Transition.HAND_OUT_AGAIN), PRECISION);
        assertEquals(0.6, day.rateOf(Transition.PUT_BACK_TO_WORK), PRECISION);
        assertEquals(1.2, day.rateOf(Transition.DELETE), PRECISION);
    }

    @Test
    void theTransitionsSumToTheStepsThatMoveAndDeleteTasks() {
        double moves = 0;
        double deletions = 0;
        for (Transition transition : Transition.values()) {
            if (transition == Transition.DELETE) {
                deletions += day.rateOf(transition);
            } else {
                moves += day.rateOf(transition);
            }
        }
        assertEquals(day.rateOf(StepKind.MOVING_A_TASK), moves, PRECISION);
        assertEquals(day.rateOf(StepKind.DELETING_A_TASK), deletions, PRECISION);
    }

    @Test
    void aTaskIsSettledWhenItsWorkIsApprovedOrItsRefusalAccepted() {
        assertEquals(6, day.settlementsPerMinute(), PRECISION,
                "the settlements are the approvals and the acknowledgements themselves, not a"
                        + " division of every transition by a weighting constant");
    }

    @Test
    void theWindowGrowsTheTableByWhatItCreatesLessWhatItDeletes() {
        assertEquals(48, day.tableGrowthOverWindow(), PRECISION,
                "six creations and 1.2 deletions a minute over ten minutes; a transition moves a"
                        + " task and never removes one");
    }
}
