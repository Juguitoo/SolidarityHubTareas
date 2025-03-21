package solidarityhub.backend.service;

import solidarityhub.backend.model.Catastrophe;
import solidarityhub.backend.repository.CatastropheRepository;
import org.springframework.stereotype.Service;

@Service
public class CatastropheService {
    private final CatastropheRepository catastropheRepository;
    public CatastropheService(CatastropheRepository catastropheRepository) {this.catastropheRepository = catastropheRepository;}
    public Catastrophe saveCatastrophe(Catastrophe catastrophe) {return catastropheRepository.save(catastrophe);}
}
