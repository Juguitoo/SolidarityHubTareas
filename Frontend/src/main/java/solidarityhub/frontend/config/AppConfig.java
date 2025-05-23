package solidarityhub.frontend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import solidarityhub.frontend.service.ResourceService;
import solidarityhub.frontend.service.TaskService;

@Configuration
public class AppConfig {

    @Bean
    public TaskService taskService() {
        return new TaskService();
    }

    @Bean
    public ResourceService resourceService() {
        return new ResourceService();
    }
}
