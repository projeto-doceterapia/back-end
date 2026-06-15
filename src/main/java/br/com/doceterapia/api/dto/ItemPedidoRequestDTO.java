package br.com.doceterapia.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Schema(description = "DTO para requisição de cadastro ou atualização de item de pedido")
public class ItemPedidoRequestDTO {

    @NotNull(message = "Produto é obrigatório")
    @Schema(description = "ID do produto", example = "1")
    private Integer fkProduto;

    @NotNull(message = "Pedido é obrigatório")
    @Schema(description = "ID do pedido", example = "1")
    private Integer fkPedido;

    @NotNull(message = "Quantidade é obrigatória")
    @DecimalMin(value = "0.0001", message = "Quantidade deve ser maior que zero")
    @Schema(description = "Quantidade do produto no pedido", example = "2")
    private BigDecimal quantidade;

    @NotNull(message = "Valor unitário é obrigatório")
    @DecimalMin(value = "0.00", message = "Valor unitário deve ser maior ou igual a zero")
    @Schema(description = "Valor unitário do produto em reais", example = "45.90")
    private BigDecimal valorUnitario;

    @Size(max = 245, message = "Observação não pode exceder 245 caracteres")
    @Schema(description = "Observação sobre o item", example = "Sem glúten", maxLength = 245)
    private String observacao;

    public Integer getFkProduto() {
        return fkProduto;
    }

    public void setFkProduto(Integer fkProduto) {
        this.fkProduto = fkProduto;
    }

    public Integer getFkPedido() {
        return fkPedido;
    }

    public void setFkPedido(Integer fkPedido) {
        this.fkPedido = fkPedido;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(BigDecimal quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getValorUnitario() {
        return valorUnitario;
    }

    public void setValorUnitario(BigDecimal valorUnitario) {
        this.valorUnitario = valorUnitario;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}
