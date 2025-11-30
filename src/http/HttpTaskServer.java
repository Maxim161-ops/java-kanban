package http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import manager.Managers;
import manager.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {

    private final TaskManager manager;
    private HttpServer server;
    private final Gson gson;

    public HttpTaskServer(TaskManager manager) throws IOException {
        this.manager = manager;
        this.gson = new GsonBuilder().serializeNulls().create();
        this.server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/tasks", new TaskHandler(manager, gson));
        server.createContext("/subtasks", new SubtaskHandler(manager, gson));
        server.createContext("/epics", new EpicHandler(manager, gson));
        server.createContext("/history", new HistoryHandler(manager, gson));
        server.createContext("/prioritized", new PrioritizedHandler(manager, gson));
    }

    public void start() {
        server.start();
        System.out.println("HTTP-сервер запущен на порту 8080");
    }

    public void stop() {
        server.stop(1);
        System.out.println("HTTP-сервер остановлен");
    }

    public static Gson getGson() {
        return new GsonBuilder().serializeNulls().create();
    }

    public static void main(String[] args) throws IOException {
        TaskManager manager = Managers.getDefaultTaskManager();
        HttpTaskServer server = new HttpTaskServer(manager);
        server.start();
    }
}