package br.com.doceterapia.api.swagger;

import br.com.doceterapia.api.dto.MovimentacaoEstoqueRequestDTO;
import br.com.doceterapia.api.dto.MovimentacaoEstoqueResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Movimentações de estoque", description = "Registro e consulta do histórico de estoque")
public interface EstoqueMovimentoControllerOpenApi {

    @Operation(summary = "Registrar entrada de compra",
            description = "Registra uma entrada de compra de forma idempotente. O cabeçalho Idempotency-Key é obrigatório.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Movimentação registrada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MovimentacaoEstoqueResponseDTO.class))),
            @ApiResponse(responseCode = "200", description = "Reenvio com a mesma chave e mesmo conteúdo (duplicado)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MovimentacaoEstoqueResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Cabeçalho Idempotency-Key ausente"),
            @ApiResponse(responseCode = "404", description = "Insumo inexistente ou inativo"),
            @ApiResponse(responseCode = "409", description = "Mesma chave de idempotência com conteúdo diferente"),
            @ApiResponse(responseCode = "422", description = "Conteúdo inválido (quantidade, unidade ou chave de nota)")
    })
    ResponseEntity<MovimentacaoEstoqueResponseDTO> registrarMovimento(
            @Parameter(name = "Idempotency-Key", in = ParameterIn.HEADER, required = true,
                    description = "Chave de idempotência da entrada")
            String idempotencyKey,
            @RequestBody(description = "Item de compra", required = true,
                    content = @Content(schema = @Schema(implementation = MovimentacaoEstoqueRequestDTO.class)))
            MovimentacaoEstoqueRequestDTO request);

    @Operation(summary = "Listar histórico de movimentações",
            description = "Lista as movimentações de um insumo, mais recentes primeiro, com intervalo de datas opcional.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Histórico retornado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MovimentacaoEstoqueResponseDTO.class)))
    })
    ResponseEntity<List<MovimentacaoEstoqueResponseDTO>> listarMovimentos(
            @Parameter(description = "ID do insumo", example = "1") Integer insumoId,
            @Parameter(description = "Início do intervalo de ocorrência") LocalDateTime de,
            @Parameter(description = "Fim do intervalo de ocorrência") LocalDateTime ate);
}
