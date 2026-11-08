package br.com.doceterapia.api.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import br.com.doceterapia.api.dto.ConfiguracaoPrecificacaoRequestDTO;
import br.com.doceterapia.api.dto.ConfiguracaoPrecificacaoResponseDTO;
import br.com.doceterapia.api.service.ConfiguracaoPrecificacaoService;
import br.com.doceterapia.api.swagger.ConfiguracaoPrecificacaoControllerOpenApi;

@RestController
@RequestMapping("/configuracao-precificacao")
public class ConfiguracaoPrecificacaoController implements ConfiguracaoPrecificacaoControllerOpenApi {

    private final ConfiguracaoPrecificacaoService service;

    public ConfiguracaoPrecificacaoController(ConfiguracaoPrecificacaoService service) {
        this.service = service;
    }

    @GetMapping
    @Override
    public ResponseEntity<ConfiguracaoPrecificacaoResponseDTO> buscarConfiguracao() {
        return ResponseEntity.ok(service.buscar());
    }

    @PutMapping
    @Override
    public ResponseEntity<ConfiguracaoPrecificacaoResponseDTO> atualizarConfiguracao(
            @Valid @RequestBody ConfiguracaoPrecificacaoRequestDTO request) {
        return ResponseEntity.ok(service.atualizar(request));
    }
}
