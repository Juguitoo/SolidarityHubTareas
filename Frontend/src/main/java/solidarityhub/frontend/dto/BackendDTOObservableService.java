package solidarityhub.frontend.dto;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.pingu.domain.DTO.AffectedDTO;
import org.pingu.domain.DTO.RouteDTO;
import org.pingu.domain.DTO.RoutePointDTO;
import org.pingu.domain.DTO.ZoneDTO;
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
            NeedList = new BackendDTOObservableList<>("/api/needs", 3, new ParameterizedTypeReference<>() {});
            Thread.sleep(1000); // Espera entre servicios para no explotar el back

            AffectedList = new BackendDTOObservableList<>("/api/affecteds", 3,  new ParameterizedTypeReference<>() {});
            Thread.sleep(1000);

            CatastropheList = new BackendDTOObservableList<>("/api/catastrophes", 5,  new ParameterizedTypeReference<>() {});
            Thread.sleep(1000);

            RouteList = new BackendDTOObservableList<>("/api/routes", 3,  new ParameterizedTypeReference<>() {});
            Thread.sleep(1000);

            RoutePointList = new BackendDTOObservableList<>("/api/routepoints", 3,  new ParameterizedTypeReference<>() {});
            Thread.sleep(1000);

            StorageList = new BackendDTOObservableList<>("/api/storages", 5,  new ParameterizedTypeReference<>() {});
            Thread.sleep(1000);

            TaskList = new BackendDTOObservableList<>("/api/tasks", 3, new ParameterizedTypeReference<>() {});
            Thread.sleep(1000);

            VolunteerList = new BackendDTOObservableList<>("/api/volunteers", 3,  new ParameterizedTypeReference<>() {});
            Thread.sleep(1000);

            ZoneList = new BackendDTOObservableList<>("/api/zones", 3,  new ParameterizedTypeReference<>() {});
        }
        catch (InterruptedException e){
            log.error("Error en una pausa del servicio {} {}", e.getMessage(), e.getStackTrace());
            throw new RuntimeException();
        }
    }

    private final BackendDTOObservableList<NeedDTO> NeedList;
    private final BackendDTOObservableList<AffectedDTO> AffectedList;
    private final BackendDTOObservableList<CatastropheDTO> CatastropheList;
    private final BackendDTOObservableList<RouteDTO> RouteList;
    private final BackendDTOObservableList<RoutePointDTO> RoutePointList;
    private final BackendDTOObservableList<StorageDTO> StorageList;
    private final BackendDTOObservableList<TaskDTO> TaskList;
    private final BackendDTOObservableList<VolunteerDTO> VolunteerList;
    private final BackendDTOObservableList<ZoneDTO> ZoneList;

    @Override
    public void shutdown(){
        NeedList.shutdown();
        AffectedList.shutdown();
        CatastropheList.shutdown();
        RouteList.shutdown();
        RoutePointList.shutdown();
        StorageList.shutdown();
        TaskList.shutdown();;
        VolunteerList.shutdown();
        ZoneList.shutdown();
    }

    @Override
    public void update(){
        NeedList.update();
        AffectedList.update();
        CatastropheList.update();
        RouteList.update();
        RoutePointList.update();
        StorageList.update();
        TaskList.update();
        VolunteerList.update();
        ZoneList.update();
    }
}
