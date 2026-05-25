package com.example.todoapp;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.nonNull;

/**
 * Main class of the application. Managing routing and HTTP layer.
 */
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);
    private static final Pattern ID_PATH = Pattern.compile("^/tasks/([0-9]+)$");
    private static final TaskDao dao = new TaskDao();

    public static void main(String[] args) throws Exception {
        log.info("In-memory repository initialised");

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/tasks", Application::handleTasks);
        server.setExecutor(null);
        server.start();
        log.info("HTTP server started on http://localhost:8080");
    }

    private static void handleTasks(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod(); //GET, POST, PUT, DELETE
        String path = exchange.getRequestURI().getPath(); // /tasks par exemple

        //region Manage POST /tasks
        if ("POST".equals(method) && "/tasks".equals(path)) {
            Task input = JsonUtils.deserialize(new String(exchange.getRequestBody().readAllBytes(), UTF_8), Task.class); //body
            Task createdTask = dao.save(input); //enregistrement dans storage

            exchange.getResponseHeaders().add("Location", "/tasks/" + createdTask.id());
            sendResponse(exchange, 201, JsonUtils.serialize(createdTask));
            return;
        }
        //endregion

        //region Manage GET /tasks
        if ("GET".equals(method) && "/tasks".equals(path)) {
            String query = exchange.getRequestURI().getQuery(); //prend tout après le ? dans l'URL
            boolean todoOnly = query != null && query.contains("todo-only=true");
            Map<Integer, Task> allTasks = dao.findAll(); //prendre toutes les taches
            List<Task> listeTODO = new ArrayList<>(); //definir une liste qui contiendera toute les taches à faire

            // parcourir toutes les tâches
            for (Task task : allTasks.values()) {
                // filtrage
                if (todoOnly) {
                    // garder seulement les tâches non terminées
                    if (!task.done()) {
                        listeTODO.add(task);
                    }
                }
                else {
                    // garder toutes les tâches
                    listeTODO.add(task);
                }
            }

            if (listeTODO.isEmpty()) {
                sendResponse(exchange, 204, null);
            } else {
                sendResponse(exchange, 200, JsonUtils.serialize(listeTODO));
            }
            return;
        }
        //endregion

        //region Manage GET /tasks/{id}
        Matcher m = ID_PATH.matcher(path);
        if ("GET".equals(method) && m.matches()) {
            int id = Integer.parseInt(m.group(1));
            Optional<Task> task = dao.findById(id);

            if (task.isPresent()) {
                sendResponse(exchange, 200, JsonUtils.serialize(task.get())); // si la tache existe bien on la transcrit en JSON
            }
            else {
                sendResponse(exchange, 404, null);
            }
            return;
        }
        //endregion

        //region Manage DELETE /tasks
        if ("DELETE".equals(method) && m.matches()) {
            int id = Integer.parseInt(m.group(1));
            boolean deleted = dao.delete(id);

            if (deleted) {
                sendResponse(exchange, 204, null);
            }
            else {
                sendResponse(exchange, 404, null);
            }
            return;
        }
        //endregion

        //region Manage PUT /tasks
        if ("PUT".equals(method) && m.matches()) {
            int id = Integer.parseInt(m.group(1));
            Task input = JsonUtils.deserialize(new String(exchange.getRequestBody().readAllBytes(), UTF_8), Task.class); // lire le JSON envoyé par le client
            boolean updated = dao.update(id, input);

            if (updated) {
                sendResponse(exchange, 204, null);
            }
            else {
                sendResponse(exchange, 404, null);
            }
            return;
        }
        //endregion

        // Sinon → 404
        sendResponse(exchange, 404, null);
    }

    private static void sendResponse(HttpExchange exchange, int status, String json) throws IOException {
        if(nonNull(json)) {
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
