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

@CrossOrigin
@RestController
@RequestMapping("/pedidos")
public class PedidoController implements PedidoControllerOpenApi {

    private final PedidoService service;

    public PedidoController(PedidoService service) {
        this.service = service;
    }

    @PostMapping
    @Override
    public ResponseEntity<PedidoResponseDTO> cadastrarPedido(
            @Valid @org.springframework.web.bind.annotation.RequestBody PedidoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.cadastrar(request));
    }

    @GetMapping
    @Override
    public ResponseEntity<List<PedidoResponseDTO>> listarPedidos() {
        return ResponseEntity.ok(service.listarTodos());
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
