package solidarityhub.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import solidarityhub.backend.dto.NeedDTO;
import solidarityhub.backend.service.NeedService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/needs")
public class NeedController {
    private final NeedService needService;

    public NeedController(NeedService needService) {
        this.needService = needService;
    }

    @GetMapping
    public ResponseEntity<?> getAllNeeds() {
        List<NeedDTO> needDTOList = new ArrayList<>();
        needService.getAllNeeds().forEach(n -> {needDTOList.add(new NeedDTO(n));});
        return ResponseEntity.ok(needDTOList);
    }
}
