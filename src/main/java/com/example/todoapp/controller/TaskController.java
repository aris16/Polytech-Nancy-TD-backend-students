package com.example.todoapp.controller;

import com.example.todoapp.business.model.Task;
import com.example.todoapp.service.TaskService;
import com.example.todoapp.JsonUtils;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.nonNull;

public class TaskController {

    private static final Pattern ID_PATH = Pattern.compile("^/tasks/([0-9]+)$");
    private static final TaskService service = new TaskService();

    /**
     * Gestion des requêtes HTTP.
     * @param exchange Représente l'échange HTTP (requête du client et réponse du serveur).
     * @throws IOException Si une erreur de Input/Output.
     */
    public static void handleTasks(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            // region Manage POST /tasks
            if ("POST".equals(method) && "/tasks".equals(path)) {
                Task input = JsonUtils.deserialize(new String(exchange.getRequestBody().readAllBytes(), UTF_8), Task.class);
                Task createdTask = service.save(input);

                exchange.getResponseHeaders().add("Location", "/tasks/" + createdTask.id());
                sendResponse(exchange, 201, JsonUtils.serialize(createdTask));
                return;
            }
            // endregion

            // region Manage GET /tasks
            if ("GET".equals(method) && "/tasks".equals(path)) {
                String query = exchange.getRequestURI().getQuery();
                boolean todoOnly = query != null && query.contains("todo-only=true");

                List<Task> listeTODO = service.getFilteredTasks(todoOnly);

                if (listeTODO.isEmpty()) {
                    sendResponse(exchange, 204, null);
                }
                else {
                    sendResponse(exchange, 200, JsonUtils.serialize(listeTODO));
                }
                return;
            }
            // endregion

            Matcher m = ID_PATH.matcher(path);

            // region Manage GET /tasks/{id}
            if ("GET".equals(method) && m.matches()) {
                int id = Integer.parseInt(m.group(1));
                Optional<Task> task = service.findById(id);

                if (task.isPresent()) {
                    sendResponse(exchange, 200, JsonUtils.serialize(task.get()));
                }
                else {
                    sendResponse(exchange, 404, null);
                }
                return;
            }
            // endregion

            // region Manage DELETE /tasks/{id}
            if ("DELETE".equals(method) && m.matches()) {
                int id = Integer.parseInt(m.group(1));
                boolean deleted = service.delete(id);

                if (deleted) {
                    sendResponse(exchange, 204, null);
                }
                else {
                    sendResponse(exchange, 404, null);
                }
                return;
            }
            // endregion

            // region Manage PUT /tasks/{id}
            if ("PUT".equals(method) && m.matches()) {
                int id = Integer.parseInt(m.group(1));
                Task input = JsonUtils.deserialize(new String(exchange.getRequestBody().readAllBytes(), UTF_8), Task.class);
                boolean updated = service.update(id, input);

                if (updated) {
                    sendResponse(exchange, 204, null);
                }
                else {
                    sendResponse(exchange, 404, null);
                }
                return;
            }
            // endregion

            sendResponse(exchange, 404, null);
        }
        catch (Exception e) {
            System.err.println("Erreur : " + e.getMessage());
            sendResponse(exchange, 500, null);
            throw new RuntimeException(e);
        }
    }

    private static void sendResponse(HttpExchange exchange, int status, String json) throws IOException {
        if (nonNull(json)) {
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            byte[] bytes = json.getBytes(UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } else {
            exchange.sendResponseHeaders(status, 0);
            exchange.close();
        }
    }
}