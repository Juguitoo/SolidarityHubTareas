package solidarityhub.backend.model.builder;

import solidarityhub.backend.model.Task;

public interface TaskBuilder {

    void setTitle();

    void setDescription();

    void setStartDate();

    void setPriority();

    void setEmergencyLevel();

    void setStatus();

    void setVolunteers();

    void setNeed();

    Task getResult();
}
