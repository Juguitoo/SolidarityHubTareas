package solidarityhub.backend.controller;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import solidarityhub.backend.config.FcmService;
import solidarityhub.backend.dto.NeedDTO;
import solidarityhub.backend.dto.TaskDTO;
import solidarityhub.backend.dto.VolunteerDTO;
import solidarityhub.backend.model.*;
import solidarityhub.backend.model.enums.*;
import solidarityhub.backend.service.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TaskControllerTest {

    @Mock
    private TaskService taskService;

    @Mock
    private VolunteerService volunteerService;

    @Mock
    private NeedService needService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private CatastropheService catastropheService;

    @Mock
    private FcmService fcmService;

    @InjectMocks
    private TaskController taskController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetTasks() {
        // Arrange
        List<Task> tasks = createMockTasks();
        when(taskService.getAllTasks()).thenReturn(tasks);

        // Act
        ResponseEntity<?> response = taskController.getTasks("", "", "", "", null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof List);
        List<?> responseBody = (List<?>) response.getBody();
        assertEquals(2, responseBody.size());
        assertTrue(responseBody.get(0) instanceof TaskDTO);
    }

    @Test
    void testGetTask_ExistingId() {
        // Arrange
        int taskId = 1;
        Task task = createMockTask(taskId);
        when(taskService.getTaskById(taskId)).thenReturn(task);

        // Act
        ResponseEntity<?> response = taskController.getTask(taskId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof TaskDTO);
        TaskDTO responseTask = (TaskDTO) response.getBody();
        assertEquals(taskId, responseTask.getId());
    }

    @Test
    void testGetTask_NonExistingId() {
        // Arrange
        int nonExistingId = 999;
        when(taskService.getTaskById(nonExistingId)).thenReturn(null);

        // Act
        ResponseEntity<?> response = taskController.getTask(nonExistingId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testAddTask_Success() {
        // Arrange
        TaskDTO taskDTO = createMockTaskDTO();
        Need need = createMockNeed(1);
        Volunteer volunteer = createMockVolunteer("12345678A");
        Catastrophe catastrophe = createMockCatastrophe(1);

        when(catastropheService.getCatastrophe(taskDTO.getCatastropheId())).thenReturn(catastrophe);
        when(needService.findNeed(anyInt())).thenReturn(need);
        when(volunteerService.getVolunteer(anyString())).thenReturn(volunteer);

        // Act
        ResponseEntity<?> response = taskController.addTask(taskDTO);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(taskService, times(2)).save(any(Task.class));
        verify(needService, times(1)).save(any(Need.class));
        verify(volunteerService, times(1)).save(any(Volunteer.class));
        verify(notificationService, times(1)).notifyEmail(anyString(), any(Notification.class));
        verify(notificationService, times(1)).save(any(Notification.class));
    }

    @Test
    void testAddTask_NoCatastrophe() {
        // Arrange
        TaskDTO taskDTO = createMockTaskDTO();
        taskDTO.setCatastropheId(null);

        Need need = createMockNeed(1);
        need.setCatastrophe(createMockCatastrophe(1));

        Volunteer volunteer = createMockVolunteer("12345678A");

        when(needService.findNeed(anyInt())).thenReturn(need);
        when(volunteerService.getVolunteer(anyString())).thenReturn(volunteer);

        // Act
        ResponseEntity<?> response = taskController.addTask(taskDTO);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(taskService, times(2)).save(any(Task.class));
    }

    @Test
    void testAddTask_InvalidCatastrophe() {
        // Arrange
        TaskDTO taskDTO = createMockTaskDTO();
        when(catastropheService.getCatastrophe(taskDTO.getCatastropheId())).thenReturn(null);

        // Act
        ResponseEntity<?> response = taskController.addTask(taskDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("La catástrofe especificada no existe", response.getBody());
    }

    @Test
    void testAddTask_NoNeeds() {
        // Arrange
        TaskDTO taskDTO = createMockTaskDTO();
        Catastrophe catastrophe = createMockCatastrophe(1);

        when(catastropheService.getCatastrophe(taskDTO.getCatastropheId())).thenReturn(catastrophe);
        when(needService.findNeed(anyInt())).thenReturn(null);

        // Act
        ResponseEntity<?> response = taskController.addTask(taskDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Se debe seleccionar al menos una necesidad", response.getBody());
    }

    @Test
    void testUpdateTask_Success() {
        // Arrange
        int taskId = 1;
        TaskDTO taskDTO = createMockTaskDTO();
        Task existingTask = createMockTask(taskId);
        Need need = createMockNeed(1);
        Volunteer volunteer = createMockVolunteer("12345678A");
        Catastrophe catastrophe = createMockCatastrophe(1);

        when(taskService.getTaskById(taskId)).thenReturn(existingTask);
        when(catastropheService.getCatastrophe(taskDTO.getCatastropheId())).thenReturn(catastrophe);
        when(needService.findNeed(anyInt())).thenReturn(need);
        when(volunteerService.getVolunteer(anyString())).thenReturn(volunteer);

        // Act
        ResponseEntity<?> response = taskController.updateTask(taskId, taskDTO);

        // Assert
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        verify(taskService, times(1)).save(existingTask);
        verify(needService, atLeastOnce()).save(any(Need.class));
        verify(volunteerService, atLeastOnce()).save(any(Volunteer.class));
        verify(notificationService, times(1)).notifyEmail(anyString(), any(Notification.class));
        verify(notificationService, times(1)).save(any(Notification.class));
    }

    @Test
    void testUpdateTask_NotFound() {
        // Arrange
        int nonExistingId = 999;
        TaskDTO taskDTO = createMockTaskDTO();

        when(taskService.getTaskById(nonExistingId)).thenReturn(null);

        // Act
        ResponseEntity<?> response = taskController.updateTask(nonExistingId, taskDTO);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(taskService, never()).save(any(Task.class));
    }

    @Test
    void testDeleteTask_Success() {
        // Arrange
        int taskId = 1;
        Task task = createMockTask(taskId);
        Need need = createMockNeed(1);
        Volunteer volunteer = createMockVolunteer("12345678A");

        List<Need> needs = new ArrayList<>();
        needs.add(need);

        List<Volunteer> volunteers = new ArrayList<>();
        volunteers.add(volunteer);

        task.setNeeds(needs);
        task.setVolunteers(volunteers);

        when(taskService.getTaskById(taskId)).thenReturn(task);

        // Act
        ResponseEntity<?> response = taskController.deleteTask(taskId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(needService, times(1)).save(need);
        verify(volunteerService, times(1)).save(volunteer);
        verify(taskService, times(1)).deleteTask(task);
        verify(notificationService, times(1)).notifyEmail(anyString(), any(Notification.class));
    }

    @Test
    void testDeleteTask_NotFound() {
        // Arrange
        int nonExistingId = 999;
        when(taskService.getTaskById(nonExistingId)).thenReturn(null);

        // Act
        ResponseEntity<?> response = taskController.deleteTask(nonExistingId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(taskService, never()).deleteTask(any(Task.class));
    }

    @Test
    void testGetSuggestedTasks_Success() {
        // Arrange
        int catastropheId = 1;
        List<Need> needs = new ArrayList<>();
        needs.add(createMockNeed(1));

        List<Task> suggestedTasks = new ArrayList<>();
        suggestedTasks.add(createMockTask(1));

        when(needService.getNeedsWithoutTask(catastropheId)).thenReturn(needs);
        when(taskService.getSuggestedTasks(any())).thenReturn(suggestedTasks);

        // Act
        ResponseEntity<?> response = taskController.getSuggestedTasks(catastropheId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof List);
        List<?> responseBody = (List<?>) response.getBody();
        assertEquals(1, responseBody.size());
        assertTrue(responseBody.get(0) instanceof TaskDTO);
    }

    @Test
    void testGetSuggestedTasks_NoNeeds() {
        // Arrange
        int catastropheId = 1;
        when(needService.getNeedsWithoutTask(catastropheId)).thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<?> response = taskController.getSuggestedTasks(catastropheId);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Se debe seleccionar al menos una necesidad", response.getBody());
    }

    // Helper methods to create mock objects
    private List<Task> createMockTasks() {
        List<Task> tasks = new ArrayList<>();
        tasks.add(createMockTask(1));
        tasks.add(createMockTask(2));
        return tasks;
    }

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

    private TaskDTO createMockTaskDTO() {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setId(1);
        taskDTO.setName("Task 1");
        taskDTO.setDescription("Description 1");
        taskDTO.setStartTimeDate(LocalDateTime.now());
        taskDTO.setEstimatedEndTimeDate(LocalDateTime.now().plusDays(7));
        taskDTO.setType(TaskType.LOGISTICS);
        taskDTO.setPriority(Priority.MODERATE);
        taskDTO.setEmergencyLevel(EmergencyLevel.MEDIUM);
        taskDTO.setStatus(Status.TO_DO);
        taskDTO.setCatastropheId(1);
        taskDTO.setMeetingDirection("Meeting point 1");

        List<NeedDTO> needDTOs = new ArrayList<>();
        NeedDTO needDTO = new NeedDTO();
        needDTO.setId(1);
        needDTOs.add(needDTO);
        taskDTO.setNeeds(needDTOs);

        List<VolunteerDTO> volunteerDTOs = new ArrayList<>();
        VolunteerDTO volunteerDTO = new VolunteerDTO();
        volunteerDTO.setDni("12345678A");
        volunteerDTOs.add(volunteerDTO);
        taskDTO.setVolunteers(volunteerDTOs);

        return taskDTO;
    }

    private Need createMockNeed(int id) {
        Affected affected = new Affected("12345678B", "John", "Doe", "john.doe@example.com",
                123456789, "123 Main St", "password", false);

        GPSCoordinates coordinates = new GPSCoordinates(40.416775, -3.703790);
        Catastrophe catastrophe = createMockCatastrophe(1);

        Need need = new Need(affected, "Need " + id, UrgencyLevel.MODERATE,
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