package br.com.doceterapia.api.mapper;

import org.springframework.stereotype.Component;
import br.com.doceterapia.api.dto.ProdutoRequestDTO;
import br.com.doceterapia.api.dto.ProdutoResponseDTO;
import br.com.doceterapia.api.entity.CategoriaProduto;
import br.com.doceterapia.api.entity.Produto;
import br.com.doceterapia.api.repository.CategoriaProdutoRepository;
import br.com.doceterapia.api.service.precificacao.PrecificacaoProduto;

import java.math.BigDecimal;

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
        entity.setPrecoAtual(dto.getPrecoAtual());
        entity.setMargemLucro(dto.getMargemLucro());
        entity.setUnidadeProducao(dto.getUnidadeProducao());
        entity.setStatus(dto.getStatus());
        entity.setDescricao(dto.getDescricao());
        entity.setQuantidadeProduzida(dto.getQuantidadeProduzida());
        return entity;
    }

    public ProdutoResponseDTO toResponseDTO(Produto entity, PrecificacaoProduto precificacao) {
        ProdutoResponseDTO dto = new ProdutoResponseDTO();
        dto.setIdProduto(entity.getIdProduto());
        if (entity.getCategoriaProduto() != null) {
            dto.setCategoriaId(entity.getCategoriaProduto().getIdCategoriaProduto());
            dto.setCategoriaNome(entity.getCategoriaProduto().getNome());
        }
        dto.setNome(entity.getNome());
        dto.setCustoEstimado(precificacao.custoEstimado());
        dto.setPrecoAtual(entity.getPrecoAtual());
        dto.setPrecoSugerido(precificacao.precoSugerido());
        dto.setMargemLucro(entity.getMargemLucro());
        dto.setUnidadeProducao(entity.getUnidadeProducao());
        dto.setStatus(entity.getStatus());
        dto.setDescricao(entity.getDescricao());
        dto.setQuantidadeProduzida(entity.getQuantidadeProduzida());
        dto.setPrecoAbaixoDoSugerido(precoAbaixoDoSugerido(entity.getPrecoAtual(), precificacao.precoSugerido()));
        return dto;
    }

    private boolean precoAbaixoDoSugerido(BigDecimal precoAtual, BigDecimal precoSugerido) {
        return precoAtual != null && precoSugerido != null && precoAtual.compareTo(precoSugerido) < 0;
    }
}
