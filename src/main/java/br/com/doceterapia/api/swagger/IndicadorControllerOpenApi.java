package br.com.doceterapia.api.swagger;

import br.com.doceterapia.api.dto.IndicadoresFinanceirosResponseDTO;
import br.com.doceterapia.api.dto.IndicadoresOperacionaisResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

@Tag(name = "Indicadores", description = "Números agregados da Visão Geral, calculados no banco para um período")
public interface IndicadorControllerOpenApi {

    @Operation(summary = "Indicadores operacionais",
            description = "Pedidos por status, entregas de hoje e da semana, pedidos por dia, dias lotados, "
                    + "produtos mais vendidos e insumos abaixo do mínimo. O período usa a data de entrega e "
                    + "tem no máximo 366 dias. Os insumos abaixo do mínimo refletem o saldo atual. Disponível "
                    + "para DONA e FUNCIONARIA.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Indicadores calculados",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = IndicadoresOperacionaisResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Período ausente, invertido, malformado ou acima de 366 dias"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    ResponseEntity<IndicadoresOperacionaisResponseDTO> operacionais(
            @Parameter(description = "Primeiro dia do período (yyyy-MM-dd)", example = "2026-11-01") LocalDate de,
            @Parameter(description = "Último dia do período (yyyy-MM-dd)", example = "2026-11-30") LocalDate ate);

    @Operation(summary = "Indicadores financeiros",
            description = "Faturamento (recebido e a receber), ticket médio, gastos com insumos, lucro estimado, "
                    + "ranking por faturamento, pedidos cancelados, pedidos em orçamento e faturamento dos últimos "
                    + "6 meses. Orçamentos e cancelados ficam fora do faturamento. Os gastos somam quantidade vezes "
                    + "custo unitário das compras lançadas no período, então podem diferir da nota por arredondamento. "
                    + "O lucro estimado compara o faturamento das entregas com as compras do período, não o consumo "
                    + "exato de insumos. Somente DONA.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Indicadores calculados",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = IndicadoresFinanceirosResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Período ausente, invertido, malformado ou acima de 366 dias"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão (FUNCIONARIA)")
    })
    ResponseEntity<IndicadoresFinanceirosResponseDTO> financeiros(
            @Parameter(description = "Primeiro dia do período (yyyy-MM-dd)", example = "2026-11-01") LocalDate de,
            @Parameter(description = "Último dia do período (yyyy-MM-dd)", example = "2026-11-30") LocalDate ate);
}
