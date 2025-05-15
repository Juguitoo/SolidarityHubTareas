package solidarityhub.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import solidarityhub.backend.model.Task;
import solidarityhub.backend.model.Volunteer;
import solidarityhub.backend.model.enums.Status;
import solidarityhub.backend.repository.PDFCertificateRepository;
import solidarityhub.backend.service.PDFCertificateService;
import solidarityhub.backend.service.TaskService;
import solidarityhub.backend.service.VolunteerService;

import java.util.List;

@RequestMapping("/solidarityhub/certificates")
@RestController
public class PDFCertificateController {
    private final PDFCertificateService pdfService;
    private final TaskService taskService;
    private final VolunteerService volunteerService;


    @Autowired
    public PDFCertificateController(PDFCertificateService pdfService,
                                    TaskService taskService,
                                    VolunteerService volunteerService) {
        this.pdfService = pdfService;
        this.taskService = taskService;
        this.volunteerService = volunteerService;
    }
 
    @GetMapping
    public ResponseEntity<?> get() {
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<?> createCertificate(@RequestBody Integer id) {
        Task task = taskService.getTaskById(id);
        List<Volunteer> volunteerList = task.getVolunteers();
        for (Volunteer volunteer : volunteerList) {
            if(task.getAcceptedVolunteers().contains(volunteer)) {
                volunteer.getCertificates().stream().filter(c -> c.getTask().getId() == task.getId())
                        .toList()
                        .forEach(c -> pdfService.delete(c.getId()));
                pdfService.createPDFCertificate(volunteer, task);
            }
        }
        return ResponseEntity.ok().build();
    }
}