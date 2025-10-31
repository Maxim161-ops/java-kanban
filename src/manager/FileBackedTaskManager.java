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


import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    private void save() {
        try (Writer fileWriter = new FileWriter(file, false)) {
            // Заголовок CSV
            fileWriter.write("id,type,name,status,description,epic\n");
            // Пробежка по всем задачам
            for (Task task : getAllTasks()) {
                fileWriter.write(taskToString(task) + "\n");
            }
            for (Epic epic : getAllEpics()) {
                fileWriter.write(epicToString(epic) + "\n");
            }
            for (Subtask subtask : getAllSubtasks()) {
                fileWriter.write(subtaskToString(subtask) + "\n");
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении в файл", e);
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        try {
            List<String> lines = java.nio.file.Files.readAllLines(file.toPath()); // читаем все строки файла
            // Пропускаем первую строку (заголовок CSV)
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.isBlank()) continue; // пропускаем пустые строки

                Task task = fromString(line); // создаём задачу из строки
                if (task == null) continue;

                // В зависимости от типа задачи добавляем в нужный список
                if (task instanceof Epic) {
                    manager.restoreEpic((Epic) task);
                } else if (task instanceof Subtask) {
                    manager.restoreSubtask((Subtask) task);
                } else {
                    manager.restoreTask(task);
                }
            }
            for (Subtask subtask : manager.getAllSubtasks()) {
                Epic epic = manager.getEpic(subtask.getEpicId());
                if (epic != null) {
                    epic.addSubtaskId(subtask.getId());
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при чтении файла", e);
        }

        return manager; // возвращаем готовый менеджер
    }

    private static Task fromString(String line) {
        String[] parts = line.split(",");

        int id = Integer.parseInt(parts[0]);
        String type = parts[1];
        String title = parts[2];
        String status = parts[3];
        String description = parts[4];

        switch (type) {
            case "TASK":
                Task task = new Task(title, description, Status.valueOf(status));
                task.setId(id);
                return task;

            case "EPIC":
                Epic epic = new Epic(title, description);
                epic.setStatus(Status.valueOf(status));    // вручную ставим статус
                epic.setId(id);
                return epic;

            case "SUBTASK":
                int epicId = Integer.parseInt(parts[5]);
                Subtask subtask = new Subtask(title, description, Status.valueOf(status), epicId);
                subtask.setId(id);
                return subtask;

            default:
                return null;
        }
    }

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
        if (id >= nextId) {
            nextId = id + 1;
        }
    }


    private String taskToString(Task task) {
        return task.getId() + "," +
                TaskType.TASK + "," +
                task.getTitle() + "," +
                task.getStatus() + "," +
                task.getDescription() + ",";
    }

    private String epicToString(Epic epic) {
        return epic.getId() + "," +
                TaskType.EPIC + "," +
                epic.getTitle() + "," +
                epic.getStatus() + "," +
                epic.getDescription() + ",";
    }

    private String subtaskToString(Subtask subtask) {
        return subtask.getId() + "," +
                TaskType.SUBTASK + "," +
                subtask.getTitle() + "," +
                subtask.getStatus() + "," +
                subtask.getDescription() + "," +
                subtask.getEpicId(); // у подзадачи добавляем id эпика
    }

    //  ДОБАВЛЕНИЕ
    @Override
    public int addTask(Task task) {
        int id = super.addTask(task);    // вызываем родительский метод
        save();                          // сохраняем изменения в файл
        return id;                       // возвращаем id подзадачи
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

    //  ОБНОВЛЕНИЕ
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

    // УДАЛЕНИЕ ВСЕХ
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

    // УДАЛЕНИЕ ПО ID
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
}
