package br.com.doceterapia.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "DTO para requisição de cadastro ou atualização de configuração do farol da agenda")
public class ConfiguracaoFarolRequestDTO {

    @NotNull(message = "Limite verde é obrigatório")
    @Min(value = 0, message = "Limite verde deve ser maior ou igual a 0")
    @Schema(description = "Limite de dias para o status verde", example = "3")
    private Integer limiteVerde;

    @NotNull(message = "Limite amarelo é obrigatório")
    @Min(value = 0, message = "Limite amarelo deve ser maior ou igual a 0")
    @Schema(description = "Limite de dias para o status amarelo", example = "7")
    private Integer limiteAmarelo;

    @NotNull(message = "Limite vermelho é obrigatório")
    @Min(value = 0, message = "Limite vermelho deve ser maior ou igual a 0")
    @Schema(description = "Limite de dias para o status vermelho", example = "14")
    private Integer limiteVermelho;

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
