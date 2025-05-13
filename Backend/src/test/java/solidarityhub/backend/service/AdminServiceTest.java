package solidarityhub.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import solidarityhub.backend.model.Admin;
import solidarityhub.backend.repository.AdminRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AdminServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @InjectMocks
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSave() {
        // Arrange
        Admin admin = createTestAdmin("12345678A", "securePassword");
        when(adminRepository.save(any(Admin.class))).thenReturn(admin);

        // Act
        Admin savedAdmin = adminService.save(admin);

        // Assert
        assertNotNull(savedAdmin);
        assertEquals("12345678A", savedAdmin.getDni());
        assertEquals("securePassword", savedAdmin.getPassword());
        verify(adminRepository, times(1)).save(admin);
    }

    // Helper method
    private Admin createTestAdmin(String dni, String password) {
        return new Admin(dni, password);
    }
}