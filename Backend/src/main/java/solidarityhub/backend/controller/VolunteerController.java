package solidarityhub.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import solidarityhub.backend.dto.TaskDTO;
import solidarityhub.backend.dto.VolunteerDTO;
import solidarityhub.backend.service.VolunteerService;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
    public ResponseEntity<?> getVolunteers(@RequestParam String strategy, @RequestParam String taskString) {
        List<VolunteerDTO> volunteerDTOList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        String decodedTaskString = URLDecoder.decode(taskString, StandardCharsets.UTF_8);
        TaskDTO taskDTO = null;
        try {
            taskDTO = objectMapper.readValue(decodedTaskString, TaskDTO.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        volunteerService.getVolunteersByStrategy(strategy, taskDTO).forEach(v -> {volunteerDTOList.add(new VolunteerDTO(v));});
        return ResponseEntity.ok(volunteerDTOList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getVolunteer(@PathVariable String id) {
        return ResponseEntity.ok(new VolunteerDTO(volunteerService.getVolunteer(id)));
    }
}
