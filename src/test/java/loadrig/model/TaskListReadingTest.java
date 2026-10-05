package loadrig.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Holds the reading of the list page against a captured answer of the application. What it reads
 * decides which tasks a warm start believes the stand holds, and a misread row is a run that
 * starts on a population it only imagined - so it is proven here and not in a run, which would
 * pass either way.
 */
class TaskListReadingTest {

    private static final String LIST_PAGE = "task-list-page.html";

    @Test
    void everyRowIsReadWithTheFactsTheRegistryActsOn() throws IOException {
        List<TaskListReading.Row> rows = TaskListReading.rowsOf(capturedAnswer());

        assertEquals(3, rows.size());
        TaskListReading.Row first = rows.get(0);
        assertEquals("407", first.taskId());
        assertEquals("lr3-20260829-192015-t7", first.title());
        assertEquals("manager-02", first.creator());
        assertEquals("worker-16", first.assignee());
        assertEquals(LocalDate.of(2026, 9, 1), first.dueDate());
        assertEquals(SutSurface.StatusMark.OPEN, first.status());
    }

    @Test
    void theStatusIsTheStateBehindTheWordTheApplicationShows() throws IOException {
        List<TaskListReading.Row> rows = TaskListReading.rowsOf(capturedAnswer());

        assertEquals(SutSurface.StatusMark.COMPLETED, rows.get(1).status(),
                "finished work is stored as completed and shown as done; a reader that took the"
                        + " word for the state would register it as a state nobody defined");
        assertEquals(SutSurface.StatusMark.REFUSED, rows.get(2).status());
    }

    @Test
    void aMarkThatNamesNoStatusRefusesTheReading() throws IOException {
        String answer = capturedAnswer().replace(">Open<", ">Rejected<");

        IllegalStateException refusal =
                assertThrows(IllegalStateException.class, () -> TaskListReading.rowsOf(answer));

        assertTrue(refusal.getMessage().contains("Rejected"),
                "the refusal names the mark it could not read: " + refusal.getMessage());
    }

    @Test
    void theHotSetIsDecidedByTheDueDateAgainstTheWindow() throws IOException {
        List<TaskListReading.Row> rows = TaskListReading.rowsOf(capturedAnswer());
        LocalDate today = LocalDate.of(2026, 8, 30);

        assertTrue(rows.get(0).dueSoon(today), "due in two days is inside the three-day window");
        assertFalse(rows.get(1).dueSoon(today), "due in three weeks is not");
        assertTrue(rows.get(0).dueSoon(LocalDate.of(2026, 9, 5)),
                "a task already past its due date is due soon, and more so");
    }

    @Test
    void theVolumeOfTheStandIsTheCountTheListPagesItselfBy() throws IOException {
        Optional<TaskListReading.Paging> paging = TaskListReading.pagingOf(capturedAnswer());

        assertTrue(paging.isPresent());
        assertEquals(1, paging.get().page());
        assertEquals(17, paging.get().pages());
        assertEquals(323, paging.get().tasksInTotal());
    }

    @Test
    void theCountIsReadThroughTheGroupingTheListRendersItUnder() {
        Optional<TaskListReading.Paging> paging =
                TaskListReading.pagingOf("Page 1 of 54, 1,063 in total");

        assertTrue(paging.isPresent(), "the first stand to pass a thousand tasks refused the"
                + " reading, and a stand the rig cannot read cannot be acted on");
        assertEquals(54, paging.get().pages());
        assertEquals(1063, paging.get().tasksInTotal());
    }

    @Test
    void anAnswerWithoutAPagingStateIsReadAsNoCountAtAll() {
        assertEquals(Optional.empty(), TaskListReading.pagingOf("<section id=\"task-table\">"),
                "a list with nothing to page over states no count, and the caller decides what"
                        + " that means; inventing a count here would publish it as measured");
    }

    private String capturedAnswer() throws IOException {
        try (InputStream captured = getClass().getResourceAsStream(LIST_PAGE)) {
            if (captured == null) {
                throw new IOException("the captured answer " + LIST_PAGE + " is missing");
            }
            return new String(captured.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
