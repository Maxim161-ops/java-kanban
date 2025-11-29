package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;

import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager manager;
    private final Gson gson;

    public HistoryHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange h) throws IOException {
        try {
            if (!h.getRequestMethod().equals("GET")) {
                sendServerError(h, "only GET allowed");
                return;
            }

            sendText(h, gson.toJson(manager.getHistory()));

        } catch (Exception e) {
            sendServerError(h, e.getMessage());
        }
    }
}