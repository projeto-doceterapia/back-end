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
import br.com.doceterapia.api.dto.NotificacaoRequestDTO;
import br.com.doceterapia.api.dto.NotificacaoResponseDTO;

import java.util.List;

@Tag(name = "Notificação", description = "Endpoints para gerenciamento de notificações")
public interface NotificacaoControllerOpenApi {

    @Operation(summary = "Cadastrar nova notificação",
            description = "Cria uma nova notificação no sistema com os dados fornecidos")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Notificação cadastrada com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = NotificacaoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "404", description = "Pedido ou Insumo com ID fornecido não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<NotificacaoResponseDTO> cadastrarNotificacao(
            @RequestBody(description = "Dados da notificação a ser cadastrada", required = true,
                    content = @Content(schema = @Schema(implementation = NotificacaoRequestDTO.class)))
            NotificacaoRequestDTO request);

    @Operation(summary = "Listar todas as notificações",
            description = "Retorna uma lista com todas as notificações cadastradas no sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notificações listadas com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = NotificacaoResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<List<NotificacaoResponseDTO>> listarNotificacoes();

    @Operation(summary = "Buscar notificação por ID",
            description = "Retorna uma notificação pelo seu ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notificação encontrada com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = NotificacaoResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Notificação não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<NotificacaoResponseDTO> buscarNotificacao(
            @Parameter(description = "ID da notificação", example = "1") Integer id);

    @Operation(summary = "Atualizar notificação",
            description = "Atualiza os dados de uma notificação existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notificação atualizada com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = NotificacaoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "404", description = "Notificação, Pedido ou Insumo com ID fornecido não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<NotificacaoResponseDTO> atualizarNotificacao(
            @Parameter(description = "ID da notificação a ser atualizada", example = "1")
            Integer id,
            @RequestBody(description = "Novos dados da notificação", required = true,
                    content = @Content(schema = @Schema(implementation = NotificacaoRequestDTO.class)))
            NotificacaoRequestDTO request);

    @Operation(summary = "Deletar notificação",
            description = "Remove uma notificação do sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Notificação deletada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Notificação com ID fornecido não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> deletarNotificacao(
            @Parameter(description = "ID da notificação a ser deletada", example = "1")
            Integer id);

    @Operation(summary = "Marcar notificação como lida",
            description = "Marca uma notificação como lida pelo seu ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notificação marcada como lida com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = NotificacaoResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Notificação com ID fornecido não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<NotificacaoResponseDTO> marcarNotificacaoLida(
            @Parameter(description = "ID da notificação", example = "1")
            Integer id);

    @Operation(summary = "Listar notificações não lidas",
            description = "Retorna uma lista com todas as notificações não lidas ordenadas pela data de criação decrescente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notificações não lidas listadas com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = NotificacaoResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<List<NotificacaoResponseDTO>> listarNotificacoesNaoLidas();
}
