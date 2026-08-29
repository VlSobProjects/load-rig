package loadrig.model;

/**
 * The hot window of the scenario specification: an open task due within three days is a hot task,
 * one of the few the discussion converges on and the skew sends most of its attention to.
 *
 * <p>Stated once, because three facts of the rig rest on it and would drift apart if each held its
 * own copy: the due date a creation draws, the flag the registry marks a task with when it reads
 * one off the stand, and the census the warm start brings a stand to before a window opens.
 */
public final class HotWindow {

    /** How far ahead a due date may sit and still be due soon. */
    public static final int DAYS = 3;

    /** The due date of a task meant to be hot, as an ISO period from today. */
    public static final String DUE_INSIDE = "P" + DAYS + "D";

    /**
     * The due date of a task meant to be cold: far enough beyond the window that no run of a
     * campaign pulls it into the hot set by simply lasting.
     */
    public static final String DUE_OUTSIDE = "P14D";

    private HotWindow() {
    }
}
