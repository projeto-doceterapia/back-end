package br.com.doceterapia.api.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import br.com.doceterapia.api.dto.ClienteRequestDTO;
import br.com.doceterapia.api.dto.ClienteResponseDTO;
import br.com.doceterapia.api.mapper.ClienteMapper;
import br.com.doceterapia.api.service.ClienteService;
import br.com.doceterapia.api.swagger.ClienteControllerOpenApi;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/clientes")
public class ClienteController implements ClienteControllerOpenApi {

    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    @GetMapping
    @Override
    public ResponseEntity<List<ClienteResponseDTO>> listarClientes() {
        return ResponseEntity.ok(service.listarTodos().stream().map(ClienteMapper::toClienteResponse).toList());
    }

    @PostMapping
    @Override
    public ResponseEntity<ClienteResponseDTO> cadastrarCliente(
            @Valid @org.springframework.web.bind.annotation.RequestBody ClienteRequestDTO request) {
        ClienteResponseDTO response = ClienteMapper.toClienteResponse(service.cadastrar(request));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Override
    public ResponseEntity<ClienteResponseDTO> atualizarCliente(
            @PathVariable Integer id,
            @Valid @org.springframework.web.bind.annotation.RequestBody ClienteRequestDTO request) {
        ClienteResponseDTO response = ClienteMapper.toClienteResponse(service.atualizar(id, request));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<Void> deletarCliente(
            @PathVariable Integer id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
