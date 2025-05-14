package solidarityhub.frontend.dto;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.pingu.web.BackendObservableService.BackendObservableService;
import org.pingu.web.BackendObservableService.Singleton;
import org.pingu.web.BackendObservableService.observableList.concrete.DTOObservableList;
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

            CatastropheList = new DTOObservableList<>("http://localhost:8082/solidarityhub/catastrophes", 5,  new ParameterizedTypeReference<>() {});
            Thread.sleep(1000);

            StorageList = new DTOObservableList<>("http://localhost:8082/solidarityhub/storages", 5,  new ParameterizedTypeReference<>() {});
            Thread.sleep(1000);

            TaskList = new DTOObservableList<>("http://localhost:8082/solidarityhub/tasks", 3, new ParameterizedTypeReference<>() {});
            Thread.sleep(1000);
        }
        catch (InterruptedException e){
            log.error("Error en una pausa del servicio {} {}", e.getMessage(), e.getStackTrace());
            throw new RuntimeException();
        }
    }

    private final DTOObservableList<CatastropheDTO> CatastropheList;
    private final DTOObservableList<StorageDTO> StorageList;
    private final DTOObservableList<TaskDTO> TaskList;

    @Override
    public void shutdown(){
        CatastropheList.shutdown();
        StorageList.shutdown();
        TaskList.shutdown();;
    }

    @Override
    public void update(){
        CatastropheList.update();
        StorageList.update();
        TaskList.update();
    }
}
