package solidarityhub.backend.criteria.resources;

import solidarityhub.backend.model.Resource;
import solidarityhub.backend.model.enums.ResourceType;

import java.util.List;
import java.util.stream.Collectors;

public class TypeFilter implements ResourceFilter {
    private final ResourceType type;

    public TypeFilter(ResourceType type) {
        this.type = type;
    }

    @Override
    public List<Resource> filter(List<Resource> resources) {
        return resources.stream()
                .filter(resource -> resource.getType().equals(type))
                .collect(Collectors.toList());
    }
}
