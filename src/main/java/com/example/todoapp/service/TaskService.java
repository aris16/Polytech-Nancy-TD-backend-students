package com.example.todoapp.service;

import com.example.todoapp.business.model.Task;
import com.example.todoapp.dao.TaskDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TaskService {

    private final TaskDao dao = new TaskDao();

    public Task save(Task task) {
        return dao.save(task);
    }

    public Optional<Task> findById(int id) {
        return dao.findById(id);
    }

    public boolean delete(int id) {
        return dao.delete(id);
    }

    public boolean update(int id, Task task) {
        return dao.update(id, task);
    }

    public List<Task> getFilteredTasks(boolean todoOnly) {
        Map<Integer, Task> allTasks = dao.findAll();
        List<Task> listeTODO = new ArrayList<>();

        for (Task task : allTasks.values()) {
            if (todoOnly) {
                if (!task.done()) {
                    listeTODO.add(task);
                }
            }
            else {
                listeTODO.add(task);
            }
        }
        return listeTODO;
    }
}