package solidarityhub.backend.criteria.resources;

import solidarityhub.backend.criteria.ResourceFilter;
import solidarityhub.backend.model.Resource;

import java.util.List;
import java.util.stream.Collectors;

public class MinQuantityFilter implements ResourceFilter {
    private final double minQuantity;

    public MinQuantityFilter(double minQuantity) {
        this.minQuantity = minQuantity;
    }

    @Override
    public List<Resource> filter(List<Resource> resources) {
        return resources.stream()
                .filter(resource -> resource.getQuantity() >= minQuantity)
                .collect(Collectors.toList());
    }
}
