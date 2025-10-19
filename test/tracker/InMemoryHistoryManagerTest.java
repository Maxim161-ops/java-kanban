package tracker;
import manager.InMemoryHistoryManager;
import model.Status;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для проверки корректности InMemoryHistoryManager.
 * Проверяем добавление, удаление, порядок и защиту от дубликатов.
 */
class InMemoryHistoryManagerTest {

    private InMemoryHistoryManager history;
    private Task task1;
    private Task task2;
    private Task task3;

    @BeforeEach
    void setUp() {
        history = new InMemoryHistoryManager();

        task1 = new Task("A", "Первое", Status.NEW);
        task1.setId(1);
        task2 = new Task("B", "Второе", Status.IN_PROGRESS);
        task2.setId(2);
        task3 = new Task("C", "Третье", Status.DONE);
        task3.setId(3);
    }

    @Test
    void shouldAddTasksToHistory() {
        history.add(task1);
        history.add(task2);
        history.add(task3);

        List<Task> result = history.getHistory();
        assertEquals(3, result.size(), "Должно быть 3 элемента");
        assertEquals(List.of(task1, task2, task3), result, "Порядок должен сохраняться");
    }

    @Test
    void shouldRemoveTaskById() {
        history.add(task1);
        history.add(task2);
        history.add(task3);

        history.remove(2);

        List<Task> result = history.getHistory();
        assertEquals(2, result.size(), "После удаления должно остаться 2 элемента");
        assertFalse(result.contains(task2), "Удалённая задача не должна присутствовать");
        assertEquals(List.of(task1, task3), result, "Порядок оставшихся должен сохраниться");
    }

    @Test
    void shouldRemoveFirstNodeCorrectly() {
        history.add(task1);
        history.add(task2);
        history.add(task3);

        history.remove(1);

        List<Task> result = history.getHistory();
        assertEquals(List.of(task2, task3), result, "После удаления головы порядок должен сохраниться");
    }

    @Test
    void shouldRemoveLastNodeCorrectly() {
        history.add(task1);
        history.add(task2);
        history.add(task3);

        history.remove(3);

        List<Task> result = history.getHistory();
        assertEquals(List.of(task1, task2), result, "После удаления хвоста порядок должен сохраниться");
    }

    @Test
    void shouldReturnEmptyListWhenNothingAdded() {
        assertTrue(history.getHistory().isEmpty(), "История должна быть пустой");
    }

    @Test
    void shouldNotAddNullTasks() {
        history.add(null);
        assertTrue(history.getHistory().isEmpty(), "Null не должен добавляться");
    }

    @Test
    void shouldNotDuplicateSameTask() {
        history.add(task1);
        history.add(task2);
        history.add(task1);

        List<Task> result = history.getHistory();

        assertEquals(2, result.size(), "Повторная задача не должна дублироваться");
        assertEquals(List.of(task2, task1), result, "Задача должна перемещаться в конец при повторном просмотре");
    }

    @Test
    void shouldHandleRemoveOfNonexistentIdGracefully() {
        history.add(task1);
        history.remove(99);
        List<Task> result = history.getHistory();
        assertEquals(List.of(task1), result, "Удаление несуществующего id не должно влиять на историю");
    }

    @Test
    void shouldWorkCorrectlyAfterMultipleOperations() {
        history.add(task1);
        history.add(task2);
        history.add(task3);
        history.add(task1);
        history.remove(2);

        List<Task> result = history.getHistory();

        assertEquals(2, result.size());
        assertEquals(List.of(task3, task1), result, "После серии операций порядок должен быть корректным");
    }
}