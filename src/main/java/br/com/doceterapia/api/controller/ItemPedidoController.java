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
import br.com.doceterapia.api.service.PedidoConsultaService;

@RestController
@RequestMapping("/itens-pedido")
public class ItemPedidoController implements ItemPedidoControllerOpenApi {

    private final ItemPedidoService service;
    private final ItemPedidoMapper mapper;
    private final PedidoConsultaService consultaService;

    public ItemPedidoController(ItemPedidoService service, ItemPedidoMapper mapper, PedidoConsultaService consultaService) {
        this.service = service;
        this.mapper = mapper;
        this.consultaService = consultaService;
    }

    @GetMapping
    @Override
    public ResponseEntity<List<ItemPedidoResponseDTO>> listarItensPedido(
            @RequestParam(required = false) String[] pedidoId) {
        return ResponseEntity.ok(consultaService.listarItensComParametros(pedidoId));
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
