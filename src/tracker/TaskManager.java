package tracker;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private int nextId = 1;

    final private HashMap<Integer, Task> tasks = new HashMap<>();
    final private HashMap<Integer, Epic> epics = new HashMap<>();
    final private HashMap<Integer, Subtask> subtasks = new HashMap<>();

    private int generateId() {
        return nextId++;
    }

    // TASK
    public int addTask(Task task) {
        task.setId(generateId());
        tasks.put(task.getId(), task);
        return task.getId();
    }

    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public Task getTaskById(int id) {
        return tasks.get(id);
    }

    public boolean updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
            return true;
        } else {
            System.out.println("Ошибка: задача с ID " + task.getId() + " не найдена.");
            return false;
        }
    }

    public void deleteTaskById(int id) {
        tasks.remove(id);
    }

    public void deleteAllTasks() {
        tasks.clear();
    }

    // EPIC
    public int addEpic(Epic epic) {
        epic.setId(generateId());
        epics.put(epic.getId(), epic);
        return epic.getId();
    }

    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public Epic getEpicById(int id) {
        return epics.get(id);
    }

    public boolean updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            Epic oldEpic = epics.get(epic.getId());
            epic.clearSubtasks();
            for (int subId : oldEpic.getSubtaskIds()) {
                epic.addSubtask(subId);
            }
            epics.put(epic.getId(), epic);
            updateEpicStatus(epic);
            return true;
        } else {
            System.out.println("Ошибка: эпик с ID " + epic.getId() + " не найден.");
            return false;
        }
    }

    public void deleteEpicById(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (int subId : epic.getSubtaskIds()) {
                subtasks.remove(subId);
            }
        }
    }

    public void deleteAllEpics() {
        subtasks.clear();
        epics.clear();
    }

    // SUBTASK
    public int addSubtask(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpicId());

        if (epic == null) {
            System.out.println("Ошибка: эпик с ID " + subtask.getEpicId() + " не найден!");
            return -1;
        }

        subtask.setId(generateId());
        subtasks.put(subtask.getId(), subtask);
        epic.addSubtask(subtask.getId());
        updateEpicStatus(epic);
        return subtask.getId();
    }

    public ArrayList<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public Subtask getSubtaskById(int id) {
        return subtasks.get(id);
    }

    public boolean updateSubtask(Subtask subtask) {
        Subtask oldSubtask = subtasks.get(subtask.getId());

        if (oldSubtask == null) {
            System.out.println("Ошибка: подзадача с ID " + subtask.getId() + " не найдена.");
            return false;
        }

        if (oldSubtask.getEpicId() != subtask.getEpicId()) {
            System.out.println("Ошибка: нельзя менять эпик подзадачи!");
            return false;
        }

        subtasks.put(subtask.getId(), subtask);

        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            updateEpicStatus(epic);
        }

        return true;
    }

    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtask(id);
                updateEpicStatus(epic);
            }
        } else {
            System.out.println("Ошибка: подзадача с ID " + id + " не найдена.");
        }
    }

    public void deleteAllSubtasks() {
        subtasks.clear();

        for (Epic epic : epics.values()) {
            epic.clearSubtasks();
            updateEpicStatus(epic);
        }
    }

    public ArrayList<Subtask> getSubtasksByEpic(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return new ArrayList<>();

        ArrayList<Subtask> result = new ArrayList<>();
        for (int subId : epic.getSubtaskIds()) {
            Subtask sub = subtasks.get(subId);
            if (sub != null) result.add(sub);
        }
        return result;
    }

    // HELPERS
    private void updateEpicStatus(Epic epic) {
        if (epic.getSubtaskIds().isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (int id : epic.getSubtaskIds()) {
            Status status = subtasks.get(id).getStatus();
            if (status != Status.NEW) allNew = false;
            if (status != Status.DONE) allDone = false;
        }

        if (allNew) {
            epic.setStatus(Status.NEW);
        } else if (allDone) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }
}