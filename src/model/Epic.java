package model;

import java.util.ArrayList;
import java.util.List;
import java.time.Duration;
import java.time.LocalDateTime;

public class Epic extends Task {
    private final List<Integer> subtaskIds = new ArrayList<>();
    private LocalDateTime endTime;

    /// Конструктор по умолчанию (создаёт эпик со статусом NEW)
    public Epic(String title, String description) {
        super(title, description, Status.NEW, Duration.ZERO, null);
    }

    //конструктор для загрузки из файла (принимает статус)
    public Epic(String title, String description, Status status, Duration duration, LocalDateTime startTime) {
        super(title, description, status, duration, startTime);
    }

    public List<Integer> getSubtaskIds() {
        return new ArrayList<>(subtaskIds);
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void addSubtaskId(int id) {
        subtaskIds.add(id);
    }

    public void clearSubtasks() {
        subtaskIds.clear();
    }

    public void removeSubtaskId(int id) {
        subtaskIds.remove((Integer) id);
    }
}