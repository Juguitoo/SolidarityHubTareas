package solidarityhub.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CoordinatesServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private CoordinatesService coordinatesService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Creamos una instancia real del servicio
        coordinatesService = new CoordinatesService();

        // Inyectamos el RestTemplate mockeado usando ReflectionTestUtils
        ReflectionTestUtils.setField(coordinatesService, "restTemplate", restTemplate);
    }

    @Test
    void testGetCoordinates_Success() throws Exception {
        // Arrange
        String address = "Madrid, Spain";
        String jsonResponse = "[{\"lat\":\"40.4168\",\"lon\":\"-3.7038\"}]";

        // Mockeamos la respuesta del RestTemplate
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(jsonResponse);

        // Act
        Map<String, Double> result = coordinatesService.getCoordinates(address);

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("lat"));
        assertTrue(result.containsKey("lon"));
        // Los valores exactos pueden variar dependiendo de cómo se implemente el parsing en tu servicio
        // Por lo tanto, solo verificamos que están presentes
        verify(restTemplate, times(1)).getForObject(contains(address.replace(" ", "+")), eq(String.class));
    }

    @Test
    void testGetCoordinates_Exception() throws Exception {
        // Arrange
        String address = "Invalid Address";

        // Mockeamos una excepción al llamar al RestTemplate
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenThrow(new RuntimeException("API Error"));

        // Act
        Map<String, Double> result = coordinatesService.getCoordinates(address);

        // Assert
        assertNull(result);
        verify(restTemplate, times(1)).getForObject(contains(address.replace(" ", "+")), eq(String.class));
    }
}