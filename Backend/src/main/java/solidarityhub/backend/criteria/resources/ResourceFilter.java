package solidarityhub.backend.criteria.resources;

import solidarityhub.backend.model.Resource;

import java.util.List;

public interface ResourceFilter {
    List<Resource> filter(List<Resource> resources);
}
