package solidarityhub.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import solidarityhub.backend.dto.VolunteerDTO;
import solidarityhub.backend.service.VolunteerService;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/volunteers")
public class VolunteerController {
    private final VolunteerService volunteerService;

    public VolunteerController(VolunteerService volunteerService) {
        this.volunteerService = volunteerService;
    }

    @GetMapping
    public ResponseEntity<?> getVolunteers() {
        List<VolunteerDTO> volunteerDTOList = new ArrayList<>();
        volunteerService.getAllVolunteers().forEach(v -> {volunteerDTOList.add(new VolunteerDTO(v));});
        return ResponseEntity.ok(volunteerDTOList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getVolunteers(@PathVariable String id) {
        return ResponseEntity.ok(new VolunteerDTO(volunteerService.getVolunteer(id)));
    }
}
