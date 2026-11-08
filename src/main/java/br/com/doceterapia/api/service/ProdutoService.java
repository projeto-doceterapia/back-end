package br.com.doceterapia.api.service;

import org.springframework.stereotype.Service;
import br.com.doceterapia.api.dto.ProdutoRequestDTO;
import br.com.doceterapia.api.dto.ProdutoResponseDTO;
import br.com.doceterapia.api.entity.Produto;
import br.com.doceterapia.api.exception.CategoriaProdutoIdDontExistsException;
import br.com.doceterapia.api.exception.ProdutoIdDontExistsException;
import br.com.doceterapia.api.mapper.ProdutoMapper;
import br.com.doceterapia.api.repository.CategoriaProdutoRepository;
import br.com.doceterapia.api.repository.ProdutoRepository;
import br.com.doceterapia.api.service.precificacao.PrecificacaoProduto;
import br.com.doceterapia.api.service.precificacao.PrecificacaoService;

import java.util.List;
import java.util.Map;

@Service
public class ProdutoService {

    private final ProdutoRepository repository;
    private final CategoriaProdutoRepository categoriaProdutoRepository;
    private final ProdutoMapper mapper;
    private final PrecificacaoService precificacaoService;

    public ProdutoService(ProdutoRepository repository, CategoriaProdutoRepository categoriaProdutoRepository,
                          ProdutoMapper mapper, PrecificacaoService precificacaoService) {
        this.repository = repository;
        this.categoriaProdutoRepository = categoriaProdutoRepository;
        this.mapper = mapper;
        this.precificacaoService = precificacaoService;
    }

    public List<ProdutoResponseDTO> listarTodos() {
        List<Produto> produtos = repository.findAll();
        Map<Integer, PrecificacaoProduto> precificacoes = precificacaoService.calcular(produtos);
        return produtos.stream()
                .map(produto -> mapper.toResponseDTO(produto, precificacoes.get(produto.getIdProduto())))
                .toList();
    }

    public ProdutoResponseDTO buscarPorId(Integer id) {
        Produto produto = repository.findById(id).orElseThrow(ProdutoIdDontExistsException::new);
        return mapper.toResponseDTO(produto, precificacaoService.calcular(produto));
    }

    public ProdutoResponseDTO cadastrar(ProdutoRequestDTO request) {
        if (!categoriaProdutoRepository.existsById(request.getFkCategoriaProduto())) {
            throw new CategoriaProdutoIdDontExistsException();
        }

        Produto entity = mapper.toEntity(request);
        repository.save(entity);
        return mapper.toResponseDTO(entity, precificacaoService.calcular(entity));
    }

    public ProdutoResponseDTO atualizar(Integer id, ProdutoRequestDTO request) {
        if (!repository.existsById(id)) {
            throw new ProdutoIdDontExistsException();
        }

        if (!categoriaProdutoRepository.existsById(request.getFkCategoriaProduto())) {
            throw new CategoriaProdutoIdDontExistsException();
        }

        Produto entity = mapper.toEntity(request);
        entity.setIdProduto(id);
        repository.save(entity);
        return mapper.toResponseDTO(entity, precificacaoService.calcular(entity));
    }

    public void deletar(Integer id) {
        if (!repository.existsById(id)) {
            throw new ProdutoIdDontExistsException();
        }

        repository.deleteById(id);
    }
}
