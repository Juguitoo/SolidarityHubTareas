package solidarityhub.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import solidarityhub.backend.dto.NeedDTO;
import solidarityhub.backend.dto.TaskDTO;
import solidarityhub.backend.dto.VolunteerDTO;
import solidarityhub.backend.model.*;
import solidarityhub.backend.model.enums.Status;
import solidarityhub.backend.service.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/solidarityhub/tasks")
public class TaskController {
    private final TaskService taskService;
    private final VolunteerService volunteerService;
    private final NeedService needService;
    private final NotificationService notificationService;
    private final CatastropheService catastropheService;
    private final PDFCertificateService pdfService;

    @Autowired
    public TaskController(TaskService taskService, VolunteerService volunteerService, NeedService needService,
                          NotificationService notificationService, CatastropheService catastropheService, PDFCertificateService pdfService) {
        this.taskService = taskService;
        this.volunteerService = volunteerService;
        this.needService = needService;
        this.catastropheService = catastropheService;
        this.notificationService = notificationService;
        this.pdfService = pdfService;
    }

    @GetMapping
    public ResponseEntity<?> getTasks() {
        List<TaskDTO> taskDTOList = new ArrayList<>();
        taskService.getAllTasks().forEach(t -> {taskDTOList.add(new TaskDTO(t));});
        return ResponseEntity.ok(taskDTOList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTask(@PathVariable Integer id) {
        if(taskService.getTaskById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new TaskDTO(taskService.getTaskById(id)));
    }

    @PostMapping
    public ResponseEntity<?> addTask(@RequestBody TaskDTO taskDTO) {
        List<Need> needs = new ArrayList<>();
        List<Volunteer> volunteers = new ArrayList<>();
        Catastrophe catastrophe = null;

        // Obtener la catástrofe si se ha especificado
        if (taskDTO.getCatastropheId() != null) {
            catastrophe = catastropheService.getCatastrophe(taskDTO.getCatastropheId());
            if (catastrophe == null) {
                return ResponseEntity.badRequest().body("La catástrofe especificada no existe");
            }
        }


        List<Integer> needIds = taskDTO.getNeeds().stream().map(NeedDTO::getId).toList();
        for (Integer id : needIds) {
            Need need = needService.findNeed(id);
            if (need != null) {
                needs.add(need);
            }
        }

        if (needs.isEmpty()) {
            return ResponseEntity.badRequest().body("Se debe seleccionar al menos una necesidad");
        }

        List<String> volunteerDnis = taskDTO.getVolunteers().stream().map(VolunteerDTO::getDni).toList();
        for (String dni : volunteerDnis) {
            Volunteer volunteer = volunteerService.getVolunteer(dni);
            if (volunteer != null) {
                volunteers.add(volunteer);
            }
        }

        // Crear la tarea
        Task task;
        if (catastrophe != null) {
            task = new Task(needs, taskDTO.getName(), taskDTO.getDescription(), taskDTO.getStartTimeDate(),
                    taskDTO.getEstimatedEndTimeDate(), taskDTO.getPriority(), taskDTO.getEmergencyLevel(),
                    taskDTO.getStatus(), volunteers, taskDTO.getMeetingDirection(), catastrophe);
        } else {
            task = new Task(needs, taskDTO.getName(), taskDTO.getDescription(), taskDTO.getStartTimeDate(),
                    taskDTO.getEstimatedEndTimeDate(), taskDTO.getPriority(), taskDTO.getEmergencyLevel(),
                    taskDTO.getStatus(), volunteers, taskDTO.getMeetingDirection());

            // Si no se especificó una catástrofe, utilizar la de la primera necesidad
            if (!needs.isEmpty() && needs.getFirst().getCatastrophe() != null) {
                task.setCatastrophe(needs.getFirst().getCatastrophe());
            }
        }

        taskService.save(task);

        for (Need need : needs) {
            need.setTask(task);
            needService.save(need);
        }

        for (Volunteer volunteer : volunteers) {
            volunteer.getTasks().add(task);

            Notification notification = getNotification(volunteer, task, "Nueva tarea");
            task.addNotification(notification);

            notificationService.notifyEmail(volunteer.getEmail(), notification);
            notificationService.notifyApp(volunteer.getNotificationToken(),"Nueva tarea",
                    "Se le ha asignado una nueva tarea: " + task.getTaskName());
            notificationService.save(notification);

            volunteerService.save(volunteer);
        }
        taskService.save(task);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable Integer id, @RequestBody TaskDTO taskDTO) {
        Task task = taskService.getTaskById(id);
        if(task == null) {
            return ResponseEntity.notFound().build();
        }

        task.getVolunteers().forEach(volunteer -> {volunteer.getTasks().remove(task); volunteerService.save(volunteer);});
        task.getNeeds().forEach(need -> {need.setTask(null); needService.save(need);});

        List<Need> needs = new ArrayList<>();
        List<Volunteer> volunteers = new ArrayList<>();
        Catastrophe catastrophe = null;

        if (taskDTO.getCatastropheId() != null) {
            catastrophe = catastropheService.getCatastrophe(taskDTO.getCatastropheId());
            if (catastrophe == null) {
                return ResponseEntity.badRequest().body("La catástrofe especificada no existe");
            }
            task.setCatastrophe(catastrophe);
        }

        List<Integer> needIds = taskDTO.getNeeds().stream().map(NeedDTO::getId).toList();
        for (Integer needId : needIds) {
            Need need = needService.findNeed(needId);
            if (need != null) {
                needs.add(need);
            }
        }

        List<String> volunteerDnis = taskDTO.getVolunteers().stream().map(VolunteerDTO::getDni).toList();
        for (String volunteerID : volunteerDnis) {
            Volunteer volunteer = volunteerService.getVolunteer(volunteerID);
            volunteers.add(volunteer);
        }

        task.setTaskName(taskDTO.getName());
        task.setTaskDescription(taskDTO.getDescription());
        task.setStartTimeDate(taskDTO.getStartTimeDate());
        task.setEstimatedEndTimeDate(taskDTO.getEstimatedEndTimeDate());
        task.setPriority(taskDTO.getPriority());
        task.setEmergencyLevel(taskDTO.getEmergencyLevel());
        task.setStatus(taskDTO.getStatus());
        task.setVolunteers(volunteers);
        task.setNeeds(needs);
        task.setType(taskDTO.getType());
        task.setMeetingDirection(taskDTO.getMeetingDirection());

        taskService.save(task);

        for (Need need : needs) {
            if(need.getTask() == null){
                need.setTask(task);
                needService.save(need);
            }
        }

        for (Volunteer volunteer : volunteers) {
            if(!volunteer.getTasks().contains(task)) {
                volunteer.getTasks().add(task);
                volunteerService.save(volunteer);
            }

            Notification notification = getNotification(volunteer, task, "Tarea actualizada");

            notificationService.notifyEmail(volunteer.getEmail(), notification);
            notificationService.save(notification);
            notificationService.notifyApp(volunteer.getNotificationToken(),
                    "Tarea actualizada",
                    "Se ha actualizado la tarea " + task.getTaskName() + " que se le había asignado. ");
            if(taskDTO.getStatus() == Status.FINISHED && task.getAcceptedVolunteers().contains(volunteer)) {
                pdfService.createPDFDocument(volunteer, task);
            }
        }

        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Integer id) {
        Task task = taskService.getTaskById(id);
        if(task == null) {
            return ResponseEntity.notFound().build();
        }
        // Desasociar la tarea de las necesidades y voluntarios
        for (Need need : task.getNeeds()) {
            need.setTask(null);
            needService.save(need);
        }
        for (Volunteer volunteer : task.getVolunteers()) {

            Notification notification = getNotification(volunteer, task, "Tarea eliminada");

            notificationService.notifyEmail(volunteer.getEmail(),notification);
            notificationService.notifyApp(volunteer.getNotificationToken(), "Tarea eliminada",
                    "Se ha eliminado la tarea " + task.getTaskName() + " que se le había asignado.");

            volunteer.getTasks().remove(task);
            volunteerService.save(volunteer);
        }
        taskService.deleteTask(task);
        return ResponseEntity.ok().build();
    }

    private static Notification getNotification(Volunteer volunteer, Task task, String tipo) {
        String title = tipo + " -> " + task.getTaskName();
        String intro = "";

        switch (tipo) {
            case "Nueva tarea" -> {
                intro = "Se le ha asignado una nueva tarea: ";
            }
            case "Tarea actualizada" -> {
                intro = "Se ha actualizado la tarea que tenía asignada: ";
            }
            case "Tarea eliminada" -> {
                intro = "Se ha eliminado la tarea que tenía asignada: ";
            }
        }

        String body = intro + task.getTaskName() + "\n" +
                "Referente a la catástrofe: " + task.getCatastrophe().getName() + "\n" +
                "Descripción: " + task.getTaskDescription() + "\n" +
                "Fecha de inicio: " + task.getStartTimeDate() + "\n" +
                "Fecha estimada de finalización: " + task.getEstimatedEndTimeDate() + "\n" +
                "Prioridad: " + task.getPriority() + "\n" +
                "Nivel de emergencia: " + task.getEmergencyLevel() + "\n" +
                "Estado: " + task.getStatus() + "\n" +
                "Punto de encuentro: " + task.getMeetingDirection();

        return new Notification(title, body, task, volunteer);
    }

    @GetMapping("/suggestedTasks")
    public ResponseEntity<?> getSuggestedTasks(@RequestParam Integer catastropheId) {
        List<Need> needs = needService.getNeedsWithoutTask(catastropheId);
        if(needs.size()> 3) {
            needs = needs.subList(0, 3);
        }

        if (needs.isEmpty()) {
            return ResponseEntity.badRequest().body("Se debe seleccionar al menos una necesidad");
        }

        List<Task> suggestedTasks = taskService.getSuggestedTasks(needs);
        List<TaskDTO> suggestedTaskDTOs = new ArrayList<>();
        for (Task task : suggestedTasks) {
            suggestedTaskDTOs.add(new TaskDTO(task));
        }
        return ResponseEntity.ok(suggestedTaskDTOs);
    }
}
