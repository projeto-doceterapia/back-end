package br.com.doceterapia.api.mapper;

import org.springframework.stereotype.Component;
import br.com.doceterapia.api.dto.ProdutoRequestDTO;
import br.com.doceterapia.api.dto.ProdutoResponseDTO;
import br.com.doceterapia.api.entity.CategoriaProduto;
import br.com.doceterapia.api.entity.Produto;
import br.com.doceterapia.api.repository.CategoriaProdutoRepository;

@Component
public class ProdutoMapper {

    private final CategoriaProdutoRepository categoriaProdutoRepository;

    public ProdutoMapper(CategoriaProdutoRepository categoriaProdutoRepository) {
        this.categoriaProdutoRepository = categoriaProdutoRepository;
    }

    public Produto toEntity(ProdutoRequestDTO dto) {
        Produto entity = new Produto();
        CategoriaProduto categoria = categoriaProdutoRepository.getReferenceById(dto.getFkCategoriaProduto());
        entity.setCategoriaProduto(categoria);
        entity.setNome(dto.getNome());
        entity.setCustoEstimado(dto.getCustoEstimado());
        entity.setPrecoAtual(dto.getPrecoAtual());
        entity.setPrecoSugerido(dto.getPrecoSugerido());
        entity.setMargemLucro(dto.getMargemLucro());
        entity.setUnidadeProducao(dto.getUnidadeProducao());
        entity.setStatus(dto.getStatus());
        entity.setDescricao(dto.getDescricao());
        entity.setQuantidadeProduzida(dto.getQuantidadeProduzida());
        return entity;
    }

    public ProdutoResponseDTO toResponseDTO(Produto entity) {
        ProdutoResponseDTO dto = new ProdutoResponseDTO();
        dto.setIdProduto(entity.getIdProduto());
        if (entity.getCategoriaProduto() != null) {
            dto.setCategoriaId(entity.getCategoriaProduto().getIdCategoriaProduto());
            dto.setCategoriaNome(entity.getCategoriaProduto().getNome());
        }
        dto.setNome(entity.getNome());
        dto.setCustoEstimado(entity.getCustoEstimado());
        dto.setPrecoAtual(entity.getPrecoAtual());
        dto.setPrecoSugerido(entity.getPrecoSugerido());
        dto.setMargemLucro(entity.getMargemLucro());
        dto.setUnidadeProducao(entity.getUnidadeProducao());
        dto.setStatus(entity.getStatus());
        dto.setDescricao(entity.getDescricao());
        dto.setQuantidadeProduzida(entity.getQuantidadeProduzida());
        return dto;
    }
}
