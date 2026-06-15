package br.com.doceterapia.api.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import br.com.doceterapia.api.dto.NotificacaoRequestDTO;
import br.com.doceterapia.api.dto.NotificacaoResponseDTO;
import br.com.doceterapia.api.service.NotificacaoService;
import br.com.doceterapia.api.swagger.NotificacaoControllerOpenApi;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/notificacoes")
public class NotificacaoController implements NotificacaoControllerOpenApi {

    private final NotificacaoService service;

    public NotificacaoController(NotificacaoService service) {
        this.service = service;
    }

    @PostMapping
    @Override
    public ResponseEntity<NotificacaoResponseDTO> cadastrarNotificacao(
            @Valid @RequestBody NotificacaoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.cadastrar(request));
    }

    @GetMapping
    @Override
    public ResponseEntity<List<NotificacaoResponseDTO>> listarNotificacoes() {
        return ResponseEntity.ok(service.listarTodas());
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<NotificacaoResponseDTO> buscarNotificacao(@PathVariable Integer id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Override
    public ResponseEntity<NotificacaoResponseDTO> atualizarNotificacao(
            @PathVariable Integer id,
            @Valid @RequestBody NotificacaoRequestDTO request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<Void> deletarNotificacao(@PathVariable Integer id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/marcar-lida")
    @Override
    public ResponseEntity<NotificacaoResponseDTO> marcarNotificacaoLida(@PathVariable Integer id) {
        return ResponseEntity.ok(service.marcarLida(id));
    }

    @GetMapping("/nao-lidas")
    @Override
    public ResponseEntity<List<NotificacaoResponseDTO>> listarNotificacoesNaoLidas() {
        return ResponseEntity.ok(service.listarNaoLidas());
    }
}
