package com.example.application.dto;

import lombok.Getter;
import com.example.application.model.Need;
import com.example.application.model.enums.NeedType;
import com.example.application.model.enums.UrgencyLevel;

@Getter
public class NeedDTO {
    private final int id;
    private final String description;
    private final UrgencyLevel urgency;
    private final NeedType needType;
    private final int taskId;

    public NeedDTO(Need need) {
        this.id = need.getId();
        this.description = need.getDescription();
        this.urgency = need.getUrgency();
        this.needType = need.getNeedType();
        if(need.getTask() != null) {
            this.taskId = need.getTask().getId();
        } else {
            this.taskId = -1;
        }
    }
}
