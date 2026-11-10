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

import java.time.LocalDate;
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

    @Operation(summary = "Listar movimentações por período",
            description = "Lista as movimentações de estoque ocorridas entre as datas informadas (inclusive), "
                    + "mais recentes primeiro. O período máximo é de 366 dias. Sem insumoId, traz todos os insumos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movimentações retornadas",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MovimentacaoEstoqueResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Período ausente, invertido, malformado ou acima de 366 dias")
    })
    ResponseEntity<List<MovimentacaoEstoqueResponseDTO>> listarMovimentos(
            @Parameter(description = "Primeiro dia do período (yyyy-MM-dd)", example = "2026-10-04") LocalDate de,
            @Parameter(description = "Último dia do período (yyyy-MM-dd)", example = "2026-11-03") LocalDate ate,
            @Parameter(description = "ID do insumo; omita para listar todos", example = "1") Integer insumoId);
}
