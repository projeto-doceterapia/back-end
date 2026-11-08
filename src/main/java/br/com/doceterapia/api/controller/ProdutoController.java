package br.com.doceterapia.api.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import br.com.doceterapia.api.dto.ProdutoRequestDTO;
import br.com.doceterapia.api.dto.ProdutoResponseDTO;
import br.com.doceterapia.api.service.ProdutoService;
import br.com.doceterapia.api.swagger.ProdutoControllerOpenApi;

import java.util.List;

@RestController
@RequestMapping("/produtos")
public class ProdutoController implements ProdutoControllerOpenApi {

    private final ProdutoService service;

    public ProdutoController(ProdutoService service) {
        this.service = service;
    }

    @GetMapping
    @Override
    public ResponseEntity<List<ProdutoResponseDTO>> listarProdutos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<ProdutoResponseDTO> buscarProduto(@PathVariable Integer id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    @Override
    public ResponseEntity<ProdutoResponseDTO> cadastrarProduto(
            @Valid @org.springframework.web.bind.annotation.RequestBody ProdutoRequestDTO request) {
        ProdutoResponseDTO response = service.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Override
    public ResponseEntity<ProdutoResponseDTO> atualizarProduto(
            @PathVariable Integer id,
            @Valid @org.springframework.web.bind.annotation.RequestBody ProdutoRequestDTO request) {
        ProdutoResponseDTO response = service.atualizar(id, request);
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
