package tracker;

import manager.TaskManager;
import manager.HistoryManager;
import manager.Managers;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ManagersTest {

    @Test
    void getDefaultReturnsTaskManager() {
        TaskManager manager = Managers.getDefaultTaskManager();
        assertNotNull(manager, "Менеджер задач должен быть инициализирован");
    }

    @Test
    void getDefaultHistoryReturnsHistoryManager() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        assertNotNull(historyManager, "История должна быть инициализирована");
    }
}