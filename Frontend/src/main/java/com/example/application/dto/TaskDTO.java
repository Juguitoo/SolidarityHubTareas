package com.example.application.dto;

import com.example.application.model.enums.NeedType;
import com.example.application.model.enums.Priority;
import com.example.application.model.enums.Status;
import lombok.Getter;
import com.example.application.model.Task;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
public class TaskDTO {
    private final int id;
    private final String name;
    private final String description;
    private final LocalDateTime startTimeDate;
    private final LocalDateTime estimatedEndTimeDate;
    private final NeedType type;
    private final Priority priority;
    private final Status status;
    private final List<NeedDTO> needs;
    private final List<VolunteerDTO> volunteers;

    public TaskDTO(Task task) {
        this.needs = new ArrayList<>();
        this.volunteers = new ArrayList<>();
        this.id = task.getId();
        this.name = task.getTaskName();
        this.description = task.getTaskDescription();
        this.startTimeDate = task.getStartTimeDate();
        this.estimatedEndTimeDate = task.getEstimatedEndTimeDate();
        this.type = task.getType();
        this.priority = task.getPriority();
        this.status = task.getStatus();
        task.getNeeds().forEach(n -> {needs.add(new NeedDTO(n));});
        task.getVolunteers().forEach(v -> {volunteers.add(new VolunteerDTO(v));});
    }
}
