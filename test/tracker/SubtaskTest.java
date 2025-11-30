package tracker;

import model.Subtask;
import model.Epic;
import model.Status;
import manager.InMemoryTaskManager;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.LocalDateTime;

public class SubtaskTest {

    @Test
    void subtasksAreEqualIfIdIsSame() {
        Subtask sub1 = new Subtask(
                "Sub1",
                "Desc1",
                Status.NEW,
                Duration.ofMinutes(30),
                LocalDateTime.now(),
                10
        );
        sub1.setId(100);

        Subtask sub2 = new Subtask(
                "Sub2",
                "Desc2",
                Status.DONE,
                Duration.ofMinutes(45),
                LocalDateTime.now().plusHours(1),
                10
        );
        sub2.setId(100);

        assertEquals(sub1, sub2,
                "Подзадачи с одинаковым id должны быть равны");
    }

    @Test
    void subtaskCannotBeItsOwnEpic() {
        InMemoryTaskManager manager = new InMemoryTaskManager();

        Epic epic = new Epic("Epic", "Desc");
        int epicId = manager.addEpic(epic);

        // создаём subtask, привязанный к epicId
        Subtask sub = new Subtask(
                "Sub",
                "Desc",
                Status.NEW,
                Duration.ofMinutes(10),
                LocalDateTime.now(),
                epicId
        );


        sub.setId(epicId);

        int result = manager.addSubtask(sub);

        assertEquals(-1, result,
                "Подзадача не должна добавляться, если её epicId совпадает с её id");
    }
}
