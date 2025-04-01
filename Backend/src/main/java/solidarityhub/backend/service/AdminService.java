package solidarityhub.backend.service;

import solidarityhub.backend.model.Admin;
import solidarityhub.backend.repository.AdminRepository;
import org.springframework.stereotype.Service;

@Service
public class AdminService {
    private final AdminRepository adminRepository;
    public AdminService(AdminRepository adminRepository) {this.adminRepository = adminRepository;}
    public Admin saveAdmin(Admin admin) {return adminRepository.save(admin);}
}
