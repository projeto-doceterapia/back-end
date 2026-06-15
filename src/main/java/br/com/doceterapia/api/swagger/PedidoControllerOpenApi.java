package br.com.doceterapia.api.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import br.com.doceterapia.api.dto.PedidoRequestDTO;
import br.com.doceterapia.api.dto.PedidoResponseDTO;
import br.com.doceterapia.api.enums.StatusPedido;

import java.util.List;

@Tag(name = "Pedidos", description = "Endpoints para gerenciamento de pedidos")
public interface PedidoControllerOpenApi {

    @Operation(summary = "Cadastrar novo pedido",
            description = "Cria um novo pedido no sistema associado a um cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pedido cadastrado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PedidoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos:\n" +
                    "- ID do cliente não fornecido\n" +
                    "- Tipo do pedido não fornecido\n" +
                    "- Status do pedido não fornecido\n" +
                    "- Data de entrega no passado\n" +
                    "- Campos obrigatórios em branco"),
            @ApiResponse(responseCode = "404", description = "Cliente com ID fornecido não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<PedidoResponseDTO> cadastrarPedido(
            @RequestBody(description = "Dados do pedido a ser cadastrado", required = true,
                    content = @Content(schema = @Schema(implementation = PedidoRequestDTO.class)))
            PedidoRequestDTO request);

    @Operation(summary = "Listar todos os pedidos",
            description = "Retorna uma lista com todos os pedidos cadastrados, incluindo informações do cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedidos listados com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PedidoResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<List<PedidoResponseDTO>> listarPedidos();

    @Operation(summary = "Atualizar pedido",
            description = "Atualiza os dados de um pedido existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido atualizado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PedidoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos:\n" +
                    "- Tipo do pedido não fornecido\n" +
                    "- Status do pedido não fornecido\n" +
                    "- Data de entrega no passado\n" +
                    "- Endereço de entrega obrigatório para forma de entrega ENTREGA\n" +
                    "- Campos obrigatórios em branco"),
            @ApiResponse(responseCode = "404", description = "Pedido com ID fornecido não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<PedidoResponseDTO> atualizarPedido(
            @Parameter(description = "ID do pedido a ser atualizado", example = "1")
            Integer id,
            @RequestBody(description = "Novos dados do pedido", required = true,
                    content = @Content(schema = @Schema(implementation = PedidoRequestDTO.class)))
            PedidoRequestDTO request);

    @Operation(summary = "Deletar pedido",
            description = "Remove um pedido do sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Pedido deletado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Pedido com ID fornecido não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> deletarPedido(
            @Parameter(description = "ID do pedido a ser deletado", example = "1")
            Integer id);

    @Operation(summary = "Atualizar status do pedido",
            description = "Atualiza o status de um pedido seguindo o fluxo de transições permitidas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status do pedido atualizado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PedidoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos fornecidos:\n" +
                    "- ID do pedido não fornecido\n" +
                    "- Status não fornecido\n" +
                    "- Transição de status não permitida"),
            @ApiResponse(responseCode = "404", description = "Pedido com ID fornecido não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<PedidoResponseDTO> atualizarStatus(
            @Parameter(description = "ID do pedido", example = "1", required = true)
            Integer id,
            @Parameter(description = "Novo status do pedido", example = "AGUARDANDO_SINAL", required = true)
            StatusPedido statusPedido);
}
