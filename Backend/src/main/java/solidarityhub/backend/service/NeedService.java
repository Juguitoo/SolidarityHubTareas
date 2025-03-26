package solidarityhub.backend.service;

import solidarityhub.backend.model.Need;
import solidarityhub.backend.repository.NeedRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NeedService {
    private final NeedRepository needRepository;
    public NeedService(NeedRepository needRepository) {this.needRepository = needRepository;}
    public Need saveNeed(Need need) {
        return needRepository.save(need);
    }
    public Need findNeed(Integer id) {
        return needRepository.findById(id).get();
    }

    public List<Need> getAllNeeds() {return needRepository.findAll();}
}
