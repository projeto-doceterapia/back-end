package br.com.doceterapia.api.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import br.com.doceterapia.api.dto.CategoriaInsumoRequestDTO;
import br.com.doceterapia.api.dto.CategoriaInsumoResponseDTO;
import br.com.doceterapia.api.mapper.CategoriaInsumoMapper;
import br.com.doceterapia.api.service.CategoriaInsumoService;
import br.com.doceterapia.api.swagger.CategoriaInsumoControllerOpenApi;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/categorias-insumo")
public class CategoriaInsumoController implements CategoriaInsumoControllerOpenApi {

    private final CategoriaInsumoService service;

    public CategoriaInsumoController(CategoriaInsumoService service) {
        this.service = service;
    }

    @GetMapping
    @Override
    public ResponseEntity<List<CategoriaInsumoResponseDTO>> listarCategoriasInsumo() {
        return ResponseEntity.ok(service.listarTodos().stream().map(CategoriaInsumoMapper::toResponseDTO).toList());
    }

    @PostMapping
    @Override
    public ResponseEntity<CategoriaInsumoResponseDTO> cadastrarCategoriaInsumo(
            @Valid @org.springframework.web.bind.annotation.RequestBody CategoriaInsumoRequestDTO request) {
        CategoriaInsumoResponseDTO response = CategoriaInsumoMapper.toResponseDTO(service.cadastrar(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Override
    public ResponseEntity<CategoriaInsumoResponseDTO> atualizarCategoriaInsumo(
            @PathVariable Integer id,
            @Valid @org.springframework.web.bind.annotation.RequestBody CategoriaInsumoRequestDTO request) {
        CategoriaInsumoResponseDTO response = CategoriaInsumoMapper.toResponseDTO(service.atualizar(id, request));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<Void> deletarCategoriaInsumo(
            @PathVariable Integer id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
