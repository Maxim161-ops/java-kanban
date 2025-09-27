package tracker;

import model.Task;
import model.Status;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TaskTest {

    @Test
    void tasksAreEqualIfIdIsSame() {
        Task task1 = new Task("Task1", "Desc1", Status.NEW);
        task1.setId(1);
        Task task2 = new Task("Task2", "Desc2", Status.DONE);
        task2.setId(1);

        assertEquals(task1, task2, "Задачи с одинаковым id должны быть равны");
    }
}