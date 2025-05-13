package solidarityhub.frontend.dto;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.pingu.web.BackendObservableService.BackendObservableService;
import org.pingu.web.BackendObservableService.Singleton;
import org.pingu.web.BackendObservableService.observableList.concrete.BackendDTOObservableList;
import org.springframework.core.ParameterizedTypeReference;

@Getter
@Slf4j
public class BackendDTOObservableService extends Singleton implements BackendObservableService {

    private static final BackendDTOObservableService s_pInstancia = new BackendDTOObservableService();

    public static BackendDTOObservableService GetInstancia() {
        return s_pInstancia;
    }

    protected BackendDTOObservableService() {
        try {
            log.info("Iniciando los observadores del backend");
            NeedList = new BackendDTOObservableList<>("/solidarityhub/needs", 3, new ParameterizedTypeReference<>() {});
            Thread.sleep(1000); // Espera entre servicios para no explotar el back

            CatastropheList = new BackendDTOObservableList<>("/solidarityhub/catastrophes", 5,  new ParameterizedTypeReference<>() {});
            Thread.sleep(1000);

            StorageList = new BackendDTOObservableList<>("/solidarityhub/storages", 5,  new ParameterizedTypeReference<>() {});
            Thread.sleep(1000);

            TaskList = new BackendDTOObservableList<>("/solidarityhub/tasks", 3, new ParameterizedTypeReference<>() {});
            Thread.sleep(1000);

            VolunteerList = new BackendDTOObservableList<>("/solidarityhub/volunteers", 3,  new ParameterizedTypeReference<>() {});
            Thread.sleep(1000);
        }
        catch (InterruptedException e){
            log.error("Error en una pausa del servicio {} {}", e.getMessage(), e.getStackTrace());
            throw new RuntimeException();
        }
    }

    private final BackendDTOObservableList<NeedDTO> NeedList;
    private final BackendDTOObservableList<CatastropheDTO> CatastropheList;
    private final BackendDTOObservableList<StorageDTO> StorageList;
    private final BackendDTOObservableList<TaskDTO> TaskList;
    private final BackendDTOObservableList<VolunteerDTO> VolunteerList;

    @Override
    public void shutdown(){
        NeedList.shutdown();
        CatastropheList.shutdown();
        StorageList.shutdown();
        TaskList.shutdown();;
        VolunteerList.shutdown();
    }

    @Override
    public void update(){
        NeedList.update();
        CatastropheList.update();
        StorageList.update();
        TaskList.update();
        VolunteerList.update();
    }
}
