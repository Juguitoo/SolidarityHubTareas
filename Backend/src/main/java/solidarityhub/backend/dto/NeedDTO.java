package solidarityhub.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import solidarityhub.backend.model.Need;
import solidarityhub.backend.model.enums.NeedType;
import solidarityhub.backend.model.enums.UrgencyLevel;

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
