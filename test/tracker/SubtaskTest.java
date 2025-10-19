package tracker;

import model.Subtask;
import model.Epic;
import model.Status;
import manager.InMemoryTaskManager;

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
        InMemoryTaskManager manager = new InMemoryTaskManager();

        Epic epic = new Epic("Epic", "Desc");
        int epicId = manager.addEpic(epic);


        Subtask sub = new Subtask("Sub", "Desc", Status.NEW, epicId);
        sub.setId(epicId);

        int result = manager.addSubtask(sub);

        assertEquals(-1, result,
                "Подзадача не должна добавляться, если её epicId совпадает с её id");
    }
}
