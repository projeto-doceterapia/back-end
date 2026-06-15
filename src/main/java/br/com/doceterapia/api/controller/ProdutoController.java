package br.com.doceterapia.api.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import br.com.doceterapia.api.dto.ProdutoRequestDTO;
import br.com.doceterapia.api.dto.ProdutoResponseDTO;
import br.com.doceterapia.api.mapper.ProdutoMapper;
import br.com.doceterapia.api.service.ProdutoService;
import br.com.doceterapia.api.swagger.ProdutoControllerOpenApi;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/produtos")
public class ProdutoController implements ProdutoControllerOpenApi {

    private final ProdutoService service;
    private final ProdutoMapper mapper;

    public ProdutoController(ProdutoService service, ProdutoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    @Override
    public ResponseEntity<List<ProdutoResponseDTO>> listarProdutos() {
        return ResponseEntity.ok(service.listarTodos().stream().map(mapper::toResponseDTO).toList());
    }

    @PostMapping
    @Override
    public ResponseEntity<ProdutoResponseDTO> cadastrarProduto(
            @Valid @org.springframework.web.bind.annotation.RequestBody ProdutoRequestDTO request) {
        ProdutoResponseDTO response = mapper.toResponseDTO(service.cadastrar(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Override
    public ResponseEntity<ProdutoResponseDTO> atualizarProduto(
            @PathVariable Integer id,
            @Valid @org.springframework.web.bind.annotation.RequestBody ProdutoRequestDTO request) {
        ProdutoResponseDTO response = mapper.toResponseDTO(service.atualizar(id, request));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<Void> deletarProduto(
            @PathVariable Integer id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
