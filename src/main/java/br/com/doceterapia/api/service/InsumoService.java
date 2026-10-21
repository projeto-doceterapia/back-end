package br.com.doceterapia.api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.com.doceterapia.api.dto.InsumoRequestDTO;
import br.com.doceterapia.api.entity.Insumo;
import br.com.doceterapia.api.exception.CategoriaInsumoIdDontExistsException;
import br.com.doceterapia.api.exception.InsumoHasMovimentacoesException;
import br.com.doceterapia.api.exception.InsumoIdDontExistsException;
import br.com.doceterapia.api.mapper.InsumoMapper;
import br.com.doceterapia.api.repository.CategoriaInsumoRepository;
import br.com.doceterapia.api.repository.InsumoRepository;
import br.com.doceterapia.api.repository.MovimentacaoEstoqueRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
public class InsumoService {

    private final InsumoRepository repository;
    private final CategoriaInsumoRepository categoriaInsumoRepository;
    private final InsumoMapper insumoMapper;
    private final MovimentacaoEstoqueService movimentacaoEstoqueService;
    private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

    public InsumoService(InsumoRepository repository, CategoriaInsumoRepository categoriaInsumoRepository,
                         InsumoMapper insumoMapper, MovimentacaoEstoqueService movimentacaoEstoqueService,
                         MovimentacaoEstoqueRepository movimentacaoEstoqueRepository) {
        this.repository = repository;
        this.categoriaInsumoRepository = categoriaInsumoRepository;
        this.insumoMapper = insumoMapper;
        this.movimentacaoEstoqueService = movimentacaoEstoqueService;
        this.movimentacaoEstoqueRepository = movimentacaoEstoqueRepository;
    }

    public List<Insumo> listarTodos() {
        return repository.findAll();
    }

    @Transactional
    public Insumo cadastrar(InsumoRequestDTO request) {
        if (!categoriaInsumoRepository.existsById(request.getFkCategoriaInsumo())) {
            throw new CategoriaInsumoIdDontExistsException();
        }

        Insumo entity = insumoMapper.toEntity(request);
        BigDecimal saldoInicial = entity.getQuantidadeAtual();
        entity.setQuantidadeAtual(BigDecimal.ZERO);
        repository.save(entity);

        if (saldoInicial != null && saldoInicial.signum() > 0) {
            movimentacaoEstoqueService.registrarSaldoInicial(entity, saldoInicial);
        }
        return entity;
    }

    @Transactional
    public Insumo atualizar(Integer id, InsumoRequestDTO request) {
        Insumo existente = repository.findById(id)
                .orElseThrow(InsumoIdDontExistsException::new);

        if (!categoriaInsumoRepository.existsById(request.getFkCategoriaInsumo())) {
            throw new CategoriaInsumoIdDontExistsException();
        }

        // Adjust the balance first: the delta is computed inside the locked
        // section against the committed balance, so concurrent updates never
        // apply a stale difference. The metadata is written afterwards to
        // survive the row refresh done while locking.
        movimentacaoEstoqueService.registrarAjusteParaAlvo(existente, request.getQuantidadeAtual());

        Insumo dados = insumoMapper.toEntity(request);
        existente.setCategoria(dados.getCategoria());
        existente.setNome(dados.getNome());
        existente.setNomeNormalizado(dados.getNomeNormalizado());
        existente.setQuantidadeMinima(dados.getQuantidadeMinima());
        existente.setUnidade(dados.getUnidade());
        existente.setStatus(dados.getStatus());
        existente.setMarca(dados.getMarca());
        existente.setCustoUnitario(dados.getCustoUnitario());

        repository.save(existente);
        return existente;
    }

    @Transactional
    public void deletar(Integer id) {
        if (!repository.existsById(id)) {
            throw new InsumoIdDontExistsException();
        }

        if (movimentacaoEstoqueRepository.existsByInsumoIdInsumo(id)) {
            throw new InsumoHasMovimentacoesException();
        }

        repository.deleteById(id);
    }
}
