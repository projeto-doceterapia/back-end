package br.com.doceterapia.api.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import br.com.doceterapia.api.dto.ConfiguracaoFarolRequestDTO;
import br.com.doceterapia.api.dto.ConfiguracaoFarolResponseDTO;
import br.com.doceterapia.api.service.ConfiguracaoFarolAgendaService;
import br.com.doceterapia.api.swagger.ConfiguracaoFarolAgendaControllerOpenApi;

@CrossOrigin
@RestController
@RequestMapping("/configuracao-farol")
public class ConfiguracaoFarolAgendaController implements ConfiguracaoFarolAgendaControllerOpenApi {

    private final ConfiguracaoFarolAgendaService service;

    public ConfiguracaoFarolAgendaController(ConfiguracaoFarolAgendaService service) {
        this.service = service;
    }

    @PostMapping
    @Override
    public ResponseEntity<ConfiguracaoFarolResponseDTO> cadastrarConfiguracao(
            @Valid @RequestBody ConfiguracaoFarolRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(request));
    }

    @GetMapping
    @Override
    public ResponseEntity<ConfiguracaoFarolResponseDTO> buscarConfiguracao() {
        return ResponseEntity.ok(service.buscar());
    }

    @PutMapping
    @Override
    public ResponseEntity<ConfiguracaoFarolResponseDTO> atualizarConfiguracao(
            @Valid @RequestBody ConfiguracaoFarolRequestDTO request) {
        return ResponseEntity.ok(service.atualizar(request));
    }
}
