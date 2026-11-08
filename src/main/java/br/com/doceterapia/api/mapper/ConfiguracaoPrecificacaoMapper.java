package br.com.doceterapia.api.mapper;

import br.com.doceterapia.api.dto.ConfiguracaoPrecificacaoResponseDTO;
import br.com.doceterapia.api.entity.ConfiguracaoPrecificacao;

public final class ConfiguracaoPrecificacaoMapper {

    private ConfiguracaoPrecificacaoMapper() {
    }

    public static ConfiguracaoPrecificacaoResponseDTO toResponseDTO(ConfiguracaoPrecificacao entity) {
        return new ConfiguracaoPrecificacaoResponseDTO(entity.getMargemPadrao(), entity.getPercentualIndiretos());
    }
}
