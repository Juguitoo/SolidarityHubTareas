package solidarityhub.backend.service;

import solidarityhub.backend.model.Skill;
import solidarityhub.backend.repository.SkillRepository;
import org.springframework.stereotype.Service;

@Service
public class SkillService {
    private final SkillRepository skillRepository;
    public SkillService(SkillRepository skillRepository) {this.skillRepository = skillRepository;}
    public Skill saveSkill(Skill skill) {return skillRepository.save(skill);}
}
