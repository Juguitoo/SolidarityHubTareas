package solidarityhub.backend.model.builder;

import solidarityhub.backend.model.Need;
import solidarityhub.backend.model.Task;

public interface TaskBuilder {
    void setNeed(Need need);

    void setTitle();
    void setDescription();
    void setStartDate();
    void setEndDate();
    void setPriority();
    void setEmergencyLevel();
    void setStatus();
    void setVolunteers();
    void setNeed();
    void setCatastrophe();

    Task getResult();
}
