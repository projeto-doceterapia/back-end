package br.com.doceterapia.api.dto;

import br.com.doceterapia.api.enums.ClassificacaoCliente;
import br.com.doceterapia.api.enums.StatusAtivo;
import br.com.doceterapia.api.enums.TipoPessoa;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Cliente com o resumo de pedidos, sem contar cancelados e orçamentos")
public record ClienteResumoResponseDTO(
        @Schema(description = "ID único do cliente", example = "1") Integer idCliente,
        @Schema(description = "Nome do cliente", example = "Cliente Exemplo") String nome,
        @Schema(description = "Telefone do cliente", example = "+55 11 90000-0001") String telefone,
        @Schema(description = "Endereço do cliente", example = "Praça da Sé, 100, Sé, São Paulo - SP") String endereco,
        @Schema(description = "CEP do cliente, 8 dígitos", example = "01001000", nullable = true) String cep,
        @Schema(description = "Tipo de pessoa", example = "FISICA") TipoPessoa tipoPessoa,
        @Schema(description = "Classificação do cliente", example = "PADRAO") ClassificacaoCliente classificacaoCliente,
        @Schema(description = "Status do cliente", example = "ATIVO") StatusAtivo status,
        @Schema(description = "Observação sobre o cliente") String observacao,
        @Schema(description = "Quantidade de pedidos que contam no resumo", example = "7") long totalPedidos,
        @Schema(description = "Soma dos itens dos pedidos; nulo para o perfil FUNCIONARIA", example = "845.50",
                nullable = true) BigDecimal gastoTotal,
        @Schema(description = "Maior data de entrega entre os pedidos; nulo sem pedidos", example = "2026-10-28",
                nullable = true) LocalDate ultimoPedido) {
}
