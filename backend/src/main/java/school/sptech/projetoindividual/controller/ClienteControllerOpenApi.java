package school.sptech.projetoindividual.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import school.sptech.projetoindividual.dto.ClienteRequestDTO;
import school.sptech.projetoindividual.dto.ClienteResponseDTO;

import java.util.List;

@Tag(name = "Clientes", description = "Endpoints para gerenciamento de clientes")
public interface ClienteControllerOpenApi {

    @Operation(summary = "Listar todos os clientes", description = "Retorna uma lista com todos os clientes cadastrados no sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Clientes listados com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ClienteResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
        ResponseEntity<List<ClienteResponseDTO>> listarClientes();

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
    ResponseEntity<ClienteResponseDTO> cadastrarCliente(
            @RequestBody(description = "Dados do cliente a ser cadastrado", required = true,
                    content = @Content(schema = @Schema(implementation = ClienteRequestDTO.class)))
            ClienteRequestDTO request);

    @Operation(summary = "Atualizar cliente",
            description = "Atualiza os dados de um cliente existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente atualizado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ClienteResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos:\n" +
                    "- Nome completo vazio\n" +
                    "- Telefone vazio ou duplicado\n" +
                    "- Endereço vazio"),
            @ApiResponse(responseCode = "404", description = "Cliente com ID fornecido não encontrado"),
            @ApiResponse(responseCode = "409", description = "Telefone já cadastrado para outro cliente"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<ClienteResponseDTO> atualizarCliente(
            @Parameter(description = "ID do cliente a ser atualizado", example = "1")
            Integer id,
            @RequestBody(description = "Novos dados do cliente", required = true,
                    content = @Content(schema = @Schema(implementation = ClienteRequestDTO.class)))
            ClienteRequestDTO request);

    @Operation(summary = "Deletar cliente",
            description = "Remove um cliente do sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cliente deletado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Cliente com ID fornecido não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
        ResponseEntity<Void> deletarCliente(
            @Parameter(description = "ID do cliente a ser deletado", example = "1")
            Integer id);
}