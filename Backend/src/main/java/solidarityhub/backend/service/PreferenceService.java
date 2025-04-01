package solidarityhub.backend.service;

import solidarityhub.backend.model.Preference;
import solidarityhub.backend.repository.PreferenceRepository;
import org.springframework.stereotype.Service;

@Service
public class PreferenceService {
    private final PreferenceRepository preferenceRepository;
    public PreferenceService(PreferenceRepository preferenceRepository) {this.preferenceRepository = preferenceRepository;}
    public Preference savePreference(Preference preference) {return preferenceRepository.save(preference);}
}
