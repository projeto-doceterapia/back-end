package br.com.doceterapia.api.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import br.com.doceterapia.api.dto.ClienteRequestDTO;
import br.com.doceterapia.api.dto.ClienteResponseDTO;
import br.com.doceterapia.api.dto.ClienteResumoResponseDTO;
import br.com.doceterapia.api.mapper.ClienteMapper;
import br.com.doceterapia.api.service.ClienteService;
import br.com.doceterapia.api.swagger.ClienteControllerOpenApi;

import java.util.List;

@RestController
@RequestMapping("/clientes")
public class ClienteController implements ClienteControllerOpenApi {

    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    @GetMapping
    @Override
    public ResponseEntity<List<ClienteResponseDTO>> listarClientes(
            @RequestParam(required = false) String busca) {
        return ResponseEntity.ok(service.listar(busca).stream().map(ClienteMapper::toClienteResponse).toList());
    }

    @GetMapping("/resumo")
    @Override
    public ResponseEntity<List<ClienteResumoResponseDTO>> resumirClientes(
            @RequestParam(required = false) String busca,
            @RequestParam(defaultValue = "false") boolean incluirInativos,
            Authentication authentication) {
        return ResponseEntity.ok(service.resumir(busca, incluirInativos, ehDona(authentication)));
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

    @PatchMapping("/{id}/reativar")
    @Override
    public ResponseEntity<ClienteResponseDTO> reativarCliente(@PathVariable Integer id) {
        return ResponseEntity.ok(ClienteMapper.toClienteResponse(service.reativar(id)));
    }

    private static boolean ehDona(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_DONA"::equals);
    }
}
