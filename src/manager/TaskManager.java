package manager;

import model.Task;
import model.Epic;
import model.Subtask;
import java.util.List;

public interface TaskManager {
    int addTask(Task task);
    int addEpic(Epic epic);
    int addSubtask(Subtask subtask);

    List<Task> getAllTasks();
    List<Epic> getAllEpics();
    List<Subtask> getAllSubtasks();

    Task getTask(int id);
    Epic getEpic(int id);
    Subtask getSubtask(int id);

    boolean updateTask(Task task);
    boolean updateEpic(Epic epic);
    boolean updateSubtask(Subtask subtask);

    void deleteAllTasks();
    void deleteAllEpics();
    void deleteAllSubtasks();

    boolean deleteTaskById(int id);
    boolean deleteEpicById(int id);
    boolean deleteSubtaskById(int id);

    List<Subtask> getEpicSubtasks(int epicId);

    List<Task> getHistory();
}