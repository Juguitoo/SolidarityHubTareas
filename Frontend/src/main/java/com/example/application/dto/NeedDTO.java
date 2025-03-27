package com.example.application.dto;

import lombok.Getter;
import com.example.application.model.Need;
import com.example.application.model.enums.NeedType;
import com.example.application.model.enums.UrgencyLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class NeedDTO {
    private int id;
    private String description;
    private UrgencyLevel urgency;
    private NeedType needType;
    private int taskId;

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
