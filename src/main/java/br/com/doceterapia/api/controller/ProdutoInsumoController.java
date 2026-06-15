package br.com.doceterapia.api.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import br.com.doceterapia.api.dto.ProdutoInsumoRequestDTO;
import br.com.doceterapia.api.dto.ProdutoInsumoResponseDTO;
import br.com.doceterapia.api.mapper.ProdutoInsumoMapper;
import br.com.doceterapia.api.service.ProdutoInsumoService;
import br.com.doceterapia.api.swagger.ProdutoInsumoControllerOpenApi;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/produtos/{produtoId}/insumos")
public class ProdutoInsumoController implements ProdutoInsumoControllerOpenApi {

    private final ProdutoInsumoService service;
    private final ProdutoInsumoMapper mapper;

    public ProdutoInsumoController(ProdutoInsumoService service, ProdutoInsumoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    @Override
    public ResponseEntity<List<ProdutoInsumoResponseDTO>> listarInsumosPorProduto(
            @PathVariable Integer produtoId) {
        return ResponseEntity.ok(service.listarPorProduto(produtoId).stream().map(mapper::toResponseDTO).toList());
    }

    @PostMapping
    @Override
    public ResponseEntity<ProdutoInsumoResponseDTO> cadastrarInsumoNoProduto(
            @PathVariable Integer produtoId,
            @Valid @org.springframework.web.bind.annotation.RequestBody ProdutoInsumoRequestDTO request) {
        ProdutoInsumoResponseDTO response = mapper.toResponseDTO(service.cadastrar(produtoId, request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Override
    public ResponseEntity<ProdutoInsumoResponseDTO> atualizarInsumoDoProduto(
            @PathVariable Integer produtoId,
            @PathVariable Integer id,
            @Valid @org.springframework.web.bind.annotation.RequestBody ProdutoInsumoRequestDTO request) {
        ProdutoInsumoResponseDTO response = mapper.toResponseDTO(service.atualizar(id, request));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<Void> deletarInsumoDoProduto(
            @PathVariable Integer produtoId,
            @PathVariable Integer id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
