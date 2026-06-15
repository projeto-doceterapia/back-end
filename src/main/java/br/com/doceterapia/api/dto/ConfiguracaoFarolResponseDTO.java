package br.com.doceterapia.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO para resposta de configuração do farol da agenda cadastrada ou atualizada")
public class ConfiguracaoFarolResponseDTO {

    @Schema(description = "ID único da configuração", example = "1")
    private Integer idConfiguracaoFarol;

    @Schema(description = "Limite de dias para o status verde", example = "3")
    private Integer limiteVerde;

    @Schema(description = "Limite de dias para o status amarelo", example = "7")
    private Integer limiteAmarelo;

    @Schema(description = "Limite de dias para o status vermelho", example = "14")
    private Integer limiteVermelho;

    public Integer getIdConfiguracaoFarol() {
        return idConfiguracaoFarol;
    }

    public void setIdConfiguracaoFarol(Integer idConfiguracaoFarol) {
        this.idConfiguracaoFarol = idConfiguracaoFarol;
    }

    public Integer getLimiteVerde() {
        return limiteVerde;
    }

    public void setLimiteVerde(Integer limiteVerde) {
        this.limiteVerde = limiteVerde;
    }

    public Integer getLimiteAmarelo() {
        return limiteAmarelo;
    }

    public void setLimiteAmarelo(Integer limiteAmarelo) {
        this.limiteAmarelo = limiteAmarelo;
    }

    public Integer getLimiteVermelho() {
        return limiteVermelho;
    }

    public void setLimiteVermelho(Integer limiteVermelho) {
        this.limiteVermelho = limiteVermelho;
    }
}
