package solidarityhub.backend.service;

import solidarityhub.backend.dto.TaskDTO;
import solidarityhub.backend.model.*;
import solidarityhub.backend.model.strategy.*;
import solidarityhub.backend.repository.VolunteerRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class VolunteerService {
    private final VolunteerRepository volunteerRepository;
    private final VolunteerAssigner volunteerAssigner;

    public VolunteerService(VolunteerRepository volunteerRepository) {
        this.volunteerRepository = volunteerRepository;
        this.volunteerAssigner = new VolunteerAssigner(new NoFilterStrategy());
    }
    public Volunteer save(Volunteer volunteer) {return volunteerRepository.save(volunteer);}

    public Volunteer getVolunteer(String volunteerId) {
        return volunteerRepository.findById(volunteerId).orElse(null);
    }

    public List<Volunteer> getAllVolunteers() {
        CoordinatesService coordinatesService = new CoordinatesService();
        List<Volunteer> volunteersWithCoords = new ArrayList<>();
        List<Volunteer> volunteers = volunteerRepository.findAll();
        volunteers.forEach(v -> {
            if(v.getLocation() == null){
                Map<String, Double> coordinatesMap = coordinatesService.getCoordinates(v.getHomeAddress());
                if(coordinatesMap != null) {
                    if(coordinatesMap.get("lat") != null && coordinatesMap.get("lon") != null) {
                        v.setLocation(new GPSCoordinates(coordinatesMap.get("lat"), coordinatesMap.get("lon")));
                    }
                }
                save(v);
            }
            volunteersWithCoords.add(v);
        });
        return volunteersWithCoords;
    }

    public List<Volunteer> getVolunteersByStrategy(String strategy, TaskDTO taskDTO) {

        switch (strategy) {
            case "disponibilidad" -> volunteerAssigner.setStrategy(new AvailabilityStrategy());
            case "habilidades" -> volunteerAssigner.setStrategy(new SkillStrategy());
            case "distancia" -> volunteerAssigner.setStrategy(new DistanceStrategy());
            default -> volunteerAssigner.setStrategy(new NoFilterStrategy());
        }
        return volunteerAssigner.assignVolunteers(getAllVolunteers(), taskDTO);
    }
}
