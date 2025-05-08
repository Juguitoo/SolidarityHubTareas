package solidarityhub.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import solidarityhub.backend.dto.TaskDTO;
import solidarityhub.backend.dto.VolunteerDTO;
import solidarityhub.backend.model.Volunteer;
import solidarityhub.backend.service.VolunteerService;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/solidarityhub/volunteers")
public class VolunteerController {
    private final VolunteerService volunteerService;

    public VolunteerController(VolunteerService volunteerService) {
        this.volunteerService = volunteerService;
    }

    @GetMapping
    public ResponseEntity<?> getVolunteers(@RequestParam String strategy, @RequestParam String taskString) {
        List<VolunteerDTO> volunteerDTOList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String decodedTaskString = URLDecoder.decode(taskString, StandardCharsets.UTF_8);

        TaskDTO taskDTO = null;
        try {
            taskDTO = objectMapper.readValue(decodedTaskString, TaskDTO.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        TaskDTO finalTaskDTO = taskDTO;
        volunteerService.getVolunteersByStrategy(strategy, taskDTO).forEach(v -> {
            VolunteerDTO volunteerDTO = new VolunteerDTO(v);
            volunteerDTO.setAvailabilityStatus(v.isAvailable(finalTaskDTO.getStartTimeDate(), finalTaskDTO.getEstimatedEndTimeDate()));
            volunteerDTOList.add(volunteerDTO);
        });

        return ResponseEntity.ok(volunteerDTOList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getVolunteer(@PathVariable String id) {
        return ResponseEntity.ok(new VolunteerDTO(volunteerService.getVolunteer(id)));
    }

    @PostMapping("/checkAvailability")
    public ResponseEntity<Integer> checkAvailability(@RequestBody Map<String, Object> requestParams) {
        try {
            String volunteerId = (String) requestParams.get("volunteerId");
            String startDateStr = (String) requestParams.get("startDate");
            String endDateStr = (String) requestParams.get("endDate");

            if (volunteerId == null || startDateStr == null || endDateStr == null) {
                return ResponseEntity.badRequest().build();
            }

            // Parse dates
            LocalDateTime startDate = LocalDateTime.parse(startDateStr);
            LocalDateTime endDate = LocalDateTime.parse(endDateStr);

            // Get volunteer
            Volunteer volunteer = volunteerService.getVolunteer(volunteerId);
            if (volunteer == null) {
                return ResponseEntity.notFound().build();
            }

            // Check availability
            int availabilityScore = volunteer.isAvailable(startDate, endDate);
            return ResponseEntity.ok(availabilityScore);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
