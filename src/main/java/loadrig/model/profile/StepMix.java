package loadrig.model.profile;

/**
 * The step mix of a profile: what share of all steps each of the specification's seven step
 * kinds takes, in whole percent. The shares must account for every step - they sum to one
 * hundred - because a mix with an unaccounted remainder is a mix the equilibrium check would
 * silently misjudge.
 *
 * <p>The kinds are the rows of the specification's mix table, named after the business act. The
 * discussion step is one visit to the notes tab that leaves one note, which is why the notes
 * intensity follows this share directly.
 */
public record StepMix(
        int lookingAtAListPercent,
        int openingOneTaskPercent,
        int movingATaskPercent,
        int discussionPercent,
        int runningAReportPercent,
        int creatingATaskPercent,
        int deletingATaskPercent) {

    public StepMix {
        requireShare("lookingAtAList", lookingAtAListPercent);
        requireShare("openingOneTask", openingOneTaskPercent);
        requireShare("movingATask", movingATaskPercent);
        requireShare("discussion", discussionPercent);
        requireShare("runningAReport", runningAReportPercent);
        requireShare("creatingATask", creatingATaskPercent);
        requireShare("deletingATask", deletingATaskPercent);
        int sum = lookingAtAListPercent + openingOneTaskPercent + movingATaskPercent
                + discussionPercent + runningAReportPercent + creatingATaskPercent
                + deletingATaskPercent;
        if (sum != 100) {
            throw new IllegalArgumentException(
                    "the step mix must account for every step: the shares sum to " + sum
                            + " percent instead of 100");
        }
    }

    private static void requireShare(String step, int percent) {
        if (percent < 0 || percent > 100) {
            throw new IllegalArgumentException("the share of " + step + " must be a percentage,"
                    + " and it is " + percent);
        }
    }
}
