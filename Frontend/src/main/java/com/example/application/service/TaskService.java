package com.example.application.service;

import com.example.application.dto.TaskDTO;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
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
}
