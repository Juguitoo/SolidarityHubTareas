package solidarityhub.backend.service;

import solidarityhub.backend.model.Affected;
import solidarityhub.backend.repository.AffectedRepository;
import org.springframework.stereotype.Service;

@Service
public class AffectedService {
    private final AffectedRepository AffectedRepository;
    public AffectedService(AffectedRepository AffectedRepository) {this.AffectedRepository = AffectedRepository;}
    public Affected save(Affected Affected) {return AffectedRepository.save(Affected);}
    public Affected getAffectedById(String id) {return AffectedRepository.findById(id).get();}
}
