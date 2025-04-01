package solidarityhub.backend.service;

import solidarityhub.backend.model.ScheduleAvailability;
import solidarityhub.backend.repository.ScheduleAvailabilityRepository;
import org.springframework.stereotype.Service;

@Service
public class ScheduleAvailabilityService {
    private final ScheduleAvailabilityRepository scheduleAvailabilityRepository;
    public ScheduleAvailabilityService(ScheduleAvailabilityRepository scheduleAvailabilityRepository) {this.scheduleAvailabilityRepository = scheduleAvailabilityRepository;}
    public ScheduleAvailability saveScheduleAvailability(ScheduleAvailability scheduleAvailability) {return scheduleAvailabilityRepository.save(scheduleAvailability);}
}
