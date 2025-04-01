package solidarityhub.backend.service;

import solidarityhub.backend.model.Zone;
import solidarityhub.backend.repository.ZoneRepository;
import org.springframework.stereotype.Service;

@Service
public class ZoneService {
    private final  ZoneRepository  zoneRepository;
    public ZoneService( ZoneRepository  zoneRepository) {this. zoneRepository =  zoneRepository;}
    public Zone saveZone(Zone zone) { return this. zoneRepository.save(zone);}
}
