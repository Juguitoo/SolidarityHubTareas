package com.example.application.dto;

import com.example.application.model.enums.NeedType;
import com.example.application.model.enums.Priority;
import com.example.application.model.enums.Status;
import lombok.Getter;
import com.example.application.model.Task;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
public class TaskDTO {
    private int id;
    private String name;
    private String description;
    private LocalDateTime startTimeDate;
    private LocalDateTime estimatedEndTimeDate;
    private NeedType type;
    private Priority priority;
    private Status status;
    private List<NeedDTO> needs;
    private List<VolunteerDTO> volunteers;

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
