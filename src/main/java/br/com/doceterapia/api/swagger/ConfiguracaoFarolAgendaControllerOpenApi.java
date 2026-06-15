package br.com.doceterapia.api.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import br.com.doceterapia.api.dto.ConfiguracaoFarolRequestDTO;
import br.com.doceterapia.api.dto.ConfiguracaoFarolResponseDTO;

@Tag(name = "Configuração Farol Agenda",
        description = "Endpoints para gerenciamento da configuração do farol da agenda")
public interface ConfiguracaoFarolAgendaControllerOpenApi {

    @Operation(summary = "Cadastrar configuração do farol",
            description = "Cria uma nova configuração do farol da agenda. Apenas uma configuração pode existir no sistema.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Configuração cadastrada com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ConfiguracaoFarolResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "409", description = "Já existe uma configuração cadastrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<ConfiguracaoFarolResponseDTO> cadastrarConfiguracao(
            @RequestBody(description = "Dados da configuração a ser cadastrada", required = true,
                    content = @Content(schema = @Schema(implementation = ConfiguracaoFarolRequestDTO.class)))
            ConfiguracaoFarolRequestDTO request);

    @Operation(summary = "Buscar configuração do farol",
            description = "Retorna a configuração do farol da agenda cadastrada no sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Configuração encontrada com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ConfiguracaoFarolResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Nenhuma configuração encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<ConfiguracaoFarolResponseDTO> buscarConfiguracao();

    @Operation(summary = "Atualizar configuração do farol",
            description = "Atualiza a configuração do farol da agenda existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Configuração atualizada com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ConfiguracaoFarolResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "404", description = "Nenhuma configuração encontrada para atualizar"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<ConfiguracaoFarolResponseDTO> atualizarConfiguracao(
            @RequestBody(description = "Novos dados da configuração", required = true,
                    content = @Content(schema = @Schema(implementation = ConfiguracaoFarolRequestDTO.class)))
            ConfiguracaoFarolRequestDTO request);
}
