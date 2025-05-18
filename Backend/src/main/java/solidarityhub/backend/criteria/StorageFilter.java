package solidarityhub.backend.criteria;

import solidarityhub.backend.model.Resource;
import solidarityhub.backend.model.Storage;

import java.util.List;
import java.util.stream.Collectors;

public class StorageFilter implements ResourceFilter {
    private final int storageId;

    public StorageFilter(int storageId) {
        this.storageId = storageId;
    }

    @Override
    public List<Resource> filter(List<Resource> resources) {
        return resources.stream()
                .filter(resource -> resource.getStorage() != null && resource.getStorage().getId() == this.storageId)
                .collect(Collectors.toList());
    }
}
