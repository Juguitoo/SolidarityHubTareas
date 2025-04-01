package solidarityhub.backend.service;

import solidarityhub.backend.model.Storage;
import solidarityhub.backend.repository.StorageRepository;
import org.springframework.stereotype.Service;

@Service
public class StorageService {
    private final StorageRepository storageRepository;
    public StorageService(StorageRepository storageRepository) {this.storageRepository = storageRepository;}
    public Storage saveStorage(Storage storage) {
        return storageRepository.save(storage);
    }
}
