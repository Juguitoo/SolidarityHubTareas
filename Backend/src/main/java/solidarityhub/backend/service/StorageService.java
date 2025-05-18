package solidarityhub.backend.service;

import org.springframework.stereotype.Service;
import solidarityhub.backend.dto.StorageDTO;
import solidarityhub.backend.model.Storage;
import solidarityhub.backend.repository.StorageRepository;

import java.util.List;

@Service
public class StorageService {
    private final StorageRepository storageRepository;
    public StorageService(StorageRepository storageRepository) {
        this.storageRepository = storageRepository;
    }
    public List<StorageDTO> getStorages() {return storageRepository.findStorages();}
    public Storage saveStorage(Storage storage) {return storageRepository.save(storage);}
    public Storage getStorageById(Integer id) {return storageRepository.findById(id).orElse(null);}
    public void deleteStorage(Storage storage) {storageRepository.delete(storage);}
}
