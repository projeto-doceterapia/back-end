package br.com.doceterapia.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Erro 429: o intervalo mínimo entre duas ações ainda não terminou")
public record CooldownErroResponseDTO(
        @Schema(description = "Momento do erro", example = "2026-11-18T10:00:30") LocalDateTime timestamp,
        @Schema(description = "Status HTTP", example = "429") int status,
        @Schema(description = "Descrição do status", example = "Too Many Requests") String error,
        @Schema(description = "Mensagem do erro", example = "Aguarde para enviar outra notificação de teste.") String message,
        @Schema(description = "Rota chamada", example = "/notificacoes/teste") String path,
        @Schema(description = "Segundos até poder repetir a ação", example = "270") long segundosRestantes) {
}
