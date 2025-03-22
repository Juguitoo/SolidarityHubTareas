package solidarityhub.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import solidarityhub.backend.model.Catastrophe;
import solidarityhub.backend.model.GPSCoordinates;
import solidarityhub.backend.model.enums.EmergencyLevel;
import solidarityhub.backend.service.CatastropheService;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/catastrophes")
public class CatastropheController {
    private final CatastropheService catastropheService;

    public CatastropheController(CatastropheService catastropheService) {
        this.catastropheService = catastropheService;
    }

    @GetMapping
    public ResponseEntity<?> getCatastrophes() {
        return ResponseEntity.ok(catastropheService.getAllCatastrophes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCatastrophe(@PathVariable Integer id) {
        Catastrophe catastrophe = catastropheService.getCatastrophe(id);
        if(catastrophe == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(catastrophe);
    }

    @PostMapping
    public ResponseEntity<?> addCatastrophe(@RequestBody Map<String, String> payload) {
        String name = payload.get("name");
        String description = payload.get("description");
        double locationX = Double.parseDouble(payload.get("locationX"));
        double locationY = Double.parseDouble(payload.get("locationY"));
        LocalDate startDate = LocalDate.parse(payload.get("startDate"));
        EmergencyLevel emergencyLevel = EmergencyLevel.valueOf(payload.get("emergencyLevel"));

        Catastrophe catastrophe = new Catastrophe(name, description, new GPSCoordinates(locationX, locationY), startDate, emergencyLevel);

        return ResponseEntity.ok(catastropheService.saveCatastrophe(catastrophe));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCatastrophe(@PathVariable Integer id, @RequestBody Map<String, String> payload) {
        Catastrophe catastrophe = catastropheService.getCatastrophe(id);
        if(catastrophe == null) {
            return ResponseEntity.notFound().build();
        }

        String name = payload.get("name");
        String description = payload.get("description");
        double locationX = Double.parseDouble(payload.get("locationX"));
        double locationY = Double.parseDouble(payload.get("locationY"));
        LocalDate startDate = LocalDate.parse(payload.get("startDate"));
        EmergencyLevel emergencyLevel = EmergencyLevel.valueOf(payload.get("emergencyLevel"));

        catastrophe.setName(name);
        catastrophe.setDescription(description);
        catastrophe.setLocation(new GPSCoordinates(locationX, locationY));
        catastrophe.setStartDate(startDate);
        catastrophe.setEmergencyLevel(emergencyLevel);

        return ResponseEntity.ok(catastropheService.saveCatastrophe(catastrophe));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCatastrophe(@PathVariable Integer id) {
        Catastrophe catastrophe = catastropheService.getCatastrophe(id);
        if(catastrophe == null) {
            return ResponseEntity.notFound().build();
        }
        catastropheService.deleteCatastrophe(id);
        return ResponseEntity.ok().build();
    }
}
