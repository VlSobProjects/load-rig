package loadrig.model.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Holds the equilibrium check against the specification's own arithmetic: the day mix at ten
 * users and a five-second mean think time gives about 120 steps a minute, six creations against
 * about six settlements, and the population holds; a mix that creates without settling - or
 * settles what was not created - is refused with the numbers.
 */
class EquilibriumCheckTest {

    private static final double INTENSITY_PRECISION = 0.05;

    @Test
    void theDayMixHoldsThePopulation() {
        EquilibriumCheck.Intensities intensities = EquilibriumCheck.check(dayProfile());

        assertEquals(120.0, intensities.stepsPerMinute(), INTENSITY_PRECISION);
        assertEquals(6.0, intensities.creationsPerMinute(), INTENSITY_PRECISION);
        assertEquals(6.24, intensities.settlementsPerMinute(), INTENSITY_PRECISION);
        assertEquals(1.2, intensities.deletionsPerMinute(), INTENSITY_PRECISION);
        assertEquals(14.4, intensities.notesPerMinute(), INTENSITY_PRECISION);
        assertEquals(-14.4, intensities.netDriftOverWindow(), 0.5);
    }

    @Test
    void aMixThatCreatesWithoutSettlingIsRefused() {
        LoadProfile drifting = dayProfileWith(new StepMix(30, 22, 3, 12, 10, 22, 1));

        IllegalArgumentException refusal = assertThrows(IllegalArgumentException.class,
                () -> EquilibriumCheck.check(drifting));
        assertTrue(refusal.getMessage().contains("two regimes"),
                "the refusal must state the consequence: " + refusal.getMessage());
    }

    @Test
    void aMixThatDrainsThePopulationIsRefusedToo() {
        LoadProfile draining = dayProfileWith(new StepMix(25, 21, 30, 12, 10, 1, 1));

        assertThrows(IllegalArgumentException.class, () -> EquilibriumCheck.check(draining));
    }

    private static LoadProfile dayProfile() {
        return dayProfileWith(new StepMix(37, 22, 13, 12, 10, 5, 1));
    }

    private static LoadProfile dayProfileWith(StepMix mix) {
        return new LoadProfile("day", 10, 30, 10,
                new LoadProfile.ThinkTime(3, 7), mix,
                new LoadProfile.HotSetSkew(70, 80), 50000,
                Map.of(ScenarioName.WORKER, 5, ScenarioName.MANAGER, 3,
                        ScenarioName.ADMINISTRATOR, 1, ScenarioName.DISCUSSION, 1));
    }
}
