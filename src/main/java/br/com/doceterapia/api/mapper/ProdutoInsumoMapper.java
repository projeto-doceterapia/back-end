package br.com.doceterapia.api.mapper;

import org.springframework.stereotype.Component;
import br.com.doceterapia.api.dto.ProdutoInsumoRequestDTO;
import br.com.doceterapia.api.dto.ProdutoInsumoResponseDTO;
import br.com.doceterapia.api.entity.Insumo;
import br.com.doceterapia.api.entity.Produto;
import br.com.doceterapia.api.entity.ProdutoInsumo;
import br.com.doceterapia.api.exception.InsumoIdDontExistsException;
import br.com.doceterapia.api.repository.InsumoRepository;

@Component
public class ProdutoInsumoMapper {

    private final InsumoRepository insumoRepository;

    public ProdutoInsumoMapper(InsumoRepository insumoRepository) {
        this.insumoRepository = insumoRepository;
    }

    public ProdutoInsumo toEntity(ProdutoInsumoRequestDTO dto, Produto produto) {
        Insumo insumo = insumoRepository.findById(dto.getFkInsumo())
                .orElseThrow(InsumoIdDontExistsException::new);

        ProdutoInsumo entity = new ProdutoInsumo();
        entity.setProduto(produto);
        entity.setInsumo(insumo);
        entity.setQuantidadeUtilizada(dto.getQuantidadeUtilizada());
        entity.setUnidade(dto.getUnidade());
        return entity;
    }

    public ProdutoInsumoResponseDTO toResponseDTO(ProdutoInsumo entity) {
        ProdutoInsumoResponseDTO dto = new ProdutoInsumoResponseDTO();
        dto.setIdProdutoInsumo(entity.getIdProdutoInsumo());
        dto.setFkProduto(entity.getProduto().getIdProduto());
        dto.setFkInsumo(entity.getInsumo().getIdInsumo());
        dto.setNomeInsumo(entity.getInsumo().getNome());
        dto.setQuantidadeUtilizada(entity.getQuantidadeUtilizada());
        dto.setUnidade(entity.getUnidade());
        return dto;
    }
}
