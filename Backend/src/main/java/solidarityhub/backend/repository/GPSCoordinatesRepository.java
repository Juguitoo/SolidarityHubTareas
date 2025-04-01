package solidarityhub.backend.repository;

import solidarityhub.backend.model.GPSCoordinates;
import org.springframework.data.jpa.repository.JpaRepository;


public interface GPSCoordinatesRepository extends JpaRepository<GPSCoordinates, Integer> {
}
