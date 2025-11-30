package http;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BaseHttpHandler {

    protected void sendText(HttpExchange h, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(200, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendCreated(HttpExchange h) throws IOException {
        h.sendResponseHeaders(201, 0);
        h.close();
    }

    protected void sendNotFound(HttpExchange h) throws IOException {
        String msg = "{\"error\":\"not found\"}";
        byte[] resp = msg.getBytes(StandardCharsets.UTF_8);
        h.sendResponseHeaders(404, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendHasOverlaps(HttpExchange h) throws IOException {
        String msg = "{\"error\":\"task time intersection\"}";
        byte[] resp = msg.getBytes(StandardCharsets.UTF_8);
        h.sendResponseHeaders(406, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendServerError(HttpExchange h, String error) throws IOException {
        byte[] resp = error.getBytes(StandardCharsets.UTF_8);
        h.sendResponseHeaders(500, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected String readBody(HttpExchange h) throws IOException {
        return new String(h.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }
}