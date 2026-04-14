package school.sptech.projetoindividual.controller;


import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import school.sptech.projetoindividual.dto.PedidoRequestDTO;
import school.sptech.projetoindividual.dto.PedidoResponseDTO;
import school.sptech.projetoindividual.dto.PedidoWithClienteResponseDTO;
import school.sptech.projetoindividual.mapper.PedidoMapper;
import school.sptech.projetoindividual.service.PedidoService;
import school.sptech.projetoindividual.swagger.PedidoControllerOpenApi;

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
        PedidoResponseDTO response = PedidoMapper.toPedidoResponse(service.cadastrar(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Override
    public ResponseEntity<List<PedidoWithClienteResponseDTO>> listarPedidos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @PutMapping("/{id}")
    @Override
    public ResponseEntity<PedidoResponseDTO> atualizarPedido(
            @PathVariable Integer id,
            @Valid @org.springframework.web.bind.annotation.RequestBody PedidoRequestDTO request) {
        PedidoResponseDTO response = PedidoMapper.toPedidoResponse(service.atualizar(id, request));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<Void> deletarPedido(
            @PathVariable Integer id) {
        service.deletar(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/status")
    @Override
    public ResponseEntity<Void> atualizarStatus(
            @RequestParam Integer id,
            @RequestParam Boolean status) {
        service.atualizarStatus(id, status);
        return ResponseEntity.ok().build();
    }
}
