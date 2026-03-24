package school.sptech.projetoindividual.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import school.sptech.projetoindividual.dto.ClienteRequestDTO;
import school.sptech.projetoindividual.dto.ClienteResponseDTO;
import school.sptech.projetoindividual.entity.Cliente;
import school.sptech.projetoindividual.mapper.ClienteMapper;
import school.sptech.projetoindividual.service.ClienteService;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> listarClientes () {
        return ResponseEntity.ok(service.listarTodos().stream().map(ClienteMapper::toClienteResponse).toList());
    }

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> cadastrarCliente (@Valid @RequestBody ClienteRequestDTO request) {
        ClienteResponseDTO response = ClienteMapper.toClienteResponse(service.cadastrar(request));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
