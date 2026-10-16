package br.com.doceterapia.api.dto;

import br.com.doceterapia.api.enums.OrigemMovimentacao;
import br.com.doceterapia.api.enums.TipoMovimentacao;
import br.com.doceterapia.api.enums.UnidadeMedida;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "DTO de resposta de uma movimentação de estoque")
public class MovimentacaoEstoqueResponseDTO {

    @Schema(description = "ID da movimentação", example = "1")
    private Integer idMovimentacao;

    @Schema(description = "ID do insumo movimentado", example = "1")
    private Integer insumoId;

    @Schema(description = "Tipo da movimentação", example = "ENTRADA_COMPRA")
    private TipoMovimentacao tipo;

    @Schema(description = "Quantidade na unidade do insumo (assinada)", example = "10.000")
    private BigDecimal quantidade;

    @Schema(description = "Quantidade informada na requisição", example = "10.000")
    private BigDecimal quantidadeInformada;

    @Schema(description = "Unidade informada na requisição", example = "KG")
    private UnidadeMedida unidadeInformada;

    @Schema(description = "Custo unitário registrado, quando houve preço", example = "7.5000")
    private BigDecimal custoUnitario;

    @Schema(description = "Origem da movimentação", example = "NOTA_IMAGEM")
    private OrigemMovimentacao origem;

    @Schema(description = "Chave de acesso da nota fiscal", example = "35200114200166000187650010000000181000000018")
    private String chaveNota;

    @Schema(description = "Identificador do fluxo da compra", example = "fluxo-123")
    private String fluxoId;

    @Schema(description = "Saldo atual do insumo após a movimentação", example = "10.000")
    private BigDecimal saldoAtual;

    @Schema(description = "Indica se a requisição foi um reenvio de uma movimentação já registrada", example = "false")
    private Boolean duplicado;

    @Schema(description = "Momento em que a compra ocorreu")
    private LocalDateTime ocorridoEm;

    public Integer getIdMovimentacao() {
        return idMovimentacao;
    }

    public void setIdMovimentacao(Integer idMovimentacao) {
        this.idMovimentacao = idMovimentacao;
    }

    public Integer getInsumoId() {
        return insumoId;
    }

    public void setInsumoId(Integer insumoId) {
        this.insumoId = insumoId;
    }

    public TipoMovimentacao getTipo() {
        return tipo;
    }

    public void setTipo(TipoMovimentacao tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(BigDecimal quantidade) {
        this.quantidade = quantidade;
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

    public BigDecimal getCustoUnitario() {
        return custoUnitario;
    }

    public void setCustoUnitario(BigDecimal custoUnitario) {
        this.custoUnitario = custoUnitario;
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

    public BigDecimal getSaldoAtual() {
        return saldoAtual;
    }

    public void setSaldoAtual(BigDecimal saldoAtual) {
        this.saldoAtual = saldoAtual;
    }

    public Boolean getDuplicado() {
        return duplicado;
    }

    public void setDuplicado(Boolean duplicado) {
        this.duplicado = duplicado;
    }

    public LocalDateTime getOcorridoEm() {
        return ocorridoEm;
    }

    public void setOcorridoEm(LocalDateTime ocorridoEm) {
        this.ocorridoEm = ocorridoEm;
    }
}
