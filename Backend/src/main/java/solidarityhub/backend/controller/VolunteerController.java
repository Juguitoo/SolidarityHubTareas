package solidarityhub.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import solidarityhub.backend.dto.TaskDTO;
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

    @GetMapping("/{strategy}")
    public ResponseEntity<?> getVolunteersByStrategy(@PathVariable String strategy, @RequestBody TaskDTO taskDTO) {
        List<VolunteerDTO> volunteerDTOList = new ArrayList<>();
        volunteerService.getVolunteersByStrategy(strategy, taskDTO).forEach(v -> {volunteerDTOList.add(new VolunteerDTO(v));});
        return ResponseEntity.ok(volunteerDTOList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getVolunteer(@PathVariable String id) {
        return ResponseEntity.ok(new VolunteerDTO(volunteerService.getVolunteer(id)));
    }
}
