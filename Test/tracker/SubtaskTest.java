package tracker;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SubtaskTest {

    @Test
    void subtasksAreEqualIfIdIsSame() {
        Subtask sub1 = new Subtask("Sub1", "Desc1", Status.NEW, 10);
        sub1.setId(100);

        Subtask sub2 = new Subtask("Sub2", "Desc2", Status.DONE, 10);
        sub2.setId(100);

        assertEquals(sub1, sub2, "Подзадачи с одинаковым id должны быть равны");
    }

    @Test
    void subtaskCannotBeItsOwnEpic() {
        Subtask sub = new Subtask("Sub", "Desc", Status.NEW, 1);
        sub.setId(1);

        assertNotEquals(sub.getId(), sub.getEpicId(),
                "Подзадача не может быть своим же эпиком");
    }
}