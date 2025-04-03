package solidarityhub.backend.service;

import solidarityhub.backend.dto.TaskDTO;
import solidarityhub.backend.model.*;
import solidarityhub.backend.model.strategy.*;
import solidarityhub.backend.repository.VolunteerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VolunteerService {
    private final VolunteerRepository volunteerRepository;
    private VolunteerAssigner volunteerAssigner;

    public VolunteerService(VolunteerRepository volunteerRepository) {
        this.volunteerRepository = volunteerRepository;
        this.volunteerAssigner = new VolunteerAssigner();
    }
    public Volunteer saveVolunteer(Volunteer volunteer) {
        return volunteerRepository.save(volunteer);
    }

    public Volunteer getVolunteer(String volunteerId) {
        return volunteerRepository.findById(volunteerId).get();
    }

    public List<Volunteer> getAllVolunteers() {return volunteerRepository.findAll();}

    public List<Volunteer> getVolunteersByStrategy(String strategy, TaskDTO taskDTO) {

        if (strategy.equals("disponibilidad")) {
            volunteerAssigner.setStrategy(new AvailabilityStrategy());
        } else if (strategy.equals("habilidades")) {
            volunteerAssigner.setStrategy(new SkillStrategy());
        } else if (strategy.equals("distancia")) {
            volunteerAssigner.setStrategy(new DistanceStrategy());
        } else  {
            volunteerAssigner.setStrategy(new NoFilterStrategy());
        }
        return volunteerAssigner.assignVolunteers(getAllVolunteers(), taskDTO);
    }
}
