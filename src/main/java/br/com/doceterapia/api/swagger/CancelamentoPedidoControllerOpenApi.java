package br.com.doceterapia.api.swagger;

import br.com.doceterapia.api.dto.CancelamentoPedidoRequestDTO;
import br.com.doceterapia.api.dto.CancelamentoPedidoResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Cancelamentos", description = "Endpoints para gerenciamento de cancelamentos de pedidos")
public interface CancelamentoPedidoControllerOpenApi {

    @Operation(summary = "Cadastrar novo cancelamento",
            description = "Registra o cancelamento de um pedido no sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cancelamento cadastrado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CancelamentoPedidoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "404", description = "Pedido com ID fornecido não encontrado"),
            @ApiResponse(responseCode = "409", description = "Já existe um cancelamento para este pedido"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<CancelamentoPedidoResponseDTO> cadastrarCancelamento(
            @Parameter(description = "ID do pedido a ser cancelado", example = "1") Integer pedidoId,
            @RequestBody(description = "Dados do cancelamento a ser cadastrado", required = true,
                    content = @Content(schema = @Schema(implementation = CancelamentoPedidoRequestDTO.class)))
            CancelamentoPedidoRequestDTO request);

    @Operation(summary = "Listar todos os cancelamentos",
            description = "Retorna uma lista com todos os cancelamentos cadastrados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cancelamentos listados com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CancelamentoPedidoResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<List<CancelamentoPedidoResponseDTO>> listarCancelamentos();

    @Operation(summary = "Buscar cancelamento por ID",
            description = "Retorna um cancelamento pelo seu ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cancelamento encontrado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CancelamentoPedidoResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Cancelamento não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<CancelamentoPedidoResponseDTO> buscarCancelamentoPorId(
            @Parameter(description = "ID do cancelamento", example = "1") Integer id);

    @Operation(summary = "Buscar cancelamento por ID do pedido",
            description = "Retorna um cancelamento pelo ID do pedido associado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cancelamento encontrado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CancelamentoPedidoResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Cancelamento não encontrado para este pedido"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<CancelamentoPedidoResponseDTO> buscarCancelamentoPorPedidoId(
            @Parameter(description = "ID do pedido", example = "1") Integer pedidoId);

    @Operation(summary = "Atualizar cancelamento",
            description = "Atualiza os dados de um cancelamento existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cancelamento atualizado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CancelamentoPedidoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "404", description = "Cancelamento com ID fornecido não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<CancelamentoPedidoResponseDTO> atualizarCancelamento(
            @Parameter(description = "ID do cancelamento a ser atualizado", example = "1") Integer id,
            @RequestBody(description = "Novos dados do cancelamento", required = true,
                    content = @Content(schema = @Schema(implementation = CancelamentoPedidoRequestDTO.class)))
            CancelamentoPedidoRequestDTO request);

    @Operation(summary = "Deletar cancelamento",
            description = "Remove um cancelamento do sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cancelamento deletado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Cancelamento com ID fornecido não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> deletarCancelamento(
            @Parameter(description = "ID do cancelamento a ser deletado", example = "1") Integer id);
}
