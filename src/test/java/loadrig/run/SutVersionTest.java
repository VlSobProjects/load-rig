package loadrig.run;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The reading of the stand's answer, without a stand: the shape it answers with today, the shape
 * a plainer publisher would answer with, and every shape that must produce an honest unknown
 * instead of an exception in the harness.
 */
class SutVersionTest {

    /** The body the stand answered on 2026-09-03, recorded as the fixture this reader is for. */
    private static final String THE_STANDS_ANSWER = """
            {"build":{"artifact":"spring-sut-task-tracker","name":"spring-sut-task-tracker",\
            "time":"2026-09-02T22:21:29.818Z","version":"1.0.0","group":"com.example"}}""";

    @Test
    @DisplayName("the version is read from the build facts the stand publishes")
    void readsTheBuildVersion() {
        assertEquals("1.0.0", SutVersion.versionIn(THE_STANDS_ANSWER));
    }

    @Test
    @DisplayName("a publisher that states only a version is read too")
    void readsAPlainVersion() {
        assertEquals("2.4.0", SutVersion.versionIn("{\"version\":\"2.4.0\"}"));
    }

    @Test
    @DisplayName("the build's version wins over a plain one, being the stand's own statement")
    void prefersTheBuildVersion() {
        assertEquals("1.0.0",
                SutVersion.versionIn("{\"version\":\"2.4.0\",\"build\":{\"version\":\"1.0.0\"}}"));
    }

    @Test
    @DisplayName("a shape the reader does not know answers unknown, and does not throw")
    void answersUnknownForAnUnknownShape() {
        assertEquals(SutVersion.UNKNOWN, SutVersion.versionIn("{}"));
        assertEquals(SutVersion.UNKNOWN, SutVersion.versionIn("{\"build\":{}}"));
        assertEquals(SutVersion.UNKNOWN, SutVersion.versionIn("{\"build\":{\"version\":\"\"}}"));
        assertEquals(SutVersion.UNKNOWN, SutVersion.versionIn("{\"build\":{\"version\":7}}"));
        assertEquals(SutVersion.UNKNOWN, SutVersion.versionIn("not a document at all"));
        assertEquals(SutVersion.UNKNOWN, SutVersion.versionIn(""));
    }
}
