package solidarityhub.backend.model.builder;

import solidarityhub.backend.model.Need;
import solidarityhub.backend.model.Task;

public class ModerateTask implements TaskBuilder {

    private Task task;
    private Need need;

    public ModerateTask(Need need) {
        this.task = new Task();
        this.need = need;
    }

    @Override
    public void setTitle() {

    }

    @Override
    public void setDescription() {

    }

    @Override
    public void setStartDate() {

    }

    @Override
    public void setPriority() {

    }

    @Override
    public void setEmergencyLevel() {

    }

    @Override
    public void setStatus() {

    }

    @Override
    public void setVolunteers() {

    }

    @Override
    public void setNeed() {

    }

    @Override
    public Task getResult() {
        return task;
    }
}
