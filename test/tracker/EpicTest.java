package tracker;

import model.Epic;
import model.Subtask;
import model.Status;
import manager.InMemoryTaskManager;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class EpicTest {

    @Test
    void epicsAreEqualIfIdIsSame() {
        Epic epic1 = new Epic("Epic1", "Desc1");
        epic1.setId(5);

        Epic epic2 = new Epic("Epic2", "Desc2");
        epic2.setId(5);

        assertEquals(epic1, epic2,
                "Эпики с одинаковым id должны быть равны");
    }

    @Test
    void epicCannotContainItselfAsSubtask() {
        InMemoryTaskManager manager = new InMemoryTaskManager();

        Epic epic = new Epic("Epic", "Desc");
        int epicId = manager.addEpic(epic);

        Subtask subtask = new Subtask(
                "Sub",
                "Desc",
                Status.NEW,
                Duration.ofMinutes(10),
                LocalDateTime.now(),
                epicId
        );

        // специально делаем id == epicId, чтобы проверить ошибку
        subtask.setId(epicId);

        int result = manager.addSubtask(subtask);

        assertEquals(-1, result,
                "Менеджер не должен позволять добавить эпик как его же подзадачу");
    }
}