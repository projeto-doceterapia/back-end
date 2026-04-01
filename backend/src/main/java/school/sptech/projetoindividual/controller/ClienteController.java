package school.sptech.projetoindividual.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
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
@Tag(name = "Clientes", description = "Endpoints para gerenciamento de clientes")
public class ClienteController {

    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar todos os clientes", description = "Retorna uma lista com todos os clientes cadastrados no sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Clientes listados com sucesso",
                    content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = ClienteResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<List<ClienteResponseDTO>> listarClientes() {
        return ResponseEntity.ok(service.listarTodos().stream().map(ClienteMapper::toClienteResponse).toList());
    }

    @PostMapping
    @Operation(summary = "Cadastrar novo cliente", 
            description = "Cria um novo cliente no sistema com os dados fornecidos")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cliente cadastrado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ClienteResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos:\n" +
                    "- Nome completo vazio\n" +
                    "- Telefone vazio ou duplicado\n" +
                    "- Endereço vazio"),
            @ApiResponse(responseCode = "409", description = "Telefone já cadastrado para outro cliente"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<ClienteResponseDTO> cadastrarCliente(
            @RequestBody(description = "Dados do cliente a ser cadastrado", required = true,
                    content = @Content(schema = @Schema(implementation = ClienteRequestDTO.class)))
            @Valid @org.springframework.web.bind.annotation.RequestBody ClienteRequestDTO request) {
        ClienteResponseDTO response = ClienteMapper.toClienteResponse(service.cadastrar(request));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
