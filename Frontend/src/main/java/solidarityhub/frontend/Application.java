package solidarityhub.frontend;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import solidarityhub.frontend.dto.BackendDTOObservableService;

/**
 * The entry point of the Spring Boot application.
 *
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 *
 */
@SpringBootApplication
@Theme(value = "frontend")
public class Application implements AppShellConfigurator {

    private BackendDTOObservableService backendDTOObservableService;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @PostConstruct
    public void launchService(){
        backendDTOObservableService = BackendDTOObservableService.GetInstancia();
    }

    @PreDestroy
    public void onShutdown(){
        backendDTOObservableService.shutdown();
    }
}
