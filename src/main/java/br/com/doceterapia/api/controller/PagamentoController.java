package br.com.doceterapia.api.controller;

import br.com.doceterapia.api.dto.PagamentoResponseDTO;
import br.com.doceterapia.api.dto.RegistroPagamentoRequestDTO;
import br.com.doceterapia.api.service.PagamentoService;
import br.com.doceterapia.api.swagger.PagamentoControllerOpenApi;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/pagamentos")
public class PagamentoController implements PagamentoControllerOpenApi {

    private final PagamentoService service;

    public PagamentoController(PagamentoService service) {
        this.service = service;
    }

    @GetMapping
    @Override
    public ResponseEntity<List<PagamentoResponseDTO>> listarPagamentos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<PagamentoResponseDTO> buscarPagamentoPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/pedido/{pedidoId}")
    @Override
    public ResponseEntity<PagamentoResponseDTO> buscarPagamentoPorPedidoId(@PathVariable Integer pedidoId) {
        return ResponseEntity.ok(service.buscarPorPedidoId(pedidoId));
    }

    @PatchMapping("/{id}/registrar-sinal")
    @Override
    public ResponseEntity<PagamentoResponseDTO> registrarSinal(@PathVariable Integer id,
            @Valid @RequestBody RegistroPagamentoRequestDTO request) {
        return ResponseEntity.ok(service.registrarSinal(id, request));
    }

    @PatchMapping("/{id}/registrar-restante")
    @Override
    public ResponseEntity<PagamentoResponseDTO> registrarRestante(@PathVariable Integer id,
            @Valid @RequestBody RegistroPagamentoRequestDTO request) {
        return ResponseEntity.ok(service.registrarRestante(id, request));
    }

}
