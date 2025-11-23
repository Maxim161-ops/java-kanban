package manager;

import model.Subtask;
import model.Task;
import model.Epic;
import model.TaskType;
import model.Status;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.Duration;
import java.time.LocalDateTime;

import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    // ------- SAVE -------


     // Сохраняет все задачи и историю в файл.

    protected void save() {
        try (Writer fileWriter = new FileWriter(file, false)) {
            // Заголовок CSV
            fileWriter.write("id,type,name,status,description,duration,startTime,epic\n");

            // Сохраняем задачи
            for (Task task : getAllTasks()) {
                fileWriter.write(taskToString(task) + "\n");
            }
            for (Epic epic : getAllEpics()) {
                fileWriter.write(epicToString(epic) + "\n");
            }
            for (Subtask subtask : getAllSubtasks()) {
                fileWriter.write(subtaskToString(subtask) + "\n");
            }

            // Пустая строка перед историей
            fileWriter.write("\n");

            // Сохраняем историю просмотров
            String historyLine = historyToString();
            if (!historyLine.isEmpty()) {
                fileWriter.write(historyLine);
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении в файл", e);
        }
    }

    // ------- LOAD -------


    // Загружает менеджер из файла.

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        try {
            List<String> lines = java.nio.file.Files.readAllLines(file.toPath());
            if (lines.isEmpty()) return manager;

            boolean readingHistory = false;

            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.isBlank()) {
                    readingHistory = true; // пустая строка отделяет задачи от истории
                    continue;
                }

                if (!readingHistory) {
                    // Восстанавливаем задачи
                    Task task = fromString(line);
                    if (task == null) continue;

                    switch (task.getClass().getSimpleName()) {
                        case "Task" -> manager.restoreTask(task);
                        case "Epic" -> manager.restoreEpic((Epic) task);
                        case "Subtask" -> manager.restoreSubtask((Subtask) task);
                    }
                } else {
                    // Восстанавливаем историю
                    manager.historyFromString(line);
                }
            }

            // Восстанавливаем связь Subtask -> Epic
            for (Subtask subtask : manager.getAllSubtasks()) {
                Epic epic = manager.getEpic(subtask.getEpicId());
                if (epic != null && !epic.getSubtaskIds().contains(subtask.getId())) {
                    epic.addSubtaskId(subtask.getId());
                }
            }

            // Наполняем prioritizedTasks
            for (Task task : manager.getAllTasks()) {
                manager.prioritizedTasks.add(task);
            }
            for (Subtask subtask : manager.getAllSubtasks()) {
                manager.prioritizedTasks.add(subtask);
            }

            // Пересчитываем эпики
            for (Epic epic : manager.getAllEpics()) {
                epic.updateTimeAndDuration(manager.subtasks);
                manager.updateEpicStatus(epic);
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при чтении файла", e);
        }

        return manager;
    }

    // ------- HISTORY -------

    private String historyToString() {
        List<Task> history = getHistory();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < history.size(); i++) {
            sb.append(history.get(i).getId());
            if (i < history.size() - 1) sb.append(",");
        }
        return sb.toString();
    }

    private void historyFromString(String value) {
        if (value == null || value.isEmpty()) return;

        String[] ids = value.split(",");
        for (String idStr : ids) {
            int id = Integer.parseInt(idStr);
            Task task = tasks.get(id);
            if (task == null) task = subtasks.get(id);
            if (task == null) task = epics.get(id);
            if (task != null) historyManager.add(task);
        }
    }

    // ------- FROM STRING -------

    private static Task fromString(String line) {
        String[] parts = line.split(",", -1);
        try {
            int id = Integer.parseInt(parts[0]);
            String type = parts[1];
            String title = parts[2];
            Status status = Status.valueOf(parts[3]);
            String description = parts[4];
            Duration duration = parts[5].isEmpty() ? Duration.ZERO : Duration.ofMinutes(Long.parseLong(parts[5]));
            LocalDateTime startTime = parts[6].isEmpty() ? null : LocalDateTime.parse(parts[6]);

            return switch (type) {
                case "TASK" -> {
                    Task task = new Task(title, description, status, duration, startTime);
                    task.setId(id);
                    yield task;
                }
                case "EPIC" -> {
                    Epic epic = new Epic(title, description, status, duration, startTime);
                    epic.setId(id);
                    yield epic;
                }
                case "SUBTASK" -> {
                    int epicId = Integer.parseInt(parts[7]);
                    Subtask subtask = new Subtask(title, description, status, duration, startTime, epicId);
                    subtask.setId(id);
                    yield subtask;
                }
                default -> null;
            };
        } catch (Exception e) {
            System.out.println("Ошибка при чтении строки из файла: " + line);
            return null;
        }
    }

    // ------- RESTORE HELPERS -------

    protected void restoreTask(Task task) {
        tasks.put(task.getId(), task);
        updateNextId(task.getId());
    }

    protected void restoreEpic(Epic epic) {
        epics.put(epic.getId(), epic);
        updateNextId(epic.getId());
    }

    protected void restoreSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        updateNextId(subtask.getId());
    }

    protected void updateNextId(int id) {
        if (id >= nextId) nextId = id + 1;
    }

    // ------- TO STRING HELPERS -------

    private String taskToString(Task task) {
        return String.join(",",
                String.valueOf(task.getId()),
                TaskType.TASK.name(),
                task.getTitle(),
                task.getStatus().name(),
                task.getDescription(),
                task.getDuration() == null ? "" : String.valueOf(task.getDuration().toMinutes()),
                task.getStartTime() == null ? "" : task.getStartTime().toString(),
                ""
        );
    }

    private String epicToString(Epic epic) {
        return String.join(",",
                String.valueOf(epic.getId()),
                TaskType.EPIC.name(),
                epic.getTitle(),
                epic.getStatus().name(),
                epic.getDescription(),
                epic.getDuration() == null ? "" : String.valueOf(epic.getDuration().toMinutes()),
                epic.getStartTime() == null ? "" : epic.getStartTime().toString(),
                ""
        );
    }

    private String subtaskToString(Subtask sub) {
        return String.join(",",
                String.valueOf(sub.getId()),
                TaskType.SUBTASK.name(),
                sub.getTitle(),
                sub.getStatus().name(),
                sub.getDescription(),
                sub.getDuration() == null ? "" : String.valueOf(sub.getDuration().toMinutes()),
                sub.getStartTime() == null ? "" : sub.getStartTime().toString(),
                String.valueOf(sub.getEpicId())
        );
    }

    // ------- OVERRIDDEN CRUD -------

    @Override
    public int addTask(Task task) {
        int id = super.addTask(task);
        save();
        return id;
    }

    @Override
    public int addEpic(Epic epic) {
        int id = super.addEpic(epic);
        save();
        return id;
    }

    @Override
    public int addSubtask(Subtask subtask) {
        int id = super.addSubtask(subtask);
        save();
        return id;
    }

    @Override
    public boolean updateTask(Task task) {
        boolean success = super.updateTask(task);
        save();
        return success;
    }

    @Override
    public boolean updateEpic(Epic epic) {
        boolean success = super.updateEpic(epic);
        save();
        return success;
    }

    @Override
    public boolean updateSubtask(Subtask subtask) {
        boolean success = super.updateSubtask(subtask);
        save();
        return success;
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    @Override
    public boolean deleteTaskById(int id) {
        boolean success = super.deleteTaskById(id);
        save();
        return success;
    }

    @Override
    public boolean deleteEpicById(int id) {
        boolean success = super.deleteEpicById(id);
        save();
        return success;
    }

    @Override
    public boolean deleteSubtaskById(int id) {
        boolean success = super.deleteSubtaskById(id);
        save();
        return success;
    }

    @Override
    public Task getTask(int id) {
        Task task = super.getTask(id);
        save();
        return task;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = super.getEpic(id);
        save();
        return epic;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = super.getSubtask(id);
        save();
        return subtask;
    }
}
