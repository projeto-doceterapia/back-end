package br.com.doceterapia.api.swagger;

import br.com.doceterapia.api.dto.NotaFiscalStatusResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Notas fiscais", description = "Consulta do status de registro de notas fiscais")
public interface NotaFiscalControllerOpenApi {

    @Operation(summary = "Consultar status de uma nota fiscal",
            description = "Retorna INEXISTENTE quando nenhuma movimentação usa a chave, ou REGISTRADA com os insumos já registrados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status da nota fiscal",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = NotaFiscalStatusResponseDTO.class))),
            @ApiResponse(responseCode = "422", description = "Chave inválida (não possui 44 dígitos ou dígito verificador incorreto)")
    })
    ResponseEntity<NotaFiscalStatusResponseDTO> consultarStatus(
            @Parameter(description = "Chave de acesso da nota fiscal (44 dígitos)",
                    example = "35200114200166000187650010000000181000000018")
            String chave);
}
