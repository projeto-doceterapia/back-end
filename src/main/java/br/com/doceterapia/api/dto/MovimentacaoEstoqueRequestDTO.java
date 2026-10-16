package br.com.doceterapia.api.dto;

import br.com.doceterapia.api.enums.OrigemMovimentacao;
import br.com.doceterapia.api.enums.UnidadeMedida;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "DTO para registro de uma entrada de compra no estoque")
public class MovimentacaoEstoqueRequestDTO {

    @NotNull(message = "O id do insumo é obrigatório")
    @Schema(description = "ID do insumo que recebe a entrada", example = "1")
    private Integer insumoId;

    @NotNull(message = "A quantidade informada é obrigatória")
    @Schema(description = "Quantidade comprada na unidade informada", example = "10.000")
    private BigDecimal quantidadeInformada;

    @NotNull(message = "A unidade informada é obrigatória")
    @Schema(description = "Unidade da quantidade informada", example = "KG")
    private UnidadeMedida unidadeInformada;

    @Schema(description = "Valor líquido do item (após descontos), usado para atualizar o custo", example = "15.00")
    private BigDecimal valorLiquido;

    @NotNull(message = "A origem é obrigatória")
    @Schema(description = "Origem da movimentação", example = "NOTA_IMAGEM")
    private OrigemMovimentacao origem;

    @Schema(description = "Chave de acesso da nota fiscal (44 dígitos)", example = "35200114200166000187650010000000181000000018")
    private String chaveNota;

    @Schema(description = "Identificador do fluxo da compra", example = "fluxo-123")
    private String fluxoId;

    @Schema(description = "Momento em que a compra ocorreu (padrão: agora)")
    private LocalDateTime ocorridoEm;

    public Integer getInsumoId() {
        return insumoId;
    }

    public void setInsumoId(Integer insumoId) {
        this.insumoId = insumoId;
    }

    public BigDecimal getQuantidadeInformada() {
        return quantidadeInformada;
    }

    public void setQuantidadeInformada(BigDecimal quantidadeInformada) {
        this.quantidadeInformada = quantidadeInformada;
    }

    public UnidadeMedida getUnidadeInformada() {
        return unidadeInformada;
    }

    public void setUnidadeInformada(UnidadeMedida unidadeInformada) {
        this.unidadeInformada = unidadeInformada;
    }

    public BigDecimal getValorLiquido() {
        return valorLiquido;
    }

    public void setValorLiquido(BigDecimal valorLiquido) {
        this.valorLiquido = valorLiquido;
    }

    public OrigemMovimentacao getOrigem() {
        return origem;
    }

    public void setOrigem(OrigemMovimentacao origem) {
        this.origem = origem;
    }

    public String getChaveNota() {
        return chaveNota;
    }

    public void setChaveNota(String chaveNota) {
        this.chaveNota = chaveNota;
    }

    public String getFluxoId() {
        return fluxoId;
    }

    public void setFluxoId(String fluxoId) {
        this.fluxoId = fluxoId;
    }

    public LocalDateTime getOcorridoEm() {
        return ocorridoEm;
    }

    public void setOcorridoEm(LocalDateTime ocorridoEm) {
        this.ocorridoEm = ocorridoEm;
    }
}
