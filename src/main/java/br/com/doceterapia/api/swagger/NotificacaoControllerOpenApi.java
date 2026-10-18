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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import br.com.doceterapia.api.dto.ConfirmacaoEnvioRequestDTO;
import br.com.doceterapia.api.dto.NotificacaoAgendaResponseDTO;
import br.com.doceterapia.api.dto.NotificacaoRequestDTO;
import br.com.doceterapia.api.dto.NotificacaoResponseDTO;

import java.time.LocalDateTime;
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

    @Operation(summary = "Stream de notificações em tempo real",
            description = "Abre uma conexão Server-Sent Events que envia o evento 'notificacao' "
                    + "(com o mesmo corpo de /nao-lidas) quando uma notificação é criada e o evento "
                    + "'lida' ({\"id\": n}) quando uma é marcada como lida, sempre após o commit. "
                    + "Um comentário ': ping' é enviado periodicamente como keep-alive.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conexão de stream aberta",
                    content = @Content(mediaType = "text/event-stream")),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    SseEmitter streamNotificacoes();

    @Operation(summary = "Listar agenda de entrega",
            description = "Retorna as notificações pendentes de entrega cuja janela se sobrepõe ao intervalo informado. "
                    + "Notificações apenas internas (NAO_ENVIAR) ou já confirmadas não aparecem.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Agenda retornada com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = NotificacaoAgendaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Parâmetros de data inválidos"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<List<NotificacaoAgendaResponseDTO>> listarAgenda(
            @Parameter(description = "Início do intervalo (ISO date-time)", example = "2026-06-14T00:00:00")
            LocalDateTime de,
            @Parameter(description = "Fim do intervalo (ISO date-time)", example = "2026-06-14T23:59:59")
            LocalDateTime ate);

    @Operation(summary = "Confirmar envio de notificação",
            description = "Registra o resultado do envio (ENVIADA ou FALHA), o horário e o id da mensagem no canal. "
                    + "É idempotente: uma notificação já confirmada mantém a primeira confirmação.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Confirmação registrada com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = NotificacaoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Status de envio inválido"),
            @ApiResponse(responseCode = "404", description = "Notificação não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<NotificacaoResponseDTO> confirmarEnvio(
            @Parameter(description = "ID da notificação", example = "1") Integer id,
            @RequestBody(description = "Resultado do envio", required = true,
                    content = @Content(schema = @Schema(implementation = ConfirmacaoEnvioRequestDTO.class)))
            ConfirmacaoEnvioRequestDTO request);
}
