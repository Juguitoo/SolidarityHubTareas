package solidarityhub.backend.service;

import solidarityhub.backend.model.Volunteer;
import solidarityhub.backend.repository.VolunteerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VolunteerService {
    private final VolunteerRepository volunteerRepository;
    public VolunteerService(VolunteerRepository volunteerRepository) {this.volunteerRepository = volunteerRepository;}
    public Volunteer saveVolunteer(Volunteer volunteer) {
        return volunteerRepository.save(volunteer);
    }
    public Volunteer getVolunteer(String volunteerId) {
        return volunteerRepository.findById(volunteerId).get();
    }
    public List<Volunteer> getAllVolunteers() {return volunteerRepository.findAll();}
}
