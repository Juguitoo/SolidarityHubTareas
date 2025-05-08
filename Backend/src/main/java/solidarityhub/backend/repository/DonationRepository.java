package solidarityhub.backend.repository;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import solidarityhub.backend.model.Donation;

import java.util.List;
public interface DonationRepository extends JpaRepository<Donation, Integer> {

    @Query("SELECT d FROM Donation d WHERE d.catastrophe.id = :catastropheId")
    List<Donation> findByCatastropheId(@Param("catastropheId") Integer catastropheId);

    @Query("SELECT d FROM Donation d WHERE d.donor.dni = :volunteerDni")
    List<Donation> findByDonorDni(@Param("donorDni") String volunteerDni);

    @Modifying
    @Transactional
    @Query("DELETE FROM Donation d WHERE d.id = :donationId")
    int deleteDonationById(@Param("donationId") int donationId);
}
