package br.com.doceterapia.api.mapper;

import br.com.doceterapia.api.dto.InsumoRequestDTO;
import br.com.doceterapia.api.dto.InsumoResponseDTO;
import br.com.doceterapia.api.entity.CategoriaInsumo;
import br.com.doceterapia.api.entity.Insumo;
import br.com.doceterapia.api.enums.StatusAtivo;
import br.com.doceterapia.api.repository.CategoriaInsumoRepository;
import org.springframework.stereotype.Component;

@Component
public class InsumoMapper {

    private final CategoriaInsumoRepository categoriaInsumoRepository;

    public InsumoMapper(CategoriaInsumoRepository categoriaInsumoRepository) {
        this.categoriaInsumoRepository = categoriaInsumoRepository;
    }

    public Insumo toEntity(InsumoRequestDTO request) {
        Insumo entity = new Insumo();

        CategoriaInsumo categoria = categoriaInsumoRepository
                .findById(request.getFkCategoriaInsumo())
                .orElseThrow(() -> new RuntimeException("Categoria de insumo não encontrada: " + request.getFkCategoriaInsumo()));

        entity.setCategoria(categoria);
        entity.setNome(request.getNome());
        entity.setQuantidadeAtual(request.getQuantidadeAtual());
        entity.setQuantidadeMinima(request.getQuantidadeMinima());
        entity.setUnidade(request.getUnidade());
        entity.setStatus(request.getStatus() != null ? request.getStatus() : StatusAtivo.ATIVO);
        entity.setMarca(request.getMarca());
        entity.setCustoUnitario(request.getCustoUnitario());
        return entity;
    }

    public InsumoResponseDTO toResponseDTO(Insumo entity) {
        InsumoResponseDTO dto = new InsumoResponseDTO();
        dto.setIdInsumo(entity.getIdInsumo());
        if (entity.getCategoria() != null) {
            dto.setCategoriaId(entity.getCategoria().getIdCategoriaInsumo());
            dto.setCategoriaNome(entity.getCategoria().getNome());
        }
        dto.setNome(entity.getNome());
        dto.setQuantidadeAtual(entity.getQuantidadeAtual());
        dto.setQuantidadeMinima(entity.getQuantidadeMinima());
        dto.setUnidade(entity.getUnidade());
        dto.setStatus(entity.getStatus());
        dto.setMarca(entity.getMarca());
        dto.setCustoUnitario(entity.getCustoUnitario());
        return dto;
    }
}
