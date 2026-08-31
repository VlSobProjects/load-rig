package loadrig.run;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.Map;
import loadrig.model.SutSurface.Transition;
import loadrig.model.profile.LoadProfile;
import loadrig.model.profile.ProfileLoader;
import loadrig.model.scenario.ScenarioDemand;
import loadrig.model.step.StepKind;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The bound the tolerated losses are held against. A session can only lose a task somebody
 * deleted, so the profile's own intended deletions over the window are the ceiling: below it a
 * lost task is the race the SUT confirmed, above it the cause is something else and the capture
 * is not a clean one.
 */
class IntendedDeletionsTest {

    @Test
    @DisplayName("the bound is the profile's own deletion intensity over its own window")
    void theBoundComesFromTheProfile() {
        LoadProfile day = ProfileLoader.load(Path.of("profiles", "day.json"));

        long intended = ProfileRun.intendedDeletions(day, ScenarioDemand.of(day));

        assertEquals(12, intended, "the day profile deletes 1.2 tasks a minute over ten minutes;"
                + " the bound is that intention and not a figure of its own");
    }

    @Test
    @DisplayName("a fractional intention rounds up, so a run is never spoiled for obeying it")
    void aFractionalIntentionRoundsUp() {
        LoadProfile day = ProfileLoader.load(Path.of("profiles", "day.json"));
        ScenarioDemand fractional = new ScenarioDemand(120, 2.4,
                Map.of(StepKind.DELETING_A_TASK, 0.11), Map.of(Transition.DELETE, 0.11),
                day.steadyWindowMinutes());

        assertEquals(2, ProfileRun.intendedDeletions(day, fractional),
                "1.1 deletions over the window is an intention of two, not of one");
    }
}
