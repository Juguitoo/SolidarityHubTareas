package solidarityhub.backend.service;

import solidarityhub.backend.model.Catastrophe;
import solidarityhub.backend.repository.CatastropheRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatastropheService {
    private final CatastropheRepository catastropheRepository;
    public CatastropheService(CatastropheRepository catastropheRepository) {this.catastropheRepository = catastropheRepository;}
    public Catastrophe save(Catastrophe catastrophe) {return catastropheRepository.save(catastrophe);}

    public List<Catastrophe> getAllCatastrophes() {return catastropheRepository.findAll();}
    public Catastrophe getCatastrophe(Integer id) {return catastropheRepository.findById(id).orElse(null);}
    public void deleteCatastrophe(Integer id) {catastropheRepository.deleteById(id);}
}
