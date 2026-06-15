package br.com.doceterapia.api.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import br.com.doceterapia.api.dto.InsumoRequestDTO;
import br.com.doceterapia.api.dto.InsumoResponseDTO;
import br.com.doceterapia.api.mapper.InsumoMapper;
import br.com.doceterapia.api.service.InsumoService;
import br.com.doceterapia.api.swagger.InsumoControllerOpenApi;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/insumos")
public class InsumoController implements InsumoControllerOpenApi {

    private final InsumoService service;
    private final InsumoMapper insumoMapper;

    public InsumoController(InsumoService service, InsumoMapper insumoMapper) {
        this.service = service;
        this.insumoMapper = insumoMapper;
    }

    @GetMapping
    @Override
    public ResponseEntity<List<InsumoResponseDTO>> listarInsumos() {
        return ResponseEntity.ok(service.listarTodos().stream().map(insumoMapper::toResponseDTO).toList());
    }

    @PostMapping
    @Override
    public ResponseEntity<InsumoResponseDTO> cadastrarInsumo(
            @Valid @org.springframework.web.bind.annotation.RequestBody InsumoRequestDTO request) {
        InsumoResponseDTO response = insumoMapper.toResponseDTO(service.cadastrar(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Override
    public ResponseEntity<InsumoResponseDTO> atualizarInsumo(
            @PathVariable Integer id,
            @Valid @org.springframework.web.bind.annotation.RequestBody InsumoRequestDTO request) {
        InsumoResponseDTO response = insumoMapper.toResponseDTO(service.atualizar(id, request));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<Void> deletarInsumo(
            @PathVariable Integer id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
