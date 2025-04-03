package solidarityhub.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import solidarityhub.backend.dto.CatastropheDTO;
import solidarityhub.backend.model.Catastrophe;
import solidarityhub.backend.model.GPSCoordinates;
import solidarityhub.backend.model.enums.EmergencyLevel;
import solidarityhub.backend.service.CatastropheService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/catastrophes")
public class CatastropheController {
    private final CatastropheService catastropheService;

    public CatastropheController(CatastropheService catastropheService) {
        this.catastropheService = catastropheService;
    }

    @GetMapping
    public ResponseEntity<?> getCatastrophes() {
        List<CatastropheDTO> catastropheDTOS = new ArrayList<>();
        catastropheService.getAllCatastrophes().forEach(c ->{catastropheDTOS.add(new CatastropheDTO(c));});
        return ResponseEntity.ok(catastropheDTOS);
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
    public ResponseEntity<?> addCatastrophe(@RequestBody CatastropheDTO catastropheDTO) {
        String name = catastropheDTO.getName();
        String description = catastropheDTO.getDescription();
        double locationX = catastropheDTO.getLocationX();
        double locationY = catastropheDTO.getLocationY();
        LocalDate startDate = catastropheDTO.getStartDate();
        EmergencyLevel emergencyLevel = catastropheDTO.getEmergencyLevel();

        Catastrophe catastrophe = new Catastrophe(name, description, new GPSCoordinates(locationX, locationY), startDate, emergencyLevel);

        return ResponseEntity.ok(catastropheService.saveCatastrophe(catastrophe));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCatastrophe(@PathVariable Integer id, @RequestBody CatastropheDTO catastropheDTO) {
        Catastrophe catastrophe = catastropheService.getCatastrophe(id);
        if(catastrophe == null) {
            return ResponseEntity.notFound().build();
        }

        catastrophe.setName(catastropheDTO.getName());
        catastrophe.setDescription(catastropheDTO.getDescription());
        catastrophe.setLocation(new GPSCoordinates(catastropheDTO.getLocationX(), catastropheDTO.getLocationY()));
        catastrophe.setStartDate(catastropheDTO.getStartDate());
        catastrophe.setEmergencyLevel(catastropheDTO.getEmergencyLevel());

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
