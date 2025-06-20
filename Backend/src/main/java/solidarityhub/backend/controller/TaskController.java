package solidarityhub.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import solidarityhub.backend.dto.NeedDTO;
import solidarityhub.backend.dto.TaskDTO;
import solidarityhub.backend.dto.VolunteerDTO;
import solidarityhub.backend.model.*;
import solidarityhub.backend.model.enums.Status;
import solidarityhub.backend.service.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        }else{
            return ResponseEntity.badRequest().body("La catástrofe especificada no existe");
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

        // VERIFICAR QUE LAS LISTAS NO SEAN NULL ANTES DE ITERAR
        List<Volunteer> currentVolunteers = task.getVolunteers();
        if (currentVolunteers != null && !currentVolunteers.isEmpty()) {
            // Crear una copia para evitar ConcurrentModificationException
            List<Volunteer> volunteersCopy = new ArrayList<>(currentVolunteers);
            volunteersCopy.forEach(volunteer -> {
                if (volunteer.getTasks() != null) {
                    volunteer.getTasks().remove(task);
                    volunteerService.save(volunteer);
                }
            });
        }

        List<Need> currentNeeds = task.getNeeds();
        if (currentNeeds != null && !currentNeeds.isEmpty()) {
            // Crear una copia para evitar ConcurrentModificationException
            List<Need> needsCopy = new ArrayList<>(currentNeeds);
            needsCopy.forEach(need -> {
                need.setTask(null);
                needService.save(need);
            });
        }

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
            if (volunteer != null) {
                volunteers.add(volunteer);
            }
        }

        if(task.getResourceAssignments() == null){
            task.setResourceAssignments(new ArrayList<>());
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
            if (volunteer.getTasks() == null) {
                volunteer.setTasks(new ArrayList<>());
            }
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

            // VERIFICACIÓN SEGURA DE ACCEPTED VOLUNTEERS
            if(taskDTO.getStatus() == Status.FINISHED) {
                List<Volunteer> acceptedVolunteers = task.getAcceptedVolunteers();
                if (acceptedVolunteers != null && acceptedVolunteers.contains(volunteer)) {
                    List<PDFCertificate> certificates = volunteer.getCertificates();
                    if (certificates != null) {
                        List<PDFCertificate> taskCertificates = certificates.stream()
                                .filter(c -> c.getTask() != null && c.getTask().getId() == task.getId())
                                .toList();

                        taskCertificates.forEach(c -> pdfService.delete(c.getId()));
                    }
                    pdfService.createPDFCertificate(volunteer, task);
                }
            }
        }

        Task updatedTask = taskService.save(task);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new TaskDTO(updatedTask));
    }

    @PutMapping("/{id}/status")
    @Transactional
    public ResponseEntity<?> updateTaskStatus(@PathVariable Integer id, @RequestBody Map<String, String> statusUpdate) {
        System.out.println("=== BACKEND: Actualizando estado de tarea ===");
        System.out.println("ID de tarea: " + id);
        System.out.println("Nuevo estado recibido: " + statusUpdate.get("status"));

        Task task = taskService.getTaskById(id);
        if(task == null) {
            System.err.println("✗ Tarea no encontrada con ID: " + id);
            return ResponseEntity.notFound().build();
        }

        System.out.println("Estado actual de la tarea: " + task.getStatus());

        try {
            Status newStatus = Status.valueOf(statusUpdate.get("status"));
            Status oldStatus = task.getStatus();

            System.out.println("Cambiando de " + oldStatus + " a " + newStatus);

            // USAR EL MÉTODO ESPECÍFICO PARA ACTUALIZAR SOLO EL ESTADO
            Task savedTask = taskService.updateTaskStatusOnly(task, newStatus);

            // VERIFICAR QUE SE GUARDÓ CORRECTAMENTE
            Task verifiedTask = taskService.getTaskById(id);
            System.out.println("Estado después de guardar: " + verifiedTask.getStatus());

            for (Volunteer v : task.getVolunteers()) {
                Notification notification = getNotification(v, task, "Tarea actualizada");
                task.addNotification(notification);

                notificationService.notifyEmail(v.getEmail(), notification);
                notificationService.notifyApp(v.getNotificationToken(),
                        "Tarea actualizada",
                        "Se ha actualizado la tarea " + task.getTaskName() + " que se le había asignado. ");
            }

            if (verifiedTask.getStatus() == newStatus) {
                System.out.println("✓ Estado actualizado correctamente en BD");
                return ResponseEntity.ok().body("Estado actualizado a: " + newStatus);
            } else {
                System.err.println("✗ Error: El estado no se actualizó en la BD");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error: El estado no se actualizó correctamente");
            }

        } catch (IllegalArgumentException e) {
            System.err.println("✗ Estado inválido: " + statusUpdate.get("status"));
            return ResponseEntity.badRequest().body("Estado inválido: " + statusUpdate.get("status"));
        } catch (Exception e) {
            System.err.println("✗ Error actualizando estado: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno: " + e.getMessage());
        }
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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Se debe seleccionar al menos una necesidad");
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
