package br.com.doceterapia.api.swagger;

import br.com.doceterapia.api.dto.PagamentoResponseDTO;
import br.com.doceterapia.api.dto.RegistroPagamentoRequestDTO;
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

    @Operation(summary = "Listar todos os pagamentos", description = "Retorna todos os pagamentos cadastrados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pagamentos listados com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PagamentoResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<List<PagamentoResponseDTO>> listarPagamentos();

    @Operation(summary = "Buscar pagamento por ID", description = "Retorna um pagamento pelo seu ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pagamento encontrado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PagamentoResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Pagamento não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<PagamentoResponseDTO> buscarPagamentoPorId(
            @Parameter(description = "ID do pagamento", example = "1") Integer id);

    @Operation(summary = "Buscar pagamento por pedido", description = "Retorna o pagamento associado ao pedido")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pagamento encontrado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PagamentoResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Pagamento não encontrado para este pedido"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<PagamentoResponseDTO> buscarPagamentoPorPedidoId(
            @Parameter(description = "ID do pedido", example = "1") Integer pedidoId);

    @Operation(summary = "Registrar pagamento do sinal",
            description = "Registra forma e data do sinal e avança o pedido para ESPERANDO_FAZER")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sinal registrado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PagamentoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Forma de pagamento obrigatória"),
            @ApiResponse(responseCode = "409", description = "Conflito de estado"),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada"),
            @ApiResponse(responseCode = "404", description = "Pagamento não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<PagamentoResponseDTO> registrarSinal(
            @Parameter(description = "ID do pagamento", example = "1") Integer id,
            @RequestBody(description = "Forma e data opcional do sinal", required = true,
                    content = @Content(schema = @Schema(implementation = RegistroPagamentoRequestDTO.class)))
            RegistroPagamentoRequestDTO request);

    @Operation(summary = "Registrar pagamento do restante",
            description = "Registra forma e data do restante sem alterar a fase operacional do pedido")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Restante registrado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PagamentoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Forma de pagamento obrigatória"),
            @ApiResponse(responseCode = "409", description = "Conflito de estado"),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada"),
            @ApiResponse(responseCode = "404", description = "Pagamento não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<PagamentoResponseDTO> registrarRestante(
            @Parameter(description = "ID do pagamento", example = "1") Integer id,
            @RequestBody(description = "Forma e data opcional do restante", required = true,
                    content = @Content(schema = @Schema(implementation = RegistroPagamentoRequestDTO.class)))
            RegistroPagamentoRequestDTO request);
}
