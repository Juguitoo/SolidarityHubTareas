package solidarityhub.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import solidarityhub.backend.dto.StorageDTO;
import solidarityhub.backend.model.Storage;
import solidarityhub.backend.observer.impl.StorageObservable;
import solidarityhub.backend.repository.StorageRepository;

import java.util.List;

@Service
public class StorageService {
    private final StorageRepository storageRepository;
    public StorageService(StorageRepository storageRepository) {
        this.storageRepository = storageRepository;
    }
    public List<StorageDTO> getStorages() {return storageRepository.findStorages();}

    public Storage saveStorage(Storage storage) {
        Storage savedStorage = storageRepository.save(storage);
        try {
            storageObservable.setStorage(savedStorage);
        } catch (Exception _) {}
        return savedStorage;
    }
    public Storage getStorageById(Integer id) {return storageRepository.findById(id).orElse(null);}
    public void deleteStorage(Storage storage) {storageRepository.delete(storage);}

    private StorageObservable storageObservable;

    // Add this method to check a specific storage
    public void checkStorageCapacity(Storage storage) {
        storageObservable.setStorage(storage);
    }

    // Add this method to check all storages
    public void checkAllStorages() {
        List<Storage> storages = storageRepository.findAll();
        for (Storage storage : storages) {
            checkStorageCapacity(storage);
        }
    }
}
