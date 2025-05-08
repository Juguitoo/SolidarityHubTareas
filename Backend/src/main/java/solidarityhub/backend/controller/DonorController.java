package solidarityhub.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import solidarityhub.backend.dto.DonorDTO;
import solidarityhub.backend.service.DonorService;

import java.util.List;

@RestController
@RequestMapping("/solidarityhub/donors")
public class DonorController {

    private final DonorService donorService;

    @Autowired
    public DonorController(DonorService donorService) {
        this.donorService = donorService;
    }

    @GetMapping
    public ResponseEntity<List<DonorDTO>> getAllDonors() {
        return ResponseEntity.ok(donorService.getAllDonors());
    }

    @GetMapping("/{dni}")
    public ResponseEntity<DonorDTO> getDonorByDni(@PathVariable String dni) {
        DonorDTO donorDTO = donorService.getDonorByDni(dni);
        if (donorDTO == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(donorDTO);
    }

    @PostMapping
    public ResponseEntity<DonorDTO> createDonor(@RequestBody DonorDTO donorDTO) {
        if (donorService.existsById(donorDTO.getDni())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        DonorDTO savedDonor = donorService.saveDonor(donorDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDonor);
    }

    @PutMapping("/{dni}")
    public ResponseEntity<DonorDTO> updateDonor(@PathVariable String dni, @RequestBody DonorDTO donorDTO) {
        if (!donorService.existsById(dni)) {
            return ResponseEntity.notFound().build();
        }
        donorDTO.setDni(dni); // Asegurar que el DNI en el path es el que se actualiza
        DonorDTO updatedDonor = donorService.saveDonor(donorDTO);
        return ResponseEntity.ok(updatedDonor);
    }

    @DeleteMapping("/{dni}")
    public ResponseEntity<Void> deleteDonor(@PathVariable String dni) {
        if (!donorService.existsById(dni)) {
            return ResponseEntity.notFound().build();
        }
        // Nota: Necesitarías añadir el método delete en DonorService
        // donorService.deleteDonor(dni);
        return ResponseEntity.noContent().build();
    }
}