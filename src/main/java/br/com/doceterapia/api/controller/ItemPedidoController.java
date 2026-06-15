package br.com.doceterapia.api.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import br.com.doceterapia.api.dto.ItemPedidoRequestDTO;
import br.com.doceterapia.api.dto.ItemPedidoResponseDTO;
import br.com.doceterapia.api.mapper.ItemPedidoMapper;
import br.com.doceterapia.api.service.ItemPedidoService;
import br.com.doceterapia.api.swagger.ItemPedidoControllerOpenApi;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/itens-pedido")
public class ItemPedidoController implements ItemPedidoControllerOpenApi {

    private final ItemPedidoService service;
    private final ItemPedidoMapper mapper;

    public ItemPedidoController(ItemPedidoService service, ItemPedidoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    @Override
    public ResponseEntity<List<ItemPedidoResponseDTO>> listarItensPedido() {
        return ResponseEntity.ok(service.listarTodos().stream().map(mapper::toResponseDTO).toList());
    }

    @PostMapping
    @Override
    public ResponseEntity<ItemPedidoResponseDTO> cadastrarItemPedido(
            @Valid @org.springframework.web.bind.annotation.RequestBody ItemPedidoRequestDTO request) {
        ItemPedidoResponseDTO response = mapper.toResponseDTO(service.cadastrar(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Override
    public ResponseEntity<ItemPedidoResponseDTO> atualizarItemPedido(
            @PathVariable Integer id,
            @Valid @org.springframework.web.bind.annotation.RequestBody ItemPedidoRequestDTO request) {
        ItemPedidoResponseDTO response = mapper.toResponseDTO(service.atualizar(id, request));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<Void> deletarItemPedido(
            @PathVariable Integer id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
