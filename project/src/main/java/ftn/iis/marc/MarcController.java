package ftn.iis.marc;

import ftn.iis.marc.dto.MarcRecordDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/marc")
public class MarcController {

    private final MarcMappingService marcMappingService;

    public MarcController(MarcMappingService marcMappingService) {
        this.marcMappingService = marcMappingService;
    }

    @GetMapping("/{isbn}")
    public ResponseEntity<MarcRecordDto> getMarcRecord(@PathVariable String isbn) {
        return marcMappingService.buildRecord(isbn)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
