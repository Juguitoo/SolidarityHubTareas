package solidarityhub.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import solidarityhub.backend.dto.DonationDTO;
import solidarityhub.backend.model.Donation;
import solidarityhub.backend.model.enums.DonationStatus;
import solidarityhub.backend.service.DonationService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/solidarityhub/donations")
public class DonationController {
    private final DonationService donationService;

    public DonationController(DonationService donationService) {
        this.donationService = donationService;
    }

    @GetMapping
    public ResponseEntity<?> getDonations() {
        List<DonationDTO> donationDTOs = new ArrayList<>();
        donationService.getAllDonations().forEach(d -> donationDTOs.add(new DonationDTO(d)));
        return ResponseEntity.ok(donationDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDonation(@PathVariable Integer id) {
        Donation donation = donationService.getDonationById(id);
        if (donation == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new DonationDTO(donation));
    }

    @GetMapping("/byCatastrophe")
    public ResponseEntity<?> getDonationsByCatastrophe(@RequestParam Integer catastropheId) {
        List<DonationDTO> donationDTOs = new ArrayList<>();
        donationService.getDonationsByCatastrophe(catastropheId).forEach(d -> donationDTOs.add(new DonationDTO(d)));
        return ResponseEntity.ok(donationDTOs);
    }

    @GetMapping("/byVolunteer")
    public ResponseEntity<?> getDonationsByVolunteer(@RequestParam String volunteerDni) {
        List<DonationDTO> donationDTOs = new ArrayList<>();
        donationService.getDonationsByVolunteer(volunteerDni).forEach(d -> donationDTOs.add(new DonationDTO(d)));
        return ResponseEntity.ok(donationDTOs);
    }

    @PostMapping
    public ResponseEntity<?> createDonation(@RequestBody DonationDTO donationDTO) {
        try {
            Donation donation = new Donation();
            donation.setType(donationDTO.getType());
            donation.setDescription(donationDTO.getDescription());
            donation.setStatus(donationDTO.getStatus());
            donation.setQuantity(donationDTO.getQuantity());
            donation.setUnit(donationDTO.getUnit());
            donation.setCantidad(donationDTO.getQuantity() + " " + donationDTO.getUnit());

            Donation savedDonation = donationService.createDonation(
                    donationDTO.getVolunteerDni(),
                    donationDTO.getCatastropheId(),
                    donation
            );

            if (savedDonation == null) {
                return ResponseEntity.badRequest().body("Invalid volunteer or catastrophe");
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(new DonationDTO(savedDonation));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDonation(@PathVariable Integer id, @RequestBody DonationDTO donationDTO) {
        Donation donation = donationService.getDonationById(id);
        if (donation == null) {
            return ResponseEntity.notFound().build();
        }

        donation.setType(donationDTO.getType());
        donation.setDescription(donationDTO.getDescription());
        donation.setStatus(donationDTO.getStatus());
        donation.setQuantity(donationDTO.getQuantity());
        donation.setUnit(donationDTO.getUnit());
        donation.setCantidad(donationDTO.getQuantity() + " " + donationDTO.getUnit());

        Donation updatedDonation = donationService.saveDonation(donation);
        return ResponseEntity.ok(new DonationDTO(updatedDonation));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDonation(@PathVariable Integer id) {
        Donation donation = donationService.getDonationById(id);
        if (donation == null) {
            return ResponseEntity.notFound().build();
        }
        donationService.deleteDonation(donation);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
