package solidarityhub.backend.criteria;

import solidarityhub.backend.model.Resource;

import java.util.List;

public interface ResourceFilter {
    List<Resource> filter(List<Resource> resources);
}
