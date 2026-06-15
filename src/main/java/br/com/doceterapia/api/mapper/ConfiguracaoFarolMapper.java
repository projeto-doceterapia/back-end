package br.com.doceterapia.api.mapper;

import br.com.doceterapia.api.dto.ConfiguracaoFarolRequestDTO;
import br.com.doceterapia.api.dto.ConfiguracaoFarolResponseDTO;
import br.com.doceterapia.api.entity.ConfiguracaoFarolAgenda;

public class ConfiguracaoFarolMapper {

    public static ConfiguracaoFarolAgenda toEntity(ConfiguracaoFarolRequestDTO request) {
        ConfiguracaoFarolAgenda entity = new ConfiguracaoFarolAgenda();
        entity.setLimiteVerde(request.getLimiteVerde());
        entity.setLimiteAmarelo(request.getLimiteAmarelo());
        entity.setLimiteVermelho(request.getLimiteVermelho());
        return entity;
    }

    public static ConfiguracaoFarolResponseDTO toResponseDTO(ConfiguracaoFarolAgenda entity) {
        ConfiguracaoFarolResponseDTO dto = new ConfiguracaoFarolResponseDTO();
        dto.setIdConfiguracaoFarol(entity.getIdConfiguracaoFarol());
        dto.setLimiteVerde(entity.getLimiteVerde());
        dto.setLimiteAmarelo(entity.getLimiteAmarelo());
        dto.setLimiteVermelho(entity.getLimiteVermelho());
        return dto;
    }
}
