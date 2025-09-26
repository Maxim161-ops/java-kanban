package tracker;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest {

    @Test
    void addAndFindTasksById() {
        InMemoryTaskManager manager = new InMemoryTaskManager();
        Task task = new Task("Task", "Desc", Status.NEW);
        int id = manager.addTask(task);

        Task saved = manager.getTask(id);
        assertNotNull(saved, "Задача должна быть найдена по id");
        assertEquals(task, saved, "Добавленная и найденная задача должны совпадать");
    }

    @Test
    void idsDoNotConflict() {
        InMemoryTaskManager manager = new InMemoryTaskManager();

        Task task1 = new Task("Task1", "Desc1", Status.NEW);
        task1.setId(100); // вручную
        manager.addTask(task1);

        Task task2 = new Task("Task2", "Desc2", Status.NEW);
        int id2 = manager.addTask(task2);

        assertNotEquals(task1.getId(), id2,
                "Сгенерированные id и ручные id не должны конфликтовать");
    }

    @Test
    void taskImmutabilityOnAdd() {
        InMemoryTaskManager manager = new InMemoryTaskManager();
        Task task = new Task("Immutable", "Desc", Status.NEW);
        int id = manager.addTask(task);

        Task saved = manager.getTask(id);

        assertEquals(task.getTitle(), saved.getTitle(), "Заголовок должен совпадать");
        assertEquals(task.getDescription(), saved.getDescription(), "Описание должно совпадать");
        assertEquals(task.getStatus(), saved.getStatus(), "Статус должен совпадать");
    }
}