package br.com.doceterapia.api.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import br.com.doceterapia.api.dto.ConfiguracaoNotificacaoRequestDTO;
import br.com.doceterapia.api.dto.ConfiguracaoNotificacaoResponseDTO;

@Tag(name = "Configuração de Notificações",
        description = "Endpoints para visualizar e alterar a configuração de notificações do negócio")
public interface ConfiguracaoNotificacaoControllerOpenApi {

    @Operation(summary = "Buscar configuração de notificações",
            description = "Retorna a configuração única de notificações, criando-a com os padrões caso ainda não exista")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Configuração retornada com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ConfiguracaoNotificacaoResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<ConfiguracaoNotificacaoResponseDTO> buscarConfiguracao();

    @Operation(summary = "Atualizar configuração de notificações",
            description = "Atualiza a configuração de notificações. Rejeita com 422 quando o fim não é depois do início "
                    + "ou quando um horário de resumo está fora do expediente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Configuração atualizada com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ConfiguracaoNotificacaoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "422", description = "Configuração inconsistente (fim antes do início ou resumo fora do expediente)"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<ConfiguracaoNotificacaoResponseDTO> atualizarConfiguracao(
            @RequestBody(description = "Novos dados da configuração de notificações", required = true,
                    content = @Content(schema = @Schema(implementation = ConfiguracaoNotificacaoRequestDTO.class)))
            ConfiguracaoNotificacaoRequestDTO request);
}
