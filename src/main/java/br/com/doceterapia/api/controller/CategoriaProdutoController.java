package br.com.doceterapia.api.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import br.com.doceterapia.api.dto.CategoriaProdutoRequestDTO;
import br.com.doceterapia.api.dto.CategoriaProdutoResponseDTO;
import br.com.doceterapia.api.mapper.CategoriaProdutoMapper;
import br.com.doceterapia.api.service.CategoriaProdutoService;
import br.com.doceterapia.api.swagger.CategoriaProdutoControllerOpenApi;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/categorias-produto")
public class CategoriaProdutoController implements CategoriaProdutoControllerOpenApi {

    private final CategoriaProdutoService service;

    public CategoriaProdutoController(CategoriaProdutoService service) {
        this.service = service;
    }

    @GetMapping
    @Override
    public ResponseEntity<List<CategoriaProdutoResponseDTO>> listarCategorias() {
        return ResponseEntity.ok(service.listarTodos().stream().map(CategoriaProdutoMapper::toResponseDTO).toList());
    }

    @PostMapping
    @Override
    public ResponseEntity<CategoriaProdutoResponseDTO> cadastrarCategoria(
            @Valid @org.springframework.web.bind.annotation.RequestBody CategoriaProdutoRequestDTO request) {
        CategoriaProdutoResponseDTO response = CategoriaProdutoMapper.toResponseDTO(service.cadastrar(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Override
    public ResponseEntity<CategoriaProdutoResponseDTO> atualizarCategoria(
            @PathVariable Integer id,
            @Valid @org.springframework.web.bind.annotation.RequestBody CategoriaProdutoRequestDTO request) {
        CategoriaProdutoResponseDTO response = CategoriaProdutoMapper.toResponseDTO(service.atualizar(id, request));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<Void> deletarCategoria(
            @PathVariable Integer id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
