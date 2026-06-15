package br.com.doceterapia.api.service;

import org.springframework.stereotype.Service;
import br.com.doceterapia.api.dto.ProdutoRequestDTO;
import br.com.doceterapia.api.entity.Produto;
import br.com.doceterapia.api.exception.CategoriaProdutoIdDontExistsException;
import br.com.doceterapia.api.exception.ProdutoIdDontExistsException;
import br.com.doceterapia.api.mapper.ProdutoMapper;
import br.com.doceterapia.api.repository.CategoriaProdutoRepository;
import br.com.doceterapia.api.repository.ProdutoRepository;

import java.util.List;

@Service
public class ProdutoService {

    private final ProdutoRepository repository;
    private final CategoriaProdutoRepository categoriaProdutoRepository;
    private final ProdutoMapper mapper;

    public ProdutoService(ProdutoRepository repository, CategoriaProdutoRepository categoriaProdutoRepository, ProdutoMapper mapper) {
        this.repository = repository;
        this.categoriaProdutoRepository = categoriaProdutoRepository;
        this.mapper = mapper;
    }

    public List<Produto> listarTodos() {
        return repository.findAll();
    }

    public Produto cadastrar(ProdutoRequestDTO request) {
        if (!categoriaProdutoRepository.existsById(request.getFkCategoriaProduto())) {
            throw new CategoriaProdutoIdDontExistsException();
        }

        Produto entity = mapper.toEntity(request);
        repository.save(entity);
        return entity;
    }

    public Produto atualizar(Integer id, ProdutoRequestDTO request) {
        if (!repository.existsById(id)) {
            throw new ProdutoIdDontExistsException();
        }

        if (!categoriaProdutoRepository.existsById(request.getFkCategoriaProduto())) {
            throw new CategoriaProdutoIdDontExistsException();
        }

        Produto entity = mapper.toEntity(request);
        entity.setIdProduto(id);
        repository.save(entity);
        return entity;
    }

    public void deletar(Integer id) {
        if (!repository.existsById(id)) {
            throw new ProdutoIdDontExistsException();
        }

        repository.deleteById(id);
    }
}
