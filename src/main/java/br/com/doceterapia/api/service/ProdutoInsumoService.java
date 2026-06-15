package br.com.doceterapia.api.service;

import org.springframework.stereotype.Service;
import br.com.doceterapia.api.dto.ProdutoInsumoRequestDTO;
import br.com.doceterapia.api.entity.Insumo;
import br.com.doceterapia.api.entity.ProdutoInsumo;
import br.com.doceterapia.api.exception.InsumoIdDontExistsException;
import br.com.doceterapia.api.exception.ProdutoIdDontExistsException;
import br.com.doceterapia.api.exception.ProdutoInsumoDuplicateException;
import br.com.doceterapia.api.mapper.ProdutoInsumoMapper;
import br.com.doceterapia.api.repository.InsumoRepository;
import br.com.doceterapia.api.repository.ProdutoInsumoRepository;
import br.com.doceterapia.api.repository.ProdutoRepository;

import java.util.List;

@Service
public class ProdutoInsumoService {

    private final ProdutoInsumoRepository repository;
    private final ProdutoInsumoMapper mapper;
    private final ProdutoRepository produtoRepository;
    private final InsumoRepository insumoRepository;

    public ProdutoInsumoService(ProdutoInsumoRepository repository, ProdutoInsumoMapper mapper,
                                ProdutoRepository produtoRepository, InsumoRepository insumoRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.produtoRepository = produtoRepository;
        this.insumoRepository = insumoRepository;
    }

    public List<ProdutoInsumo> listarPorProduto(Integer produtoId) {
        if (!produtoRepository.existsById(produtoId)) {
            throw new ProdutoIdDontExistsException();
        }
        return repository.findByProdutoIdProduto(produtoId);
    }

    public ProdutoInsumo cadastrar(Integer produtoId, ProdutoInsumoRequestDTO dto) {
        if (!produtoRepository.existsById(produtoId)) {
            throw new ProdutoIdDontExistsException();
        }
        if (!insumoRepository.existsById(dto.getFkInsumo())) {
            throw new InsumoIdDontExistsException();
        }
        if (repository.existsByProdutoIdProdutoAndInsumoIdInsumo(produtoId, dto.getFkInsumo())) {
            throw new ProdutoInsumoDuplicateException();
        }

        ProdutoInsumo entity = mapper.toEntity(dto, produtoRepository.getReferenceById(produtoId));
        repository.save(entity);
        return entity;
    }

    public ProdutoInsumo atualizar(Integer id, ProdutoInsumoRequestDTO dto) {
        ProdutoInsumo entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProdutoInsumo não encontrado: " + id));

        if (!insumoRepository.existsById(dto.getFkInsumo())) {
            throw new InsumoIdDontExistsException();
        }

        boolean mesmoInsumo = entity.getInsumo().getIdInsumo().equals(dto.getFkInsumo());
        if (!mesmoInsumo) {
            boolean duplicado = repository.findByProdutoIdProduto(entity.getProduto().getIdProduto())
                    .stream()
                    .anyMatch(pi -> !pi.getIdProdutoInsumo().equals(id)
                            && pi.getInsumo().getIdInsumo().equals(dto.getFkInsumo()));
            if (duplicado) {
                throw new ProdutoInsumoDuplicateException();
            }

            Insumo novoInsumo = insumoRepository.findById(dto.getFkInsumo())
                    .orElseThrow(InsumoIdDontExistsException::new);
            entity.setInsumo(novoInsumo);
        }

        entity.setQuantidadeUtilizada(dto.getQuantidadeUtilizada());
        entity.setUnidade(dto.getUnidade());
        repository.save(entity);
        return entity;
    }

    public void deletar(Integer id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("ProdutoInsumo não encontrado: " + id);
        }
        repository.deleteById(id);
    }
}
