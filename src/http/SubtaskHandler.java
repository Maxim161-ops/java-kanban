package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import model.Subtask;

import java.io.IOException;
import java.util.List;

public class SubtaskHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager manager;
    private final Gson gson;

    public SubtaskHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange h) throws IOException {
        try {
            String method = h.getRequestMethod();
            String query = h.getRequestURI().getQuery();

            if (method.equals("GET")) {
                if (query == null) {
                    List<Subtask> all = manager.getAllSubtasks();
                    sendText(h, gson.toJson(all));
                } else {
                    int id = Integer.parseInt(query.split("=")[1]);
                    Subtask sub = manager.getSubtask(id);
                    if (sub == null) { sendNotFound(h);
                        return;
                    }
                    sendText(h, gson.toJson(sub));
                }
                return;
            }

            if (method.equals("POST")) {
                String body = readBody(h);
                Subtask sub = gson.fromJson(body, Subtask.class);

                if (sub.getId() == 0) {
                    try {
                        manager.addSubtask(sub);
                        sendCreated(h);
                    } catch (IllegalArgumentException e) {
                        sendHasOverlaps(h);
                    }
                } else {
                    try {
                        boolean ok = manager.updateSubtask(sub);
                        if (!ok) { sendNotFound(h);
                            return;
                        }
                        sendCreated(h);
                    } catch (IllegalArgumentException e) {
                        sendHasOverlaps(h);
                    }
                }
                return;
            }

            if (method.equals("DELETE")) {
                if (query == null) {
                    manager.deleteAllSubtasks();
                    sendCreated(h);
                } else {
                    int id = Integer.parseInt(query.split("=")[1]);
                    boolean ok = manager.deleteSubtaskById(id);
                    if (!ok) { sendNotFound(h);
                        return;
                    }
                    sendCreated(h);
                }
            }
        } catch (Exception e) {
            sendServerError(h, e.getMessage());
        }
    }
}