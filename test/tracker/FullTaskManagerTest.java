package tracker;

import manager.InMemoryTaskManager;
import manager.FileBackedTaskManager;
import model.Task;
import model.Subtask;
import model.Epic;
import model.Status;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FullTaskManagerTest {

    private InMemoryTaskManager inMemoryManager;
    private FileBackedTaskManager fileManager;
    private File file;

    @BeforeEach
    void setUp() {
        inMemoryManager = new InMemoryTaskManager();
        file = new File("test_tasks.csv");
        fileManager = new FileBackedTaskManager(file);
    }

    @AfterEach
    void tearDown() {
        if (file.exists()) file.delete();
    }

    // InMemoryTaskManager tests
    @Test
    void shouldAddAndRetrieveTask() {
        Task task = new Task(
                "Task1", "Desc1", Status.NEW,
                Duration.ofMinutes(60),
                LocalDateTime.of(2025, 1, 1, 10, 0)
        );
        int id = inMemoryManager.addTask(task);

        Task saved = inMemoryManager.getTask(id);
        assertEquals(task, saved);
    }

    @Test
    void shouldPreventOverlappingTasks() {
        Task t1 = new Task(
                "Task1", "D1", Status.NEW,
                Duration.ofMinutes(60),
                LocalDateTime.of(2025, 1, 1, 10, 0)
        );
        inMemoryManager.addTask(t1);

        Task t2 = new Task(
                "Task2", "D2", Status.NEW,
                Duration.ofMinutes(30),
                LocalDateTime.of(2025, 1, 1, 10, 30)
        );

        assertThrows(IllegalArgumentException.class,
                () -> inMemoryManager.addTask(t2));
    }

    // Epic & Subtask tests
    @Test
    void epicAndSubtasksCRUD() {
        Epic epic = new Epic("Epic1", "EpicDesc");
        int epicId = inMemoryManager.addEpic(epic);

        Subtask sub1 = new Subtask(
                "Sub1", "SubDesc1", Status.NEW,
                Duration.ofMinutes(30),
                LocalDateTime.of(2025, 1, 1, 11, 0),
                epicId
        );
        int subId = inMemoryManager.addSubtask(sub1);

        Epic retrievedEpic = inMemoryManager.getEpic(epicId);
        Subtask retrievedSub = inMemoryManager.getSubtask(subId);

        assertNotNull(retrievedSub);
        assertEquals(sub1, retrievedSub);
        assertEquals(epicId, retrievedSub.getEpicId());

        List<Integer> subIds = retrievedEpic.getSubtaskIds();
        assertEquals(1, subIds.size());
        assertEquals(subId, subIds.get(0));
    }

    @Test
    void shouldNotAllowSubtaskWithSameIdAsEpic() {
        Epic epic = new Epic("Epic", "Desc");
        int epicId = inMemoryManager.addEpic(epic);

        Subtask sub = new Subtask(
                "Sub", "Desc", Status.NEW,
                Duration.ofMinutes(30),
                LocalDateTime.of(2025, 1, 1, 12, 0),
                epicId
        );
        sub.setId(epicId);

        int result = inMemoryManager.addSubtask(sub);
        assertEquals(-1, result);
    }

    // FileBackedTaskManager tests
    @Test
    void shouldPersistAndLoadTasks() {
        Task task = new Task(
                "Task1", "Desc1", Status.NEW,
                Duration.ofMinutes(60),
                LocalDateTime.of(2025, 1, 1, 10, 0)
        );
        fileManager.addTask(task);

        Epic epic = new Epic("Epic1", "EpicDesc");
        int epicId = fileManager.addEpic(epic);

        // ВАЖНО: подзадача НЕ ПЕРЕСЕКАЕТСЯ по времени с task
        Subtask sub = new Subtask(
                "Sub1", "SubDesc", Status.NEW,
                Duration.ofMinutes(30),
                LocalDateTime.of(2025, 1, 1, 12, 0),
                epicId
        );
        fileManager.addSubtask(sub);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        assertEquals(1, loaded.getAllTasks().size());
        assertEquals(1, loaded.getAllEpics().size());
        assertEquals(1, loaded.getAllSubtasks().size());
    }

    @Test
    void shouldPreserveTaskHistory() {
        Task task = new Task(
                "Task1", "Desc1", Status.NEW,
                Duration.ofMinutes(60),
                LocalDateTime.of(2025, 1, 1, 10, 0)
        );
        int id = fileManager.addTask(task);

        // Добавляем в историю
        fileManager.getTask(id);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);
        List<Task> history = loaded.getHistory();

        assertEquals(1, history.size());
        assertEquals(task, history.get(0));
    }

    // HistoryManager tests
    @Test
    void historyDoesNotDuplicate() {
        Task t1 = new Task(
                "T1", "D1", Status.NEW,
                Duration.ofMinutes(60),
                LocalDateTime.of(2025, 1, 1, 10, 0)
        );
        int id = inMemoryManager.addTask(t1);

        inMemoryManager.getTask(id);
        inMemoryManager.getTask(id);

        List<Task> hist = inMemoryManager.getHistory();

        assertEquals(1, hist.size());
    }

    @Test
    void removingTaskShouldRemoveFromHistory() {
        Task t1 = new Task(
                "T1", "D1", Status.NEW,
                Duration.ofMinutes(60),
                LocalDateTime.of(2025, 1, 1, 10, 0)
        );
        Task t2 = new Task(
                "T2", "D2", Status.NEW,
                Duration.ofMinutes(30),
                LocalDateTime.of(2025, 1, 1, 12, 0)
        );

        int id1 = inMemoryManager.addTask(t1);
        int id2 = inMemoryManager.addTask(t2);

        inMemoryManager.getTask(id1);
        inMemoryManager.getTask(id2);

        // Правильный метод удаления
        inMemoryManager.deleteTaskById(id1);

        List<Task> hist = inMemoryManager.getHistory();
        assertEquals(1, hist.size());
        assertEquals(t2, hist.get(0));
    }

    // Time calculations
    @Test
    void endTimeCalculationIsCorrect() {
        Task t = new Task(
                "T", "D", Status.NEW,
                Duration.ofMinutes(45),
                LocalDateTime.of(2025, 1, 1, 10, 0)
        );

        assertEquals(
                LocalDateTime.of(2025,1,1,10,45),
                t.getEndTime()
        );
    }
}

