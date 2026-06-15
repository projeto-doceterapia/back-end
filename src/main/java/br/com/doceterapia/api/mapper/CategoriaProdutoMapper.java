package br.com.doceterapia.api.mapper;

import br.com.doceterapia.api.dto.CategoriaProdutoRequestDTO;
import br.com.doceterapia.api.dto.CategoriaProdutoResponseDTO;
import br.com.doceterapia.api.entity.CategoriaProduto;

public class CategoriaProdutoMapper {

    public static CategoriaProduto toEntity(CategoriaProdutoRequestDTO dto) {
        CategoriaProduto entity = new CategoriaProduto();
        entity.setNome(dto.getNome());
        entity.setDescricao(dto.getDescricao());
        return entity;
    }

    public static CategoriaProdutoResponseDTO toResponseDTO(CategoriaProduto entity) {
        CategoriaProdutoResponseDTO dto = new CategoriaProdutoResponseDTO();
        dto.setIdCategoriaProduto(entity.getIdCategoriaProduto());
        dto.setNome(entity.getNome());
        dto.setDescricao(entity.getDescricao());
        return dto;
    }
}
