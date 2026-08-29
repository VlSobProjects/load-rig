package loadrig.model.scenario;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import loadrig.model.profile.LoadProfile;
import loadrig.model.profile.ScenarioName;
import loadrig.model.profile.StepMix;
import loadrig.model.step.StepKind;
import org.junit.jupiter.api.Test;

/**
 * Holds the mix-survival check against the day profile's own arithmetic: the scenarios' stated
 * weights over the day populations reproduce the specification's mix exactly, and populations
 * that break the reproduction are refused with both numbers.
 */
class MixSurvivalCheckTest {

    @Test
    void theDayPopulationsReproduceTheDayMix() {
        assertDoesNotThrow(() -> MixSurvivalCheck.check(dayProfile(5, 3, 1, 1)));
    }

    @Test
    void everyKindOfTheMixIsComputedExactlyForTheDayProfile() {
        LoadProfile profile = dayProfile(5, 3, 1, 1);
        Map<ScenarioName, Map<StepKind, Integer>> weights = Scenarios.stepWeights();

        assertEquals(37.0, MixSurvivalCheck.computedShare(profile, weights,
                StepKind.LOOKING_AT_A_LIST), 0.01);
        assertEquals(22.0, MixSurvivalCheck.computedShare(profile, weights,
                StepKind.OPENING_ONE_TASK), 0.01);
        assertEquals(13.0, MixSurvivalCheck.computedShare(profile, weights,
                StepKind.MOVING_A_TASK), 0.01);
        assertEquals(12.0, MixSurvivalCheck.computedShare(profile, weights,
                StepKind.DISCUSSION), 0.01);
        assertEquals(10.0, MixSurvivalCheck.computedShare(profile, weights,
                StepKind.RUNNING_A_REPORT), 0.01);
        assertEquals(5.0, MixSurvivalCheck.computedShare(profile, weights,
                StepKind.CREATING_A_TASK), 0.01);
        assertEquals(1.0, MixSurvivalCheck.computedShare(profile, weights,
                StepKind.DELETING_A_TASK), 0.01);
    }

    @Test
    void populationsThatBreakTheMixAreRefusedWithTheNumbers() {
        IllegalArgumentException refusal = assertThrows(IllegalArgumentException.class,
                () -> MixSurvivalCheck.check(dayProfile(3, 5, 1, 1)));

        assertTrue(refusal.getMessage().contains("does not survive"),
                "the refusal must name the drift: " + refusal.getMessage());
    }

    private static LoadProfile dayProfile(int workers, int managers, int administrators,
            int discussion) {
        return new LoadProfile("day", workers + managers + administrators + discussion, 30, 10,
                new LoadProfile.ThinkTime(3, 7), new StepMix(37, 22, 13, 12, 10, 5, 1),
                new LoadProfile.HotSetSkew(70, 80),
                Map.of(ScenarioName.WORKER, workers, ScenarioName.MANAGER, managers,
                        ScenarioName.ADMINISTRATOR, administrators,
                        ScenarioName.DISCUSSION, discussion));
    }
}
