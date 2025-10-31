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
        // Создаём задачи
        Task task = new Task("Task 1", "Description 1", Status.NEW);
        Epic epic = new Epic("Epic 1", "Epic Description");
        Subtask subtask = new Subtask("Subtask 1", "Subtask Description", Status.NEW, 2); // epic будет id=2

        // Добавляем задачи
        manager.addTask(task);
        manager.addEpic(epic);
        manager.addSubtask(subtask);

        // Проверяем, что файл создался
        assertTrue(file.exists(), "Файл должен быть создан после сохранения");

        // Загружаем новый менеджер из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        // Проверяем, что всё восстановилось
        assertEquals(1, loadedManager.getAllTasks().size(), "Должна быть одна задача");
        assertEquals(1, loadedManager.getAllEpics().size(), "Должен быть один эпик");
        assertEquals(1, loadedManager.getAllSubtasks().size(), "Должна быть одна подзадача");

        // Проверяем конкретные поля
        Task loadedTask = loadedManager.getAllTasks().get(0);
        assertEquals("Task 1", loadedTask.getTitle());
        assertEquals("Description 1", loadedTask.getDescription());
        assertEquals(Status.NEW, loadedTask.getStatus());

        Epic loadedEpic = loadedManager.getAllEpics().get(0);
        assertEquals("Epic 1", loadedEpic.getTitle());
        assertEquals("Epic Description", loadedEpic.getDescription());

        Subtask loadedSubtask = loadedManager.getAllSubtasks().get(0);
        assertEquals("Subtask 1", loadedSubtask.getTitle());
        assertEquals("Subtask Description", loadedSubtask.getDescription());
        assertEquals(Status.NEW, loadedSubtask.getStatus());
        assertEquals(loadedEpic.getId(), loadedSubtask.getEpicId(), "EpicId должен совпадать");
    }

    @Test
    void shouldHandleEmptyFileGracefully() {
        // Создаём пустой файл
        try {
            file.createNewFile();
        } catch (Exception e) {
            fail("Не удалось создать пустой файл");
        }

        // Загружаем менеджер из пустого файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        assertTrue(loadedManager.getAllTasks().isEmpty(), "Список задач должен быть пустым");
        assertTrue(loadedManager.getAllEpics().isEmpty(), "Список эпиков должен быть пустым");
        assertTrue(loadedManager.getAllSubtasks().isEmpty(), "Список подзадач должен быть пустым");
    }
}