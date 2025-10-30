package model;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final List<Integer> subtaskIds = new ArrayList<>();

    /// Конструктор по умолчанию (создаёт эпик со статусом NEW)
    public Epic(String title, String description) {
        super(title, description, Status.NEW);
    }

    //конструктор для загрузки из файла (принимает статус)
    public Epic(String title, String description, Status status) {
        super(title, description, status);
    }

    public List<Integer> getSubtaskIds() {
        return new ArrayList<>(subtaskIds);
    }

    public void addSubtaskId(int id) {
        subtaskIds.add(id);
    }

    public void clearSubtasks() {
        subtaskIds.clear();
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", subtaskIds=" + subtaskIds +
                '}';
    }
}