package tracker;

import manager.FileBackedTaskManager;

import model.Task;
import model.Epic;
import model.Subtask;
import model.Status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest {

    private File file;
    private FileBackedTaskManager manager;

    @BeforeEach
    void setUp() {
        file = new File("test_tasks.csv");
        manager = new FileBackedTaskManager(file);
    }

    @AfterEach
    void tearDown() {
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    void shouldSaveAndLoadTasksFromFile() {
        // Подготовка времени
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 10, 0);

        // Создаём задачи
        Task task = new Task(
                "Task 1",
                "Description 1",
                Status.NEW,
                Duration.ofMinutes(30),
                start
        );

        Epic epic = new Epic("Epic 1", "Epic Description");

        Subtask subtask = new Subtask(
                "Subtask 1",
                "Subtask Description",
                Status.NEW,
                Duration.ofMinutes(20),
                start.plusHours(1),
                2 // epic получит id=2
        );

        // Добавляем в менеджер
        manager.addTask(task);
        manager.addEpic(epic);
        manager.addSubtask(subtask);

        // Файл должен быть создан
        assertTrue(file.exists(), "Файл должен быть создан после сохранения");

        // Загружаем обратно
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        assertEquals(1, loadedManager.getAllTasks().size(), "Должна быть одна задача");
        assertEquals(1, loadedManager.getAllEpics().size(), "Должен быть один эпик");
        assertEquals(1, loadedManager.getAllSubtasks().size(), "Должна быть одна подзадача");

        // Проверяем Task
        Task loadedTask = loadedManager.getAllTasks().get(0);
        assertEquals("Task 1", loadedTask.getTitle());
        assertEquals("Description 1", loadedTask.getDescription());
        assertEquals(Status.NEW, loadedTask.getStatus());
        assertEquals(Duration.ofMinutes(30), loadedTask.getDuration());
        assertEquals(start, loadedTask.getStartTime());

        // Проверяем Epic
        Epic loadedEpic = loadedManager.getAllEpics().get(0);
        assertEquals("Epic 1", loadedEpic.getTitle());
        assertEquals("Epic Description", loadedEpic.getDescription());

        // Проверяем Subtask
        Subtask loadedSubtask = loadedManager.getAllSubtasks().get(0);
        assertEquals("Subtask 1", loadedSubtask.getTitle());
        assertEquals("Subtask Description", loadedSubtask.getDescription());
        assertEquals(Status.NEW, loadedSubtask.getStatus());
        assertEquals(Duration.ofMinutes(20), loadedSubtask.getDuration());
        assertEquals(start.plusHours(1), loadedSubtask.getStartTime());
        assertEquals(loadedEpic.getId(), loadedSubtask.getEpicId());
    }

    @Test
    void shouldHandleEmptyFileGracefully() {
        try {
            file.createNewFile();
        } catch (Exception e) {
            fail("Не удалось создать пустой файл");
        }

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        assertTrue(loadedManager.getAllTasks().isEmpty(), "Список задач должен быть пустым");
        assertTrue(loadedManager.getAllEpics().isEmpty(), "Список эпиков должен быть пустым");
        assertTrue(loadedManager.getAllSubtasks().isEmpty(), "Список подзадач должен быть пустым");
    }
}