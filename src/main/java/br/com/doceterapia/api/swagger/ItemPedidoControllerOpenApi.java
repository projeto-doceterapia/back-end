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
import br.com.doceterapia.api.dto.ItemPedidoRequestDTO;
import br.com.doceterapia.api.dto.ItemPedidoResponseDTO;

import java.util.List;

@Tag(name = "Itens do Pedido", description = "Endpoints para gerenciamento de itens do pedido")
public interface ItemPedidoControllerOpenApi {

    @Operation(summary = "Listar todos os itens de pedido", description = "Retorna uma lista com todos os itens de pedido cadastrados no sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Itens de pedido listados com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ItemPedidoResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<List<ItemPedidoResponseDTO>> listarItensPedido();

    @Operation(summary = "Cadastrar novo item de pedido",
            description = "Cria um novo item de pedido no sistema com os dados fornecidos")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Item de pedido cadastrado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ItemPedidoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos:\n" +
                    "- Quantidade menor ou igual a zero\n" +
                    "- Valores negativos"),
            @ApiResponse(responseCode = "404", description = "Produto ou Pedido com ID fornecido não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<ItemPedidoResponseDTO> cadastrarItemPedido(
            @RequestBody(description = "Dados do item de pedido a ser cadastrado", required = true,
                    content = @Content(schema = @Schema(implementation = ItemPedidoRequestDTO.class)))
            ItemPedidoRequestDTO request);

    @Operation(summary = "Atualizar item de pedido",
            description = "Atualiza os dados de um item de pedido existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item de pedido atualizado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ItemPedidoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos:\n" +
                    "- Quantidade menor ou igual a zero\n" +
                    "- Valores negativos"),
            @ApiResponse(responseCode = "404", description = "Item de pedido, Produto ou Pedido com ID fornecido não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<ItemPedidoResponseDTO> atualizarItemPedido(
            @Parameter(description = "ID do item de pedido a ser atualizado", example = "1")
            Integer id,
            @RequestBody(description = "Novos dados do item de pedido", required = true,
                    content = @Content(schema = @Schema(implementation = ItemPedidoRequestDTO.class)))
            ItemPedidoRequestDTO request);

    @Operation(summary = "Deletar item de pedido",
            description = "Remove um item de pedido do sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Item de pedido deletado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Item de pedido com ID fornecido não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> deletarItemPedido(
            @Parameter(description = "ID do item de pedido a ser deletado", example = "1")
            Integer id);
}
