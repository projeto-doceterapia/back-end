package br.com.doceterapia.api.swagger;

import br.com.doceterapia.api.dto.PagamentoRequestDTO;
import br.com.doceterapia.api.dto.PagamentoResponseDTO;
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

@Tag(name = "Pagamentos", description = "Endpoints para gerenciamento de pagamentos")
public interface PagamentoControllerOpenApi {

    @Operation(summary = "Listar todos os pagamentos",
            description = "Retorna uma lista com todos os pagamentos cadastrados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pagamentos listados com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PagamentoResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<List<PagamentoResponseDTO>> listarPagamentos();

    @Operation(summary = "Buscar pagamento por ID",
            description = "Retorna um pagamento pelo seu ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pagamento encontrado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PagamentoResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Pagamento não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<PagamentoResponseDTO> buscarPagamentoPorId(
            @Parameter(description = "ID do pagamento", example = "1") Integer id);

    @Operation(summary = "Buscar pagamento por ID do pedido",
            description = "Retorna um pagamento pelo ID do pedido associado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pagamento encontrado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PagamentoResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Pagamento não encontrado para este pedido"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<PagamentoResponseDTO> buscarPagamentoPorPedidoId(
            @Parameter(description = "ID do pedido", example = "1") Integer pedidoId);

    @Operation(summary = "Atualizar pagamento",
            description = "Atualiza os dados de um pagamento existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pagamento atualizado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PagamentoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "404", description = "Pagamento com ID fornecido não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<PagamentoResponseDTO> atualizarPagamento(
            @Parameter(description = "ID do pagamento a ser atualizado", example = "1") Integer id,
            @RequestBody(description = "Novos dados do pagamento", required = true,
                    content = @Content(schema = @Schema(implementation = PagamentoRequestDTO.class)))
            PagamentoRequestDTO request);

    @Operation(summary = "Registrar pagamento do sinal",
            description = "Registra a data de pagamento do sinal e atualiza o status para AGUARDANDO_PAGAMENTO")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sinal registrado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PagamentoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Sinal já foi pago anteriormente"),
            @ApiResponse(responseCode = "404", description = "Pagamento não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<PagamentoResponseDTO> registrarSinal(
            @Parameter(description = "ID do pagamento", example = "1") Integer id);

    @Operation(summary = "Registrar pagamento do restante",
            description = "Registra a data de pagamento do restante e atualiza o status para PAGO_INTEGRAL")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pagamento restante registrado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PagamentoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Sinal deve ser pago antes do restante"),
            @ApiResponse(responseCode = "404", description = "Pagamento não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<PagamentoResponseDTO> registrarRestante(
            @Parameter(description = "ID do pagamento", example = "1") Integer id);

    @Operation(summary = "Deletar pagamento",
            description = "Remove um pagamento do sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Pagamento deletado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Pagamento com ID fornecido não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> deletarPagamento(
            @Parameter(description = "ID do pagamento a ser deletado", example = "1") Integer id);
}
