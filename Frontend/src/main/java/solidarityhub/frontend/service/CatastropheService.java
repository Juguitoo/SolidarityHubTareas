package solidarityhub.frontend.service;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.views.catastrophe.CatastropheSelectionView;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class CatastropheService {

    private final RestTemplate restTemplate;
    private final String baseUrl = "http://localhost:8082/solidarityhub/catastrophes";

    public CatastropheService() {
        this.restTemplate = new RestTemplate();
    }

    //CRUD METHODS
    public CatastropheDTO saveCatastrophe(CatastropheDTO catastropheDTO) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<CatastropheDTO> request = new HttpEntity<>(catastropheDTO, headers);

            return restTemplate.postForObject(baseUrl, request, CatastropheDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw e; // Re-lanzar la excepción para manejarla en la vista
        }
    }

    public CatastropheDTO updateCatastrophe(Integer id, CatastropheDTO catastropheDTO) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<CatastropheDTO> request = new HttpEntity<>(catastropheDTO, headers);

            return restTemplate.exchange(
                    baseUrl + "/" + id,
                    HttpMethod.PUT,
                    request,
                    CatastropheDTO.class
            ).getBody();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void deleteCatastrophe(Integer id) {
        try {
            restTemplate.delete(baseUrl + "/" + id);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    //GET METHODS
    public List<CatastropheDTO> getAllCatastrophes() {
        try {
            CatastropheDTO[] catastrophes = restTemplate.getForObject(baseUrl, CatastropheDTO[].class);
            return catastrophes != null ? Arrays.asList(catastrophes) : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public CatastropheDTO getCatastropheById(Integer id) {
        try {
            return restTemplate.getForObject(baseUrl + "/" + id, CatastropheDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isCatastropheSelected(BeforeEnterEvent event, CatastropheDTO selectedCatastrophe) {
        if (selectedCatastrophe == null) {
            Notification.show("Por favor, selecciona una catástrofe primero",
                            3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_PRIMARY);
            event.rerouteTo("");
            return false;
        }
        return true;
    }
}
