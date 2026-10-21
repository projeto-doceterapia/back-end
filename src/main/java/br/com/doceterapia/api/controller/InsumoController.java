package br.com.doceterapia.api.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import br.com.doceterapia.api.dto.InsumoRequestDTO;
import br.com.doceterapia.api.dto.InsumoResponseDTO;
import br.com.doceterapia.api.dto.InsumoCatalogoResponseDTO;
import br.com.doceterapia.api.dto.ObterOuCriarInsumoRequestDTO;
import br.com.doceterapia.api.mapper.InsumoMapper;
import br.com.doceterapia.api.service.InsumoCatalogoService;
import br.com.doceterapia.api.service.InsumoService;
import br.com.doceterapia.api.swagger.InsumoControllerOpenApi;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/insumos")
public class InsumoController implements InsumoControllerOpenApi {

    private final InsumoService service;
    private final InsumoCatalogoService catalogoService;
    private final InsumoMapper insumoMapper;

    public InsumoController(InsumoService service, InsumoCatalogoService catalogoService, InsumoMapper insumoMapper) {
        this.service = service;
        this.catalogoService = catalogoService;
        this.insumoMapper = insumoMapper;
    }

    @GetMapping
    @Override
    public ResponseEntity<List<InsumoResponseDTO>> listarInsumos() {
        return ResponseEntity.ok(service.listarTodos().stream().map(insumoMapper::toResponseDTO).toList());
    }

    @GetMapping("/catalogo")
    @Override
    public ResponseEntity<InsumoCatalogoResponseDTO> listarCatalogo(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer categoria,
            @RequestParam(required = false) Integer limite,
            @RequestParam(required = false) String cursor) {
        return ResponseEntity.ok(catalogoService.listarCatalogo(q, categoria, limite, cursor));
    }

    @PostMapping("/obter-ou-criar")
    @Override
    public ResponseEntity<InsumoResponseDTO> obterOuCriarInsumo(
            @Valid @org.springframework.web.bind.annotation.RequestBody ObterOuCriarInsumoRequestDTO request) {
        InsumoCatalogoService.ObterOuCriarResultado resultado = catalogoService.obterOuCriar(request);
        InsumoResponseDTO response = insumoMapper.toResponseDTO(resultado.insumo());
        HttpStatus status = resultado.criado() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
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
