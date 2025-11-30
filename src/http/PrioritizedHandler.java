package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import model.Task;

import java.io.IOException;
import java.util.List;

public class PrioritizedHandler extends BaseHttpHandler implements HttpHandler {

    private final InMemoryTaskManager manager;
    private final Gson gson;

    public PrioritizedHandler(TaskManager manager, Gson gson) {
        this.manager = (InMemoryTaskManager) manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange h) throws IOException {
        try {
            if (!h.getRequestMethod().equals("GET")) {
                sendServerError(h, "only GET allowed");
                return;
            }

            List<Task> sorted = manager.getPrioritizedTasks();
            sendText(h, gson.toJson(sorted));

        } catch (Exception e) {
            sendServerError(h, e.getMessage());
        }
    }
}