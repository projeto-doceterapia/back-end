package br.com.doceterapia.api.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import br.com.doceterapia.api.dto.PedidoRequestDTO;
import br.com.doceterapia.api.dto.PedidoResponseDTO;
import br.com.doceterapia.api.enums.StatusPedido;
import br.com.doceterapia.api.service.PedidoService;
import br.com.doceterapia.api.swagger.PedidoControllerOpenApi;

import java.util.List;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import br.com.doceterapia.api.service.PedidoConsultaService;

@RestController
@RequestMapping("/pedidos")
public class PedidoController implements PedidoControllerOpenApi {

    private final PedidoService service;
    private final PedidoConsultaService consultaService;

    public PedidoController(PedidoService service, PedidoConsultaService consultaService) {
        this.service = service;
        this.consultaService = consultaService;
    }

    @PostMapping
    @Override
    public ResponseEntity<PedidoResponseDTO> cadastrarPedido(
            @Valid @org.springframework.web.bind.annotation.RequestBody PedidoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.cadastrar(request));
    }

    @GetMapping
    @Override
    public ResponseEntity<List<PedidoResponseDTO>> listarPedidos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
            @RequestParam(required = false) String[] status,
            @RequestParam(required = false) Integer clienteId) {
        return ResponseEntity.ok(consultaService.listarPedidosComParametros(de, ate, status, clienteId));
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<PedidoResponseDTO> buscarPedido(@PathVariable Integer id) {
        return ResponseEntity.ok(consultaService.buscarPedido(id));
    }

    @PutMapping("/{id}")
    @Override
    public ResponseEntity<PedidoResponseDTO> atualizarPedido(
            @PathVariable Integer id,
            @Valid @org.springframework.web.bind.annotation.RequestBody PedidoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.OK).body(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<Void> deletarPedido(
            @PathVariable Integer id) {
        service.deletar(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{id}/status")
    @Override
    public ResponseEntity<PedidoResponseDTO> atualizarStatus(
            @PathVariable Integer id,
            @RequestParam StatusPedido statusPedido) {
        return ResponseEntity.ok(service.atualizarStatus(id, statusPedido));
    }
}
