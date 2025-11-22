package model;

import java.util.ArrayList;
import java.util.List;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

public class Epic extends Task {
    private final List<Integer> subtaskIds = new ArrayList<>();

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

    public void addSubtaskId(int id) {
        subtaskIds.add(id);
    }

    public void clearSubtasks() {
        subtaskIds.clear();
    }

    public void removeSubtaskId(int id) {
        subtaskIds.remove((Integer) id);
    }

    /**
     * Обновляет duration и startTime эпика на основе всех его подзадач.
     * Duration — сумма всех подзадач.
     * startTime — самое раннее время начала.
     * endTime — время окончания последней подзадачи.
     */

    public void updateTimeAndDuration(Map<Integer, Subtask> subtaskMap) {
        if (subtaskIds.isEmpty()) {
            this.duration = Duration.ZERO;
            this.startTime = null;
            return;
        }

        Duration totalDuration = Duration.ZERO;
        LocalDateTime earliest = null;
        LocalDateTime latest = null;

        for (int subId : subtaskIds) {
            Subtask sub = subtaskMap.get(subId);  // <- здесь используем переданную карту
            if (sub == null || sub.getStartTime() == null || sub.getDuration() == null) continue;

            totalDuration = totalDuration.plus(sub.getDuration());

            if (earliest == null || sub.getStartTime().isBefore(earliest)) {
                earliest = sub.getStartTime();
            }
            if (latest == null || sub.getEndTime().isAfter(latest)) {
                latest = sub.getEndTime();
            }
        }

        this.duration = totalDuration;
        this.startTime = earliest;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", duration=" + (duration != null ? duration.toMinutes() + "min" : "null") +
                ", startTime=" + startTime +
                ", endTime=" + getEndTime() +
                ", subtaskIds=" + subtaskIds +
                '}';
    }
}