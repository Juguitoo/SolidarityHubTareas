package solidarityhub.backend.model.builder;

public class TaskDirector {
    public void construct(TaskBuilder taskBuilder) {
        taskBuilder.setTitle();
        taskBuilder.setDescription();
        taskBuilder.setStartDate();
        taskBuilder.setPriority();
        taskBuilder.setEmergencyLevel();
        taskBuilder.setStatus();
        taskBuilder.setVolunteers();
        taskBuilder.setNeed();
    }
}
