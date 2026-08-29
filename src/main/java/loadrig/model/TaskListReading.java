package loadrig.model;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The only inventory the application offers, read: the rows of a task list and the count it is
 * paging over. There is no manifest of task identities and there will be none - no user of the
 * application holds one - so a rig that must learn what a stand already holds reads the list page,
 * which is the intended path and not a workaround.
 *
 * <p>Deliberately not a rule of {@link Correlation}: a rule there is one expression with one
 * capturing group, applied by the injector as an extractor on a sampler. A row is six facts read
 * by the rig's own Java outside any test plan, and the count is two more. What the two classes
 * share is the discipline - one named expression, beside the surface, provable against a captured
 * answer.
 */
public final class TaskListReading {

    /**
     * One row of the list: everything the registry needs to act on the task without opening it.
     * The columns are the ones the table renders, in the order it renders them.
     */
    public record Row(String taskId, String title, String creator, String assignee,
            LocalDate dueDate, SutSurface.StatusMark status) {

        /** Whether the row's due date falls inside the specification's hot window. */
        public boolean dueSoon(LocalDate today) {
            return !dueDate.isAfter(today.plusDays(HotWindow.DAYS));
        }
    }

    /** What the list says about itself: which page this is, how many there are, how many rows. */
    public record Paging(int page, int pages, int tasksInTotal) {
    }

    private static final Pattern ROW = Pattern.compile("(?s)<tr id=\"task-(\\d+)\">\\s*"
            + "<td>[^<]*</td>\\s*"
            + "<td>([^<]*)</td>\\s*"
            + "<td class=\"task-description\">.*?</td>\\s*"
            + "<td>([^<]*)</td>\\s*"
            + "<td>([^<]*)</td>\\s*"
            + "<td>([^<]*)</td>"
            + ".*?<span class=\"badge[^\"]*\">([^<]*)</span>");

    private static final Pattern PAGING =
            Pattern.compile("Page (\\d+) of (\\d+), (\\d+) in total");

    /**
     * The rows of the answer, in the order the list rendered them - which is the order the sort
     * asked for, and the reason a warm start reads the list ordered by due date: the hot end of the
     * stand comes first.
     *
     * <p>A row whose status mark names no status refuses the reading loudly. The alternative -
     * skipping it - would let the rig act on a stand it has silently misread.
     */
    public static List<Row> rowsOf(String answer) {
        List<Row> rows = new ArrayList<>();
        Matcher matcher = ROW.matcher(answer);
        while (matcher.find()) {
            String taskId = matcher.group(1);
            SutSurface.StatusMark status = SutSurface.StatusMark.ofMark(matcher.group(6).trim())
                    .orElseThrow(() -> new IllegalStateException("the task " + taskId
                            + " is listed with the status mark \"" + matcher.group(6).trim()
                            + "\", which names no status this rig knows; a stand whose statuses"
                            + " the rig cannot read cannot be acted on"));
            rows.add(new Row(taskId, matcher.group(2).trim(), matcher.group(3).trim(),
                    matcher.group(4).trim(), dueDate(taskId, matcher.group(5).trim()), status));
        }
        return List.copyOf(rows);
    }

    /**
     * What the list is paging over, when it says so. It is the measured volume of the stand: the
     * number a capture publishes about the history under the load, instead of a figure a profile
     * stated about a stand it cannot know.
     *
     * <p>Empty when the answer carries no paging state at all, which is what a list with nothing
     * to page over may answer with. The caller decides what that means: an answer with no rows and
     * no paging is an empty stand, and an answer with rows but no paging is not the list page this
     * reading was written against.
     */
    public static Optional<Paging> pagingOf(String answer) {
        Matcher matcher = PAGING.matcher(answer);
        if (!matcher.find()) {
            return Optional.empty();
        }
        return Optional.of(new Paging(Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher.group(3))));
    }

    private static LocalDate dueDate(String taskId, String rendered) {
        try {
            return LocalDate.parse(rendered);
        } catch (DateTimeParseException e) {
            throw new IllegalStateException("the task " + taskId + " is listed with the due date \""
                    + rendered + "\", which is no date; the hot set is decided by due dates and"
                    + " cannot be decided by a guess", e);
        }
    }

    private TaskListReading() {
    }
}
