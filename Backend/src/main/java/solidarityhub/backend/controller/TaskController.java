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
    private TaskMonitorService taskMonitorService;

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
    public ResponseEntity<?> getTasks(@RequestParam(required = false) String status,
                                      @RequestParam(required = false) String priority,
                                      @RequestParam(required = false) String type,
                                      @RequestParam(required = false) String emergencyLevel,
                                      @RequestParam Integer catastropheId) {

        List<TaskDTO> taskDTOList = new ArrayList<>();
        if (catastropheId == null) {
            taskService.getAllTasks().forEach(t -> {taskDTOList.add(new TaskDTO(t));});
            return ResponseEntity.ok(taskDTOList);
        }
        taskService.filter(status, priority, type, emergencyLevel, catastropheId)
                .forEach(t -> {taskDTOList.add(new TaskDTO(t));});
        return ResponseEntity.ok(taskDTOList);
    }

    @GetMapping("/catastrophe/{catastropheId}")
    public ResponseEntity<?> getTasksByCatastropheId(@PathVariable Integer catastropheId) {
        List<TaskDTO> taskDTOList = taskService.getTasksByCatastropheId(catastropheId);
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

        // Guardar la tarea para obtener el ID
        Task savedTask = taskService.save(task);

        for (Need need : needs) {
            need.setTask(savedTask);
            needService.save(need);
        }

        for (Volunteer volunteer : volunteers) {
            volunteer.getTasks().add(savedTask);

            Notification notification = getNotification(volunteer, savedTask, "Nueva tarea");
            savedTask.addNotification(notification);

            notificationService.notifyEmail(volunteer.getEmail(), notification);
            notificationService.notifyApp(volunteer.getNotificationToken(),"Nueva tarea",
                    "Se le ha asignado una nueva tarea: " + savedTask.getTaskName());
        }

        // Guardar nuevamente para persistir las relaciones
        savedTask = taskService.save(savedTask);

        // Notify observers about the new task
        taskMonitorService.checkTask(savedTask);

        // CAMBIO PRINCIPAL: Devolver la tarea creada en lugar de una respuesta vacía
        return ResponseEntity.status(HttpStatus.CREATED).body(new TaskDTO(savedTask));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable Integer id, @RequestBody TaskDTO taskDTO) {
        Task task = taskService.getTaskById(id);
        if(task == null) {
            return ResponseEntity.notFound().build();
        }

        // Guardar el estado anterior para comparación
        Status oldStatus = task.getStatus();

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

        // Usar el método específico para actualización de estado si cambió
        if (oldStatus != taskDTO.getStatus()) {
            taskService.updateTaskStatus(task);
        } else {
            taskService.save(task);
        }

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
            task.addNotification(notification);

            notificationService.notifyEmail(volunteer.getEmail(), notification);
            notificationService.notifyApp(volunteer.getNotificationToken(),
                    "Tarea actualizada",
                    "Se ha actualizado la tarea " + task.getTaskName() + " que se le había asignado. ");
            if(taskDTO.getStatus() == Status.FINISHED && task.getAcceptedVolunteers().contains(volunteer)) {
                List<PDFCertificate> certificates = volunteer.getCertificates().stream().filter(c -> c.getTask().getId() == task.getId()).toList();

                certificates.forEach(c -> pdfService.delete(c.getId()));
                pdfService.createPDFCertificate(volunteer, task);
            }
        }

        Task updatedTask = taskService.save(task);

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

        try{
            List<Task> suggestedTasks = taskService.getSuggestedTasks(needs);
            List<TaskDTO> suggestedTaskDTOs = new ArrayList<>();
            for (Task task : suggestedTasks) {
                suggestedTaskDTOs.add(new TaskDTO(task));
            }
            return ResponseEntity.ok(suggestedTaskDTOs);
        }catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body("No se ha podido crear la tarea: " + e.getMessage());
        }
    }

    @GetMapping("/todo")
    public ResponseEntity<?> getToDoTasksCount(@RequestParam Integer catastropheId) {
        return ResponseEntity.ok(taskService.getToDoTasksCount(catastropheId));
    }

    @GetMapping("/inProgress")
    public ResponseEntity<?> getInProgressTasksCount(@RequestParam Integer catastropheId) {
        return ResponseEntity.ok(taskService.getInProgressTasksCount(catastropheId));
    }

    @GetMapping("/finished")
    public ResponseEntity<?> getDoneTasksCount(@RequestParam Integer catastropheId) {
        return ResponseEntity.ok(taskService.getFinishedTasksCount(catastropheId));
    }

}
