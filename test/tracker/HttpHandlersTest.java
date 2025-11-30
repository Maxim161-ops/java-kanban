package tracker;

import manager.InMemoryTaskManager;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HttpHandlersTest {

    private InMemoryTaskManager manager;

    @BeforeEach
    void setup() {
        manager = new InMemoryTaskManager();
    }

    // ---------------- TaskHandler ----------------
    @Test
    void taskHandlerAddGetDelete() {
        Task task = new Task("Task1", "Desc1", Status.NEW, Duration.ofMinutes(10), LocalDateTime.now());
        manager.addTask(task);

        // Проверка добавления
        assertEquals(1, manager.getAllTasks().size());
        Task t = manager.getTask(task.getId());
        assertNotNull(t);
        assertEquals("Task1", t.getTitle());  // используем поле напрямую

        // Удаление
        boolean deleted = manager.deleteTaskById(task.getId());
        assertTrue(deleted);
        assertNull(manager.getTask(task.getId()));
    }

    // ---------------- SubtaskHandler ----------------
    @Test
    void subtaskHandlerAddGetDelete() {
        Epic epic = new Epic("Epic1", "EpicDesc");
        manager.addEpic(epic);

        Subtask sub = new Subtask("Sub1", "SubDesc", Status.NEW, Duration.ofMinutes(5), LocalDateTime.now(), epic.getId());
        manager.addSubtask(sub);

        List<Subtask> allSubs = manager.getAllSubtasks();
        assertEquals(1, allSubs.size());

        Subtask fetched = manager.getSubtask(sub.getId());
        assertNotNull(fetched);
        assertEquals("Sub1", fetched.getTitle());  // поле напрямую

        boolean deleted = manager.deleteSubtaskById(sub.getId());
        assertTrue(deleted);
        assertNull(manager.getSubtask(sub.getId()));
    }

    // ---------------- EpicHandler ----------------
    @Test
    void epicHandlerAddGetDelete() {
        Epic epic = new Epic("Epic2", "Desc2");
        manager.addEpic(epic);

        List<Epic> epics = manager.getAllEpics();
        assertEquals(1, epics.size());

        Epic fetched = manager.getEpic(epic.getId());
        assertNotNull(fetched);
        assertEquals("Epic2", fetched.getTitle());  // поле напрямую

        boolean deleted = manager.deleteEpicById(epic.getId());
        assertTrue(deleted);
        assertNull(manager.getEpic(epic.getId()));
    }

    // ---------------- HistoryHandler ----------------
    @Test
    void historyHandlerTracksTasks() {
        Task task = new Task("TaskHistory", "Desc", Status.NEW, Duration.ofMinutes(15), LocalDateTime.now());
        manager.addTask(task);
        manager.getTask(task.getId()); // обращение для истории

        List<Task> history = manager.getHistory();
        assertEquals(1, history.size());
        assertEquals("TaskHistory", history.getFirst().getTitle());  // поле напрямую
    }

    @Test
    void prioritizedHandlerSortsTasks() {
        // task1 стартует позже
        Task task1 = new Task("A", "Desc1", Status.NEW, Duration.ofMinutes(10), LocalDateTime.now().plusMinutes(20));
        manager.addTask(task1);

        // task2 стартует раньше
        Task task2 = new Task("B", "Desc2", Status.NEW, Duration.ofMinutes(5), LocalDateTime.now().plusMinutes(10));
        manager.addTask(task2);

        List<Task> prioritized = manager.getPrioritizedTasks();
        assertEquals(2, prioritized.size());
        assertEquals("B", prioritized.get(0).getTitle());
        assertEquals("A", prioritized.get(1).getTitle());
    }
}