package br.com.doceterapia.api.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import br.com.doceterapia.api.dto.ProducaoRequestDTO;
import br.com.doceterapia.api.dto.ProducaoResponseDTO;
import br.com.doceterapia.api.enums.StatusProducao;
import br.com.doceterapia.api.mapper.ProducaoMapper;
import br.com.doceterapia.api.service.ProducaoService;
import br.com.doceterapia.api.swagger.ProducaoControllerOpenApi;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/producoes")
public class ProducaoController implements ProducaoControllerOpenApi {

    private final ProducaoService service;
    private final ProducaoMapper mapper;

    public ProducaoController(ProducaoService service, ProducaoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    @Override
    public ResponseEntity<List<ProducaoResponseDTO>> listarProducoes() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<ProducaoResponseDTO> buscarProducao(@PathVariable Integer id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    @Override
    public ResponseEntity<ProducaoResponseDTO> cadastrarProducao(
            @Valid @RequestBody ProducaoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.cadastrar(request));
    }

    @PutMapping("/{id}")
    @Override
    public ResponseEntity<ProducaoResponseDTO> atualizarProducao(
            @PathVariable Integer id,
            @Valid @RequestBody ProducaoRequestDTO request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<Void> deletarProducao(@PathVariable Integer id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @Override
    public ResponseEntity<ProducaoResponseDTO> atualizarStatus(
            @PathVariable Integer id,
            @RequestParam StatusProducao statusProducao) {
        return ResponseEntity.ok(service.atualizarStatus(id, statusProducao));
    }
}
