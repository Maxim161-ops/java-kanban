package manager;

import model.Task;
import model.Epic;
import model.Subtask;
import model.Status;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    protected int nextId = 1;

    // Основные хранилища задач
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();

    // История просмотров
    protected final HistoryManager historyManager = Managers.getDefaultHistory();

    // Приоритетные задачи по времени старта
    protected final TreeSet<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(Task::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()))
    );

    // Генерация уникального ID
    protected int generateId() {
        return nextId++;
    }

    // Проверка пересечения по времени
    private boolean isIntersect(Task t1, Task t2) {
        if (t1.getStartTime() == null || t1.getEndTime() == null) return false;
        if (t2.getStartTime() == null || t2.getEndTime() == null) return false;

        return !t1.getEndTime().isBefore(t2.getStartTime()) &&
                !t1.getStartTime().isAfter(t2.getEndTime());
    }

    // Проверка пересечений новой задачи с существующими
    private void checkIntersections(Task newTask) {
        for (Task existing : prioritizedTasks) {
            if (existing.getId() == newTask.getId()) continue; // игнорируем саму себя
            if (isIntersect(existing, newTask)) {
                throw new IllegalArgumentException(
                        "Задача '" + newTask.getTitle() + "' пересекается с задачей '" + existing.getTitle() + "' по времени"
                );
            }
        }
    }

    // ------------------------- ADD -------------------------

    @Override
    public int addTask(Task task) {
        checkIntersections(task);
        int id = generateId();
        task.setId(id);

        tasks.put(id, task);
        prioritizedTasks.add(task); // добавляем в TreeSet для приоритетов

        return id;
    }

    @Override
    public int addEpic(Epic epic) {
        int id = generateId();
        epic.setId(id);
        epics.put(id, epic);
        return id;
    }

    @Override
    public int addSubtask(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) return -1;

        // Подзадача не может иметь тот же ID, что и эпик
        if (subtask.getId() == epic.getId()) return -1;

        // Проверка пересечений перед генерацией нового id
        checkIntersections(subtask);

        int id = generateId();
        subtask.setId(id);

        subtasks.put(id, subtask);
        prioritizedTasks.add(subtask); // добавляем в TreeSet

        epic.addSubtaskId(id);
        epic.updateTimeAndDuration(subtasks);
        updateEpicStatus(epic);

        return id;
    }

    // ------------------------- GET -------------------------

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        if (task != null) historyManager.add(task);
        return task;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) historyManager.add(epic);
        return epic;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) historyManager.add(subtask);
        return subtask;
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public List<Subtask> getEpicSubtasks(int epicId) {
        List<Subtask> result = new ArrayList<>();
        for (Subtask sub : subtasks.values()) {
            if (sub.getEpicId() == epicId) result.add(sub);
        }
        return result;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    // ------------------------- UPDATE -------------------------

    @Override
    public boolean updateTask(Task task) {
        Task oldTask = tasks.get(task.getId());
        if (oldTask != null) {
            prioritizedTasks.remove(oldTask);  // удаляем старую версию из TreeSet
            checkIntersections(task);
            tasks.put(task.getId(), task);
            prioritizedTasks.add(task);       // добавляем новую версию
            return true;
        }
        return false;
    }

    @Override
    public boolean updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            epics.put(epic.getId(), epic);
            updateEpicStatus(epic); // пересчитываем статус
            return true;
        }
        return false;
    }

    @Override
    public boolean updateSubtask(Subtask subtask) {
        Subtask oldSub = subtasks.get(subtask.getId());
        if (oldSub == null) return false;

        // Проверка пересечений до удаления старой версии
        checkIntersections(subtask);

        prioritizedTasks.remove(oldSub); // удаляем старую версию
        prioritizedTasks.add(subtask);   // добавляем новую

        subtasks.put(subtask.getId(), subtask);

        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.updateTimeAndDuration(subtasks); // пересчитываем время и длительность эпика
            updateEpicStatus(epic);               // пересчитываем статус эпика
        }

        return true;
    }

    // ------------------------- DELETE -------------------------

    @Override
    public void deleteAllTasks() {
        for (Task t : tasks.values()) {
            historyManager.remove(t.getId());
        }
        tasks.clear();
        prioritizedTasks.clear(); // очищаем TreeSet
    }

    @Override
    public void deleteAllEpics() {
        for (Epic epic : epics.values()) {
            for (int subId : epic.getSubtaskIds()) {
                Subtask sub = subtasks.remove(subId);
                if (sub != null) prioritizedTasks.remove(sub);
                historyManager.remove(subId);
            }
            historyManager.remove(epic.getId());
        }
        epics.clear();
        prioritizedTasks.clear(); // очищаем TreeSet
    }

    @Override
    public void deleteAllSubtasks() {
        for (Subtask sub : subtasks.values()) {
            historyManager.remove(sub.getId());
        }
        subtasks.clear();
        prioritizedTasks.clear(); // очищаем TreeSet
        for (Epic epic : epics.values()) {
            epic.clearSubtasks();
            updateEpicStatus(epic);
        }
    }

    @Override
    public boolean deleteTaskById(int id) {
        Task task = tasks.remove(id);
        if (task != null) {
            prioritizedTasks.remove(task);
            historyManager.remove(id);
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteEpicById(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (int subId : epic.getSubtaskIds()) {
                Subtask sub = subtasks.remove(subId);
                if (sub != null) prioritizedTasks.remove(sub);
                historyManager.remove(subId);
            }
            historyManager.remove(id);
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteSubtaskById(int id) {
        Subtask sub = subtasks.remove(id);
        if (sub == null) return false;

        prioritizedTasks.remove(sub);
        historyManager.remove(id);

        Epic epic = epics.get(sub.getEpicId());
        if (epic != null) {
            epic.removeSubtaskId(id);
            epic.updateTimeAndDuration(subtasks);
            updateEpicStatus(epic);
        }
        return true;
    }

    // --------------------- EPIC STATUS -------------------------

    protected void updateEpicStatus(Epic epic) {
        List<Integer> subIds = epic.getSubtaskIds();
        if (subIds.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (int id : subIds) {
            Status status = subtasks.get(id).getStatus();
            if (status != Status.NEW) allNew = false;
            if (status != Status.DONE) allDone = false;
        }

        if (allDone) epic.setStatus(Status.DONE);
        else if (allNew) epic.setStatus(Status.NEW);
        else epic.setStatus(Status.IN_PROGRESS);
    }
}