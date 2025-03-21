package solidarityhub.backend.service;

import solidarityhub.backend.model.Volunteer;
import solidarityhub.backend.repository.VolunteerRepository;
import org.springframework.stereotype.Service;

@Service
public class VolunteerService {
    private final VolunteerRepository volunteerRepository;
    public VolunteerService(VolunteerRepository volunteerRepository) {this.volunteerRepository = volunteerRepository;}
    public Volunteer saveVolunteer(Volunteer volunteer) {
        return volunteerRepository.save(volunteer);
    }
}
