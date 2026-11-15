package br.com.doceterapia.api.controller;

import br.com.doceterapia.api.dto.CancelamentoPedidoRequestDTO;
import br.com.doceterapia.api.dto.CancelamentoPedidoResponseDTO;
import br.com.doceterapia.api.service.CancelamentoPedidoService;
import br.com.doceterapia.api.swagger.CancelamentoPedidoControllerOpenApi;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.format.annotation.DateTimeFormat;
import br.com.doceterapia.api.service.PedidoConsultaService;

@RestController
@RequestMapping("/cancelamentos")
public class CancelamentoPedidoController implements CancelamentoPedidoControllerOpenApi {

    private final CancelamentoPedidoService service;
    private final PedidoConsultaService consultaService;

    public CancelamentoPedidoController(CancelamentoPedidoService service, PedidoConsultaService consultaService) {
        this.service = service;
        this.consultaService = consultaService;
    }

    @PostMapping("/pedido/{pedidoId}")
    @Override
    public ResponseEntity<CancelamentoPedidoResponseDTO> cadastrarCancelamento(
            @PathVariable Integer pedidoId,
            @Valid @RequestBody CancelamentoPedidoRequestDTO request) {
        CancelamentoPedidoResponseDTO response = service.criar(pedidoId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Override
    public ResponseEntity<List<CancelamentoPedidoResponseDTO>> listarCancelamentos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
        return ResponseEntity.ok(consultaService.listarCancelamentos(de, ate));
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<CancelamentoPedidoResponseDTO> buscarCancelamentoPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/pedido/{pedidoId}")
    @Override
    public ResponseEntity<CancelamentoPedidoResponseDTO> buscarCancelamentoPorPedidoId(@PathVariable Integer pedidoId) {
        return ResponseEntity.ok(service.buscarPorPedidoId(pedidoId));
    }

    @PutMapping("/{id}")
    @Override
    public ResponseEntity<CancelamentoPedidoResponseDTO> atualizarCancelamento(
            @PathVariable Integer id,
            @Valid @RequestBody CancelamentoPedidoRequestDTO request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<Void> deletarCancelamento(@PathVariable Integer id) {
        service.deletar(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
