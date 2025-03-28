package com.example.application.service;

import com.example.application.dto.TaskDTO;
import com.example.application.model.Need;
import com.example.application.model.Task;
import com.example.application.model.Volunteer;
import com.example.application.model.enums.NeedType;
import com.example.application.model.enums.Priority;
import com.example.application.model.enums.Status;
import com.example.application.model.enums.UrgencyLevel;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TaskService {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public TaskService() {
        this.restTemplate = new RestTemplate();
        this.baseUrl = "http://localhost:8082/tasks";
    }

    public List<TaskDTO> getTasks() {
        ResponseEntity<TaskDTO[]> response = restTemplate.exchange(baseUrl, HttpMethod.GET, null, TaskDTO[].class);
        TaskDTO[] tasks = response.getBody();
        if (tasks != null) {
            return List.of(tasks);
        }
        return new ArrayList<>();
    }

    public List<TaskDTO> getToDoTasks() {
        return getTasksByStatus(Status.TO_DO);
    }

    public List<TaskDTO> getToDoTasks(int limit) {
        return getTasksByStatus(Status.TO_DO, limit);
    }

    public List<TaskDTO> getDoingTasks() {
        return getTasksByStatus(Status.IN_PROGRESS);
    }

    public List<TaskDTO> getDoingTasks(int limit) {
        return getTasksByStatus(Status.IN_PROGRESS, limit);
    }

    public List<TaskDTO> getDoneTasks() {
        return getTasksByStatus(Status.FINISHED);
    }

    public List<TaskDTO> getDoneTasks(int limit) {
        return getTasksByStatus(Status.FINISHED, limit);
    }

    private List<TaskDTO> getTasksByStatus(Status status) {
        return getTasks().stream()
                .filter(task -> status.equals(task.getStatus()))
                .toList();
    }

    private List<TaskDTO> getTasksByStatus(Status status, int limit) {
        return getTasks().stream()
                .filter(task -> status.equals(task.getStatus()))
                .sorted(Comparator.comparing(TaskDTO::getStartTimeDate).reversed())
                .limit(limit)
                .toList();
    }

    public TaskDTO getTask(int id) {
        ResponseEntity<TaskDTO> response = restTemplate.exchange(baseUrl + "/" + id, HttpMethod.GET, null, TaskDTO.class);
        return response.getBody();
    }

    public void addTask(TaskDTO taskDTO) {
        restTemplate.postForEntity(baseUrl, taskDTO, TaskDTO.class);
    }

    public void updateTask(int id, TaskDTO taskDTO) {
        restTemplate.put(baseUrl + "/" + id, taskDTO);
    }

    public void deleteTask(int id) {
        restTemplate.delete(baseUrl + "/" + id);
    }

    //Metodo para obtener tareas de ejemplo (No están en la base de datos)
    public List<TaskDTO> getExampleTasks() {
        Need exampleNeed = new Need("Descripción de necesidad de ejemplo", UrgencyLevel.LOW, NeedType.BUILDING, null, null);

        // Crear un voluntario de ejemplo
        Volunteer exampleVolunteer = new Volunteer("33", "Fernando", "Alonso", "astonmartin@gmail.com");

        // Crear una tarea de ejemplo con la necesidad y el voluntario
        List<Need> needs = new ArrayList<>();
        needs.add(exampleNeed);
        List<Volunteer> volunteers = new ArrayList<>();
        volunteers.add(exampleVolunteer);

        Task exampleTask = new Task(needs, "Ejemplo de tarea", "Descripción de ejemplo", LocalDateTime.now(), LocalDateTime.now(), Priority.LOW, Status.TO_DO, volunteers);

        TaskDTO taskDTO = new TaskDTO(exampleTask);
        List<TaskDTO> exampleTasks = new ArrayList<>();
        exampleTasks.add(taskDTO);

        return exampleTasks;
    }
}
