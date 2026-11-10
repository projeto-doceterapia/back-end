package br.com.doceterapia.api.controller;

import br.com.doceterapia.api.dto.IndicadoresFinanceirosResponseDTO;
import br.com.doceterapia.api.dto.IndicadoresOperacionaisResponseDTO;
import br.com.doceterapia.api.service.IndicadorService;
import br.com.doceterapia.api.service.PeriodoConsulta;
import br.com.doceterapia.api.swagger.IndicadorControllerOpenApi;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/indicadores")
public class IndicadorController implements IndicadorControllerOpenApi {

    private final IndicadorService service;

    public IndicadorController(IndicadorService service) {
        this.service = service;
    }

    @GetMapping("/operacionais")
    @Override
    public ResponseEntity<IndicadoresOperacionaisResponseDTO> operacionais(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
        return ResponseEntity.ok(service.operacionais(PeriodoConsulta.de(de, ate)));
    }

    @GetMapping("/financeiros")
    @Override
    public ResponseEntity<IndicadoresFinanceirosResponseDTO> financeiros(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
        return ResponseEntity.ok(service.financeiros(PeriodoConsulta.de(de, ate)));
    }
}
