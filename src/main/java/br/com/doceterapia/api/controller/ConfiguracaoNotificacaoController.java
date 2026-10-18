package br.com.doceterapia.api.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import br.com.doceterapia.api.dto.ConfiguracaoNotificacaoRequestDTO;
import br.com.doceterapia.api.dto.ConfiguracaoNotificacaoResponseDTO;
import br.com.doceterapia.api.service.ConfiguracaoNotificacaoService;
import br.com.doceterapia.api.swagger.ConfiguracaoNotificacaoControllerOpenApi;

@CrossOrigin
@RestController
@RequestMapping("/configuracao-notificacao")
public class ConfiguracaoNotificacaoController implements ConfiguracaoNotificacaoControllerOpenApi {

    private final ConfiguracaoNotificacaoService service;

    public ConfiguracaoNotificacaoController(ConfiguracaoNotificacaoService service) {
        this.service = service;
    }

    @GetMapping
    @Override
    public ResponseEntity<ConfiguracaoNotificacaoResponseDTO> buscarConfiguracao() {
        return ResponseEntity.ok(service.buscar());
    }

    @PutMapping
    @Override
    public ResponseEntity<ConfiguracaoNotificacaoResponseDTO> atualizarConfiguracao(
            @Valid @RequestBody ConfiguracaoNotificacaoRequestDTO request) {
        return ResponseEntity.ok(service.atualizar(request));
    }
}
