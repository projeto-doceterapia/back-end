package br.com.doceterapia.api.mapper;

import br.com.doceterapia.api.dto.CategoriaInsumoRequestDTO;
import br.com.doceterapia.api.dto.CategoriaInsumoResponseDTO;
import br.com.doceterapia.api.entity.CategoriaInsumo;

public class CategoriaInsumoMapper {

    public static CategoriaInsumo toEntity(CategoriaInsumoRequestDTO request) {
        CategoriaInsumo entity = new CategoriaInsumo();
        entity.setNome(request.getNome());
        entity.setDescricao(request.getDescricao());
        return entity;
    }

    public static CategoriaInsumoResponseDTO toResponseDTO(CategoriaInsumo entity) {
        CategoriaInsumoResponseDTO dto = new CategoriaInsumoResponseDTO();
        dto.setIdCategoriaInsumo(entity.getIdCategoriaInsumo());
        dto.setNome(entity.getNome());
        dto.setDescricao(entity.getDescricao());
        return dto;
    }
}
