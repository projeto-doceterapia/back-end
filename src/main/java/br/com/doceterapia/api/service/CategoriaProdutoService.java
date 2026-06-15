package br.com.doceterapia.api.service;

import org.springframework.stereotype.Service;
import br.com.doceterapia.api.dto.CategoriaProdutoRequestDTO;
import br.com.doceterapia.api.entity.CategoriaProduto;
import br.com.doceterapia.api.exception.CategoriaProdutoHasProdutosException;
import br.com.doceterapia.api.exception.CategoriaProdutoIdDontExistsException;
import br.com.doceterapia.api.exception.CategoriaProdutoNomeAlreadyExistsException;
import br.com.doceterapia.api.mapper.CategoriaProdutoMapper;
import br.com.doceterapia.api.repository.CategoriaProdutoRepository;
import br.com.doceterapia.api.repository.ProdutoRepository;

import java.util.List;

@Service
public class CategoriaProdutoService {

    private final CategoriaProdutoRepository repository;
    private final ProdutoRepository produtoRepository;

    public CategoriaProdutoService(CategoriaProdutoRepository repository, ProdutoRepository produtoRepository) {
        this.repository = repository;
        this.produtoRepository = produtoRepository;
    }

    public List<CategoriaProduto> listarTodos() {
        return repository.findAll();
    }

    public CategoriaProduto cadastrar(CategoriaProdutoRequestDTO request) {
        if (repository.existsByNome(request.getNome())) {
            throw new CategoriaProdutoNomeAlreadyExistsException();
        }

        CategoriaProduto entity = CategoriaProdutoMapper.toEntity(request);
        repository.save(entity);
        return entity;
    }

    public CategoriaProduto atualizar(Integer id, CategoriaProdutoRequestDTO request) {
        if (!repository.existsById(id)) {
            throw new CategoriaProdutoIdDontExistsException();
        }

        if (repository.existsByNomeAndIdCategoriaProdutoNot(request.getNome(), id)) {
            throw new CategoriaProdutoNomeAlreadyExistsException();
        }

        CategoriaProduto entity = CategoriaProdutoMapper.toEntity(request);
        entity.setIdCategoriaProduto(id);
        repository.save(entity);
        return entity;
    }

    public void deletar(Integer id) {
        if (!repository.existsById(id)) {
            throw new CategoriaProdutoIdDontExistsException();
        }

        if (produtoRepository.existsByCategoriaProdutoIdCategoriaProduto(id)) {
            throw new CategoriaProdutoHasProdutosException();
        }

        repository.deleteById(id);
    }
}
