package manager;

import model.Task;
import model.Epic;
import model.Subtask;
import model.Status;

import java.time.Duration;
import java.time.LocalDateTime;
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
        if (newTask.getStartTime() == null || newTask.getEndTime() == null) return; // если времени нет, пересечения нет

        for (Task existing : prioritizedTasks) {
            if (existing.getId() == newTask.getId()) continue;
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
        prioritizedTasks.add(task);
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

        if (subtask.getEpicId() == subtask.getId()) return -1;

        checkIntersections(subtask);

        int id = generateId();
        subtask.setId(id);

        subtasks.put(id, subtask);
        prioritizedTasks.add(subtask);

        epic.addSubtaskId(id);
        updateEpicTime(epic);
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
        Subtask sub = subtasks.get(id);
        if (sub != null) historyManager.add(sub);
        return sub;
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
            prioritizedTasks.remove(oldTask);
            checkIntersections(task);
            tasks.put(task.getId(), task);
            prioritizedTasks.add(task);
            return true;
        }
        return false;
    }

    @Override
    public boolean updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            epics.put(epic.getId(), epic);
            updateEpicTime(epic);    // пересчитываем время
            updateEpicStatus(epic);  // пересчитываем статус
            return true;
        }
        return false;
    }

    @Override
    public boolean updateSubtask(Subtask subtask) {
        Subtask oldSub = subtasks.get(subtask.getId());
        if (oldSub == null) return false;

        checkIntersections(subtask);

        prioritizedTasks.remove(oldSub);
        subtasks.put(subtask.getId(), subtask);
        prioritizedTasks.add(subtask);

        Epic epic = epics.get(subtask.getEpicId());
        updateEpicTime(epic);
        updateEpicStatus(epic);

        return true;
    }

    // ------------------------- DELETE -------------------------

    @Override
    public void deleteAllTasks() {
        for (Task t : tasks.values()) {
            prioritizedTasks.remove(t);   // удаляем только обычные задачи
            historyManager.remove(t.getId());
        }
        tasks.clear();
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
    }

    @Override
    public void deleteAllSubtasks() {
        // Удаляем подзадачи из истории и TreeSet
        for (Subtask sub : subtasks.values()) {
            historyManager.remove(sub.getId());
            prioritizedTasks.remove(sub); // удаляем только подзадачу
        }

        // Очищаем коллекцию подзадач
        subtasks.clear();

        // Обновляем эпики: очищаем списки подзадач, пересчитываем время и статус
        for (Epic epic : epics.values()) {
            epic.clearSubtasks();
            updateEpicTime(epic);     // пересчитываем время
            updateEpicStatus(epic);   // пересчитываем статус
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
            for (Integer subId : epic.getSubtaskIds()) {
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
        epic.removeSubtaskId(id);

        updateEpicTime(epic);
        updateEpicStatus(epic);

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

        for (Integer id : subIds) {
            Status status = subtasks.get(id).getStatus();
            if (status != Status.NEW) allNew = false;
            if (status != Status.DONE) allDone = false;
        }

        if (allDone) epic.setStatus(Status.DONE);
        else if (allNew) epic.setStatus(Status.NEW);
        else epic.setStatus(Status.IN_PROGRESS);
    }

    // ------------------------- EPIC TIME -------------------------

    protected void updateEpicTime(Epic epic) {
        List<Integer> subIds = epic.getSubtaskIds();

        if (subIds.isEmpty()) {
            epic.setStartTime(null);
            epic.setEndTime(null);
            epic.setDuration(Duration.ZERO);
            return;
        }

        LocalDateTime start = null;
        LocalDateTime end = null;
        Duration duration = Duration.ZERO;

        for (Integer id : subIds) {
            Subtask sub = subtasks.get(id);
            if (sub == null) continue;

            if (sub.getStartTime() != null) {
                if (start == null || sub.getStartTime().isBefore(start)) {
                    start = sub.getStartTime();
                }
            }

            if (sub.getEndTime() != null) {
                if (end == null || sub.getEndTime().isAfter(end)) {
                    end = sub.getEndTime();
                }
            }

            if (sub.getDuration() != null) {
                duration = duration.plus(sub.getDuration());
            }
        }

        epic.setStartTime(start);
        epic.setEndTime(end);
        epic.setDuration(duration);
    }
}
