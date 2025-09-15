package tracker;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Integer> subtaskIds;

    public Epic(String title, String description) {
        super(title, description);
        this.subtaskIds = new ArrayList<>();
    }

    public List<Integer> getSubtaskIds() { return subtaskIds; }
    public void addSubtask(int subtaskId) { subtaskIds.add(subtaskId); }
    public void removeSubtask(int subtaskId) { subtaskIds.remove((Integer) subtaskId); }
    public void clearSubtasks() { subtaskIds.clear(); }

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