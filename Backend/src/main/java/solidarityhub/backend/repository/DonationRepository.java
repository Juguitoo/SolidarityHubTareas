package solidarityhub.backend.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import solidarityhub.backend.model.Donation;

import java.util.List;
public interface DonationRepository extends JpaRepository<Donation, Integer> {

    @Query("SELECT d FROM Donation d WHERE d.catastrophe.id = :catastropheId")
    List<Donation> findByCatastropheId(@Param("catastropheId") Integer catastropheId);

    @Query("SELECT d FROM Donation d WHERE d.volunteer.dni = :volunteerDni")
    List<Donation> findByVolunteerDni(@Param("donorDni") String volunteerDni);

    @Query("SELECT MAX(CAST(SUBSTRING(d.code, 10) AS int)) FROM Donation d WHERE d.code LIKE CONCAT('DON-', :year, '-%')")
    Integer findMaxDonationNumberForYear(@Param("year") String year);
}
