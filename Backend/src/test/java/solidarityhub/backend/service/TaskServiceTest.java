package solidarityhub.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import solidarityhub.backend.model.*;
import solidarityhub.backend.model.enums.*;
import solidarityhub.backend.repository.TaskRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private VolunteerService volunteerService;

    @InjectMocks
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSave() {
        // Arrange
        Task task = createMockTask(1);
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // Act
        Task savedTask = taskService.save(task);

        // Assert
        assertNotNull(savedTask);
        assertEquals(1, savedTask.getId());
        verify(taskRepository, times(1)).save(task);
    }

    @Test
    void testGetAllTasks() {
        // Arrange
        List<Task> tasks = new ArrayList<>();
        tasks.add(createMockTask(1));
        tasks.add(createMockTask(2));
        when(taskRepository.findAll()).thenReturn(tasks);

        // Act
        List<Task> result = taskService.getAllTasks();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(taskRepository, times(1)).findAll();
    }

    @Test
    void testGetTaskById() {
        // Arrange
        Task task = createMockTask(1);
        when(taskRepository.findById(1)).thenReturn(Optional.of(task));

        // Act
        Task result = taskService.getTaskById(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        verify(taskRepository, times(1)).findById(1);
    }

    @Test
    void testDeleteTask() {
        // Arrange
        Task task = createMockTask(1);
        doNothing().when(taskRepository).delete(any(Task.class));

        // Act
        taskService.deleteTask(task);

        // Assert
        verify(taskRepository, times(1)).delete(task);
    }

    @Test
    void testGetSuggestedTasks_UrgentNeeds() {
        // Arrange
        List<Need> needs = new ArrayList<>();
        needs.add(createMockNeed(1, UrgencyLevel.URGENT));

        List<Volunteer> volunteers = new ArrayList<>();
        volunteers.add(createMockVolunteer("12345678A"));

        when(volunteerService.getAllVolunteers()).thenReturn(volunteers);

        // Act
        List<Task> suggestedTasks = taskService.getSuggestedTasks(needs);

        // Assert
        assertNotNull(suggestedTasks);
        assertEquals(1, suggestedTasks.size());

        Task task = suggestedTasks.get(0);
        assertEquals("Tarea con prioridad urgente", task.getTaskName());
        assertEquals(Priority.URGENT, task.getPriority());
        assertEquals(Status.TO_DO, task.getStatus());
        assertEquals(EmergencyLevel.MEDIUM, task.getEmergencyLevel());
    }

    @Test
    void testGetSuggestedTasks_ModerateNeeds() {
        // Arrange
        List<Need> needs = new ArrayList<>();
        needs.add(createMockNeed(1, UrgencyLevel.MODERATE));

        List<Volunteer> volunteers = new ArrayList<>();
        volunteers.add(createMockVolunteer("12345678A"));

        when(volunteerService.getAllVolunteers()).thenReturn(volunteers);

        // Act
        List<Task> suggestedTasks = taskService.getSuggestedTasks(needs);

        // Assert
        assertNotNull(suggestedTasks);
        assertEquals(1, suggestedTasks.size());

        Task task = suggestedTasks.get(0);
        assertEquals("Tarea con prioridad moderada", task.getTaskName());
        assertEquals(Priority.MODERATE, task.getPriority());
        assertEquals(Status.TO_DO, task.getStatus());
        assertEquals(EmergencyLevel.MEDIUM, task.getEmergencyLevel());
    }

    @Test
    void testGetSuggestedTasks_LowNeeds() {
        // Arrange
        List<Need> needs = new ArrayList<>();
        needs.add(createMockNeed(1, UrgencyLevel.LOW));

        List<Volunteer> volunteers = new ArrayList<>();
        volunteers.add(createMockVolunteer("12345678A"));

        when(volunteerService.getAllVolunteers()).thenReturn(volunteers);

        // Act
        List<Task> suggestedTasks = taskService.getSuggestedTasks(needs);

        // Assert
        assertNotNull(suggestedTasks);
        assertEquals(1, suggestedTasks.size());

        Task task = suggestedTasks.get(0);
        assertEquals("Tarea con prioridad baja", task.getTaskName());
        assertEquals(Priority.LOW, task.getPriority());
        assertEquals(Status.TO_DO, task.getStatus());
        assertEquals(EmergencyLevel.MEDIUM, task.getEmergencyLevel());
    }

    @Test
    void testGetSuggestedTasks_MixedNeeds() {
        // Arrange
        List<Need> needs = new ArrayList<>();
        needs.add(createMockNeed(1, UrgencyLevel.URGENT));
        needs.add(createMockNeed(2, UrgencyLevel.MODERATE));
        needs.add(createMockNeed(3, UrgencyLevel.LOW));

        List<Volunteer> volunteers = new ArrayList<>();
        volunteers.add(createMockVolunteer("12345678A"));

        when(volunteerService.getAllVolunteers()).thenReturn(volunteers);

        // Act
        List<Task> suggestedTasks = taskService.getSuggestedTasks(needs);

        // Assert
        assertNotNull(suggestedTasks);
        assertEquals(3, suggestedTasks.size());

        assertEquals("Tarea con prioridad urgente", suggestedTasks.get(0).getTaskName());
        assertEquals(Priority.URGENT, suggestedTasks.get(0).getPriority());

        assertEquals("Tarea con prioridad moderada", suggestedTasks.get(1).getTaskName());
        assertEquals(Priority.MODERATE, suggestedTasks.get(1).getPriority());

        assertEquals("Tarea con prioridad baja", suggestedTasks.get(2).getTaskName());
        assertEquals(Priority.LOW, suggestedTasks.get(2).getPriority());
    }

    @Test
    void testGetSuggestedTasks_EmptyNeeds() {
        // Arrange
        List<Need> needs = new ArrayList<>();

        // Act
        List<Task> suggestedTasks = taskService.getSuggestedTasks(needs);

        // Assert
        assertNotNull(suggestedTasks);
        assertTrue(suggestedTasks.isEmpty());
    }

    // Helper methods to create mock objects
    private Task createMockTask(int id) {
        Task task = new Task();

        // Using reflection to set the ID field
        try {
            java.lang.reflect.Field idField = Task.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(task, id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        task.setTaskName("Task " + id);
        task.setTaskDescription("Description " + id);
        task.setStartTimeDate(LocalDateTime.now());
        task.setEstimatedEndTimeDate(LocalDateTime.now().plusDays(7));
        task.setPriority(Priority.MODERATE);
        task.setEmergencyLevel(EmergencyLevel.MEDIUM);
        task.setStatus(Status.TO_DO);
        task.setType(TaskType.LOGISTICS);
        task.setMeetingDirection("Meeting point " + id);
        task.setCatastrophe(createMockCatastrophe(1));
        task.setNeeds(new ArrayList<>());
        task.setVolunteers(new ArrayList<>());
        return task;
    }

    private Need createMockNeed(int id, UrgencyLevel urgencyLevel) {
        Affected affected = new Affected("12345678B", "John", "Doe", "john.doe@example.com",
                123456789, "123 Main St", "password", false);

        GPSCoordinates coordinates = new GPSCoordinates(40.416775, -3.703790);
        Catastrophe catastrophe = createMockCatastrophe(1);

        Need need = new Need(affected, "Need " + id, urgencyLevel,
                TaskType.LOGISTICS, coordinates, catastrophe);

        // Set ID using reflection since there's no setter
        try {
            java.lang.reflect.Field idField = Need.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(need, id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return need;
    }

    private Volunteer createMockVolunteer(String dni) {
        List<TaskType> taskTypes = new ArrayList<>();
        taskTypes.add(TaskType.LOGISTICS);

        List<ScheduleAvailability> scheduleAvailabilities = new ArrayList<>();
        ScheduleAvailability availability = new ScheduleAvailability(DayMoment.MORNING, WeekDay.MONDAY);
        scheduleAvailabilities.add(availability);

        Volunteer volunteer = new Volunteer(dni, "Jane", "Doe", "jane.doe@example.com",
                987654321, "456 Oak St", "password",
                taskTypes, scheduleAvailabilities);

        return volunteer;
    }

    private Catastrophe createMockCatastrophe(int id) {
        GPSCoordinates coordinates = new GPSCoordinates(40.416775, -3.703790);
        Catastrophe catastrophe = new Catastrophe("Catastrophe " + id, "Description " + id,
                coordinates, LocalDate.now(), EmergencyLevel.MEDIUM);

        // Set ID using reflection since there's no setter
        try {
            java.lang.reflect.Field idField = Catastrophe.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(catastrophe, id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return catastrophe;
    }
}