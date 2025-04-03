package solidarityhub.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import solidarityhub.backend.dto.NeedDTO;
import solidarityhub.backend.dto.TaskDTO;
import solidarityhub.backend.dto.VolunteerDTO;
import solidarityhub.backend.model.Catastrophe;
import solidarityhub.backend.model.Need;
import solidarityhub.backend.model.Task;
import solidarityhub.backend.model.Volunteer;
import solidarityhub.backend.service.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService taskService;
    private final VolunteerService volunteerService;
    private final NeedService needService;
    private final NotificationService notificationService;
    private final CatastropheService catastropheService;


    public TaskController(TaskService taskService, VolunteerService volunteerService, NeedService needService, NotificationService notificationService, CatastropheService catastropheService) {
        this.taskService = taskService;
        this.volunteerService = volunteerService;
        this.needService = needService;
        this.catastropheService = catastropheService;
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<?> getTasks() {
        List<TaskDTO> taskDTOList = new ArrayList<>();
        taskService.getAllTasks().forEach(t -> {taskDTOList.add(new TaskDTO(t));});
        return ResponseEntity.ok(taskDTOList);
    }

    @GetMapping("/catastrophe/{catastropheId}")
    public ResponseEntity<?> getTasksByCatastrophe(@PathVariable Integer catastropheId) {
        List<TaskDTO> taskDTOList = new ArrayList<>();
        taskService.getTasksByCatastrophe(catastropheId).forEach(t -> {taskDTOList.add(new TaskDTO(t));});
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
                    taskDTO.getStatus(), volunteers, catastrophe);
        } else {
            task = new Task(needs, taskDTO.getName(), taskDTO.getDescription(), taskDTO.getStartTimeDate(),
                    taskDTO.getEstimatedEndTimeDate(), taskDTO.getPriority(), taskDTO.getEmergencyLevel(),
                    taskDTO.getStatus(), volunteers);

            // Si no se especificó una catástrofe, utilizar la de la primera necesidad
            if (!needs.isEmpty() && needs.getFirst().getCatastrophe() != null) {
                task.setCatastrophe(needs.getFirst().getCatastrophe());
            }
        }


        taskService.saveTask(task);

        for (Need need : needs) {
            need.setTask(task);
            needService.saveNeed(need);
        }

        for (Volunteer volunteer : volunteers) {
            volunteer.getTasks().add(task);
            notificationService.notifyEmail(volunteer.getEmail(),"Nueva tarea -> " + task.getCatastrophe().getName(),
            "Se le ha asignado una nueva tarea: " + task.getTaskName() + "\n" +
                    "Referente a la catástrofe: " + task.getCatastrophe().getName() + "\n" +
                    "Descripción: " + task.getTaskDescription() + "\n" +
                    "Fecha de inicio: " + task.getStartTimeDate() + "\n" +
                    "Fecha estimada de finalización: " + task.getEstimatedEndTimeDate() + "\n" +
                    "Prioridad: " + task.getPriority() + "\n" +
                    "Nivel de emergencia: " + task.getEmergencyLevel() + "\n" +
                    "Estado: " + task.getStatus());
            volunteerService.saveVolunteer(volunteer);
        }

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable Integer id, @RequestBody TaskDTO taskDTO) {
        Task task = taskService.getTaskById(id);
        if(task == null) {
            return ResponseEntity.notFound().build();
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
            if (need != null && !task.getNeeds().contains(need)) {
                needs.add(need);
            }
        }

        List<String> volunteerDnis = taskDTO.getVolunteers().stream().map(VolunteerDTO::getDni).toList();
        for (String volunteerID : volunteerDnis) {
            Volunteer volunteer = volunteerService.getVolunteer(volunteerID);
            if (volunteer != null && !task.getVolunteers().contains(volunteer)) {
                volunteers.add(volunteer);
            }
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

        taskService.saveTask(task);

        for (Need need : needs) {
            if(need.getTask() == null){
                need.setTask(task);
                needService.saveNeed(need);
            }
        }

        for (Volunteer volunteer : volunteers) {
            //if(!volunteer.getTasks().contains(task)) {
                volunteer.getTasks().add(task);
                notificationService.notifyEmail(volunteer.getEmail(), "Tarea actualizada -> " + task.getTaskName(),
                        "Se ha actualizado una tarea que se le había asignado. " + "\n" +
                                "Referente a la catástrofe: " + task.getCatastrophe().getName() + "\n" +
                                "Nombre de la tarea: " + task.getTaskName() + "\n" +
                                "Descripción: " + task.getTaskDescription() + "\n" +
                                "Fecha de inicio: " + task.getStartTimeDate() + "\n" +
                                "Fecha estimada de finalización: " + task.getEstimatedEndTimeDate() + "\n" +
                                "Prioridad: " + task.getPriority() + "\n" +
                                "Nivel de emergencia: " + task.getEmergencyLevel() + "\n" +
                                "Estado: " + task.getStatus());
                volunteerService.saveVolunteer(volunteer);
            //}
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
            needService.saveNeed(need);
        }
        for (Volunteer volunteer : task.getVolunteers()) {
            volunteer.getTasks().remove(task);
            volunteerService.saveVolunteer(volunteer);
        }
        taskService.deleteTask(task);
        return ResponseEntity.ok().build();
    }
}
