package ftn.iis.controller;

import ftn.iis.dto.BudzetPoZanruResponseDto;
import ftn.iis.dto.PostaviBudzetDto;
import ftn.iis.dto.PreraspodelaBudzetaDto;
import ftn.iis.service.BudzetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/budzet")
public class BudzetController {
    private final BudzetService budzetService;

    public BudzetController(BudzetService budzetService) {
        this.budzetService = budzetService;
    }

    @GetMapping("/sve-po-zanrovima")
    public ResponseEntity<List<BudzetPoZanruResponseDto>> getSviBudzetiPoZanrovima(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(budzetService.getSviBudzetiPoZanrovima(authHeader.substring(7)));
    }

    @PostMapping("/postavi")
    public ResponseEntity<BudzetPoZanruResponseDto> postaviBudzet( @RequestHeader("Authorization") String authHeader,
                                                                   @RequestBody @Valid PostaviBudzetDto dto) {
        return ResponseEntity.ok(budzetService.postaviBudzet(authHeader.substring(7), dto));
    }

    @PostMapping("/prerasporedi")
    public ResponseEntity<List<BudzetPoZanruResponseDto>> prerasporedi( @RequestHeader("Authorization") String authHeader,
                                                                        @RequestBody @Valid PreraspodelaBudzetaDto dto) {
        return ResponseEntity.ok(budzetService.prerasporedi(authHeader.substring(7), dto));
    }

}
