package solidarityhub.backend.criteria.resources;

import solidarityhub.backend.model.Resource;

import java.util.List;

public class AndFilter implements ResourceFilter {
    private final ResourceFilter filter1;
    private final ResourceFilter filter2;

    public AndFilter(ResourceFilter filter1, ResourceFilter filter2) {
        this.filter1 = filter1;
        this.filter2 = filter2;
    }

    @Override
    public List<Resource> filter(List<Resource> resources) {
        List<Resource> filteredResources1 = filter1.filter(resources);
        return filter2.filter(filteredResources1);
    }
}
