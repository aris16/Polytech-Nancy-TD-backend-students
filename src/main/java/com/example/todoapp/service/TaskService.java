package com.example.todoapp.service;

import com.example.todoapp.business.model.Task;
import com.example.todoapp.dao.TaskDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TaskService {

    private final TaskDao dao = new TaskDao();

    /**
     * Persist {@link Task} model.
     * @param task task to save.
     * @return task model.
     */
    public Task save(Task task) {
        return dao.save(task);
    }

    /**
     * Retrieve {@link Task} model by id.
     * @param id identifier of the {@link Task}.
     * @return {@link Task} model wrapped by Optional.
     */
    public Optional<Task> findById(int id) {
        return dao.findById(id);
    }

    /**
     * Supprime une tâche à partir de son identifiant.
     * @param id L'identifiant de la tâche qu'on veut supprimer.
     * @return true si la tâche existait et a bien été supprimée,
     * false si aucun élément ne correspondait à cet id.
     */
    public boolean delete(int id) {
        return dao.delete(id);
    }

    /**
     * Met à jour les informations d'une tâche.
     * @param id L'identifiant de la tâche qu'on veut modifier.
     * @param task Le nouveau Task contenant les données.
     * @return true si la mise à jour a été effectuée,
     * false si la tâche n'existe pas.
     */
    public boolean update(int id, Task task) {
        return dao.update(id, task);
    }

    /**
     * Récupère la liste des tâches, et filtrer pour ne garder
     * que les tâches restantes à faire.
     * @param todoOnly Si vrai, filtre la liste pour garder que les tâches à faire.
     * Si faux, retourne toutes les tâches.
     * @return La liste triée.
     */
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