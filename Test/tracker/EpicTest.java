package tracker;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


    public class EpicTest {

        @Test
        void epicsAreEqualIfIdIsSame() {
            Epic epic1 = new Epic("Epic1", "Desc1");
            epic1.setId(5);
            Epic epic2 = new Epic("Epic2", "Desc2");
            epic2.setId(5);

            assertEquals(epic1, epic2, "Эпики с одинаковым id должны быть равны");
        }

        @Test
        void epicCannotContainItselfAsSubtask() {
            Epic epic = new Epic("Epic", "Desc");
            epic.setId(1);

            Subtask subtask = new Subtask("Sub", "Desc", Status.NEW, epic.getId());
            subtask.setId(1);

            assertNotEquals(epic.getId(), subtask.getId(),
                    "Эпик не может содержать сам себя в подзадачах");
        }
    }