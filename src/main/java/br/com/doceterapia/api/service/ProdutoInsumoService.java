package br.com.doceterapia.api.service;

import org.springframework.stereotype.Service;
import br.com.doceterapia.api.dto.ProdutoInsumoRequestDTO;
import br.com.doceterapia.api.entity.Insumo;
import br.com.doceterapia.api.entity.ProdutoInsumo;
import br.com.doceterapia.api.exception.InsumoIdDontExistsException;
import br.com.doceterapia.api.exception.ProdutoIdDontExistsException;
import br.com.doceterapia.api.exception.ProdutoInsumoIdDontExistsException;
import br.com.doceterapia.api.exception.ProdutoInsumoDuplicateException;
import br.com.doceterapia.api.exception.UnidadeIncompativelException;
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
    private final ConversaoUnidadeService conversaoUnidadeService;

    public ProdutoInsumoService(ProdutoInsumoRepository repository, ProdutoInsumoMapper mapper,
                                ProdutoRepository produtoRepository, InsumoRepository insumoRepository,
                                ConversaoUnidadeService conversaoUnidadeService) {
        this.repository = repository;
        this.mapper = mapper;
        this.produtoRepository = produtoRepository;
        this.insumoRepository = insumoRepository;
        this.conversaoUnidadeService = conversaoUnidadeService;
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
        Insumo insumo = insumoRepository.findById(dto.getFkInsumo())
                .orElseThrow(InsumoIdDontExistsException::new);
        validarUnidade(dto, insumo);
        if (repository.existsByProdutoIdProdutoAndInsumoIdInsumo(produtoId, dto.getFkInsumo())) {
            throw new ProdutoInsumoDuplicateException();
        }

        ProdutoInsumo entity = mapper.toEntity(dto, produtoRepository.getReferenceById(produtoId));
        repository.save(entity);
        return entity;
    }

    public ProdutoInsumo atualizar(Integer produtoId, Integer id, ProdutoInsumoRequestDTO dto) {
        ProdutoInsumo entity = buscarDoProduto(produtoId, id);

        Insumo novoInsumo = insumoRepository.findById(dto.getFkInsumo())
                .orElseThrow(InsumoIdDontExistsException::new);
        validarUnidade(dto, novoInsumo);

        boolean mesmoInsumo = entity.getInsumo().getIdInsumo().equals(dto.getFkInsumo());
        if (!mesmoInsumo) {
            boolean duplicado = repository.findByProdutoIdProduto(produtoId)
                    .stream()
                    .anyMatch(pi -> !pi.getIdProdutoInsumo().equals(id)
                            && pi.getInsumo().getIdInsumo().equals(dto.getFkInsumo()));
            if (duplicado) {
                throw new ProdutoInsumoDuplicateException();
            }
            entity.setInsumo(novoInsumo);
        }

        entity.setQuantidadeUtilizada(dto.getQuantidadeUtilizada());
        entity.setUnidade(dto.getUnidade());
        repository.save(entity);
        return entity;
    }

    public void deletar(Integer produtoId, Integer id) {
        repository.delete(buscarDoProduto(produtoId, id));
    }

    private ProdutoInsumo buscarDoProduto(Integer produtoId, Integer id) {
        return repository.findById(id)
                .filter(linha -> linha.getProduto().getIdProduto().equals(produtoId))
                .orElseThrow(ProdutoInsumoIdDontExistsException::new);
    }

    private void validarUnidade(ProdutoInsumoRequestDTO dto, Insumo insumo) {
        if (!conversaoUnidadeService.saoCompativeis(dto.getUnidade(), insumo.getUnidade())) {
            throw new UnidadeIncompativelException(dto.getUnidade(), insumo.getUnidade());
        }
    }
}
