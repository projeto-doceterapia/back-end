package br.com.doceterapia.api.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import br.com.doceterapia.api.dto.ConfiguracaoPrecificacaoRequestDTO;
import br.com.doceterapia.api.dto.ConfiguracaoPrecificacaoResponseDTO;

@Tag(name = "Configuração de Precificação",
        description = "Endpoints da margem padrão e do percentual de custos indiretos usados no preço sugerido")
public interface ConfiguracaoPrecificacaoControllerOpenApi {

    @Operation(summary = "Buscar configuração de precificação",
            description = "Retorna a margem padrão e o percentual de custos indiretos. Sem configuração gravada, retorna 0.00 nos dois.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Configuração retornada com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ConfiguracaoPrecificacaoResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<ConfiguracaoPrecificacaoResponseDTO> buscarConfiguracao();

    @Operation(summary = "Atualizar configuração de precificação",
            description = "Grava a margem padrão (0 a 999,99%) e o percentual de custos indiretos (0 a 100%). Cria a linha única quando ela não existe.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Configuração salva com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ConfiguracaoPrecificacaoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Valores fora dos limites ou ausentes"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<ConfiguracaoPrecificacaoResponseDTO> atualizarConfiguracao(
            @RequestBody(description = "Novos valores da configuração", required = true,
                    content = @Content(schema = @Schema(implementation = ConfiguracaoPrecificacaoRequestDTO.class)))
            ConfiguracaoPrecificacaoRequestDTO request);
}
