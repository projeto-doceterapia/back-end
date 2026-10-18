package br.com.doceterapia.api.controller;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import br.com.doceterapia.api.dto.ConfirmacaoEnvioRequestDTO;
import br.com.doceterapia.api.dto.NotificacaoAgendaResponseDTO;
import br.com.doceterapia.api.dto.NotificacaoRequestDTO;
import br.com.doceterapia.api.dto.NotificacaoResponseDTO;
import br.com.doceterapia.api.service.NotificacaoEmitterRegistry;
import br.com.doceterapia.api.service.NotificacaoService;
import br.com.doceterapia.api.swagger.NotificacaoControllerOpenApi;

import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/notificacoes")
public class NotificacaoController implements NotificacaoControllerOpenApi {

    private final NotificacaoService service;
    private final NotificacaoEmitterRegistry emitterRegistry;

    public NotificacaoController(NotificacaoService service, NotificacaoEmitterRegistry emitterRegistry) {
        this.service = service;
        this.emitterRegistry = emitterRegistry;
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

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Override
    public SseEmitter streamNotificacoes() {
        return emitterRegistry.registrar();
    }

    @GetMapping("/agenda")
    @Override
    public ResponseEntity<List<NotificacaoAgendaResponseDTO>> listarAgenda(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime de,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime ate) {
        return ResponseEntity.ok(service.listarAgenda(de, ate));
    }

    @PostMapping("/{id}/confirmacao-envio")
    @Override
    public ResponseEntity<NotificacaoResponseDTO> confirmarEnvio(
            @PathVariable Integer id,
            @Valid @RequestBody ConfirmacaoEnvioRequestDTO request) {
        return ResponseEntity.ok(service.confirmarEnvio(id, request));
    }
}
