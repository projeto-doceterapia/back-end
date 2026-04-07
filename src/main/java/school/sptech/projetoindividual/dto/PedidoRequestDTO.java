package school.sptech.projetoindividual.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "DTO para requisição de cadastro ou atualização de pedido")
public class PedidoRequestDTO {

    @NotNull(message = "ID do cliente é obrigatório")
    @Schema(description = "ID do cliente associado ao pedido", example = "1")
    private Integer fkCliente;

    @NotBlank(message = "Descrição é obrigatória")
    @Schema(description = "Descrição do pedido", example = "Bolo de chocolate com 3 andares")
    private String descricao;

    @NotNull(message = "Data de entrega é obrigatória")
    @FutureOrPresent(message = "Data de entrega deve ser hoje ou uma data futura")
    @Schema(description = "Data de entrega do pedido", example = "2026-04-15", type = "string", format = "date")
    private LocalDate dataEntrega;

    @Schema(description = "Valor do pedido em reais", example = "150.50")
    private Double valor;

    public Integer getFkCliente() {
        return fkCliente;
    }

    public void setFkCliente(Integer fkCliente) {
        this.fkCliente = fkCliente;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDate getDataEntrega() {
        return dataEntrega;
    }

    public void setDataEntrega(LocalDate dataEntrega) {
        this.dataEntrega = dataEntrega;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }
}
