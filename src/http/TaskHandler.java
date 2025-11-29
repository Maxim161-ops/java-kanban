package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import model.Task;

import java.io.IOException;
import java.util.List;

public class TaskHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager manager;
    private final Gson gson;

    public TaskHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange h) throws IOException {
        try {
            String method = h.getRequestMethod();
            String path = h.getRequestURI().getPath();
            String query = h.getRequestURI().getQuery();

            if (method.equals("GET")) {
                if (query == null) {
                    List<Task> tasks = manager.getAllTasks();
                    sendText(h, gson.toJson(tasks));
                } else {
                    int id = Integer.parseInt(query.split("=")[1]);
                    Task t = manager.getTask(id);
                    if (t == null) {
                        sendNotFound(h);
                    } else {
                        sendText(h, gson.toJson(t));
                    }
                }
                return;
            }

            if (method.equals("POST")) {
                String body = readBody(h);
                Task task = gson.fromJson(body, Task.class);

                if (task.getId() == 0) {
                    try {
                        manager.addTask(task);
                        sendCreated(h);
                    } catch (IllegalArgumentException e) {
                        sendHasOverlaps(h);
                    }
                } else {
                    try {
                        boolean ok = manager.updateTask(task);
                        if (!ok) { sendNotFound(h); return; }
                        sendCreated(h);
                    } catch (IllegalArgumentException e) {
                        sendHasOverlaps(h);
                    }
                }
                return;
            }

            if (method.equals("DELETE")) {
                if (query == null) {
                    manager.deleteAllTasks();
                    sendCreated(h);
                } else {
                    int id = Integer.parseInt(query.split("=")[1]);
                    boolean ok = manager.deleteTaskById(id);
                    if (!ok) {
                        sendNotFound(h);
                    } else {
                        sendCreated(h);
                    }
                }
            }

        } catch (Exception e) {
            sendServerError(h, e.getMessage());
        }
    }
}