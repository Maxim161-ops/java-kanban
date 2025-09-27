package tracker;

import manager.HistoryManager;
import manager.Managers;
import model.Task;
import model.Status;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class InMemoryHistoryManagerTest {

    @Test
    void addAndRetrieveHistory() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        Task task = new Task("HistoryTest", "Check", Status.NEW);
        task.setId(1);

        historyManager.add(task);
        List<Task> history = historyManager.getHistory();

        assertNotNull(history, "История не должна быть пустой после добавления задачи");
        assertEquals(1, history.size(), "История должна содержать одну задачу");
        assertEquals(task, history.get(0), "История должна хранить добавленную задачу");
    }
}