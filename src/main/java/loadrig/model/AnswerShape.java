package loadrig.model;

/**
 * The answer shape the rig holds for a whole capture, decided by DR 1.
 *
 * <p>The application answers a step with the changed fragment when the request carries the header
 * the page's script library sets, and with a redirect to a whole page when it does not. The rig
 * sends the header on the requests the script library itself would make — the controls that carry
 * its attributes — and on no others, so that the traffic is the traffic a browser produces. The
 * choice is never varied inside a run or between the runs of one campaign.
 */
public final class AnswerShape {

    /** The header the page's script library sets on every request it makes. */
    public static final String SCRIPT_LIBRARY_HEADER = "HX-Request";

    public static final String SCRIPT_LIBRARY_HEADER_VALUE = "true";

    private AnswerShape() {
    }
}
