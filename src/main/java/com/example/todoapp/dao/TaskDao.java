package com.example.todoapp.dao;

import com.example.todoapp.business.model.Task;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Data Access Object for {@link Task} model.
 */
public class TaskDao {
    private final Map<Integer, Task> storage = new HashMap();

    public TaskDao() {
        this.save(new Task(1, "Réviser DS de maths", "Séries numériques et probabilités.", false));
        this.save(new Task(2, "Valider mon PIVE", "PIVE Club Poker.", true));
        this.save(new Task(3, "Choisir mon parcours de 4A", "SIR ou SIA ?", false));
    }

    /**
     * Persist {@link Task} model.
     * @param task task to save.
     * @return task model.
     */
    public Task save(Task task) {
        this.storage.put(task.id(), task);
        return task;
    }

    /**
     * Retrieve {@link Task} model by id.
     * @param id identifier of the {@link Task}.
     * @return {@link Task} model wrapped by Optional.
     */
    public Optional<Task> findById(int id) {
        return Optional.ofNullable((Task)this.storage.get(id));
    }

    public Map<Integer, Task> findAll() {
        return this.storage;
    }

    public boolean delete(int id) {
        return this.storage.remove(id) != null;
    }

    public boolean update(int id, Task task) {
        if (!this.storage.containsKey(id)) {
            return false;
        } else {
            this.storage.put(id, task);
            return true;
        }
    }
}
