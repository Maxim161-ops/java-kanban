package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import model.Epic;
import model.Subtask;

import java.io.IOException;
import java.util.List;

public class EpicHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager manager;
    private final Gson gson;

    public EpicHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange h) throws IOException {
        try {
            String method = h.getRequestMethod();
            String query = h.getRequestURI().getQuery();
            String path = h.getRequestURI().getPath();

            switch (method) {
                case "GET" -> {

                    if (path.endsWith("/epics/subtasks")) {
                        int epicId = Integer.parseInt(query.split("=")[1]);
                        List<Subtask> list = manager.getEpicSubtasks(epicId);
                        sendText(h, gson.toJson(list));
                        return;
                    }

                    if (query == null) {
                        sendText(h, gson.toJson(manager.getAllEpics()));
                    } else {
                        int id = Integer.parseInt(query.split("=")[1]);
                        Epic epic = manager.getEpic(id);
                        if (epic == null) {
                            sendNotFound(h);
                            return;
                        }
                        sendText(h, gson.toJson(epic));
                    }
                    return;
                }
                case "POST" -> {
                    String body = readBody(h);
                    Epic epic = gson.fromJson(body, Epic.class);

                    if (epic.getId() == 0) {
                        manager.addEpic(epic);
                        sendCreated(h);
                    } else {
                        boolean ok = manager.updateEpic(epic);
                        if (!ok) {
                            sendNotFound(h);
                            return;
                        }
                        sendCreated(h);
                    }
                    return;
                }
                case "DELETE" -> {
                    if (query == null) {
                        manager.deleteAllEpics();
                        sendCreated(h);
                    } else {
                        int id = Integer.parseInt(query.split("=")[1]);
                        boolean ok = manager.deleteEpicById(id);
                        if (!ok) {
                            sendNotFound(h);
                            return;
                        }
                        sendCreated(h);
                    }
                }
            }

        } catch (Exception e) {
            sendServerError(h, e.getMessage());
        }
    }
}