package br.com.doceterapia.api.service;

import org.springframework.stereotype.Service;
import br.com.doceterapia.api.dto.InsumoRequestDTO;
import br.com.doceterapia.api.entity.Insumo;
import br.com.doceterapia.api.exception.CategoriaInsumoIdDontExistsException;
import br.com.doceterapia.api.exception.InsumoIdDontExistsException;
import br.com.doceterapia.api.mapper.InsumoMapper;
import br.com.doceterapia.api.repository.CategoriaInsumoRepository;
import br.com.doceterapia.api.repository.InsumoRepository;

import java.util.List;

@Service
public class InsumoService {

    private final InsumoRepository repository;
    private final CategoriaInsumoRepository categoriaInsumoRepository;
    private final InsumoMapper insumoMapper;

    public InsumoService(InsumoRepository repository, CategoriaInsumoRepository categoriaInsumoRepository, InsumoMapper insumoMapper) {
        this.repository = repository;
        this.categoriaInsumoRepository = categoriaInsumoRepository;
        this.insumoMapper = insumoMapper;
    }

    public List<Insumo> listarTodos() {
        return repository.findAll();
    }

    public Insumo cadastrar(InsumoRequestDTO request) {
        if (!categoriaInsumoRepository.existsById(request.getFkCategoriaInsumo())) {
            throw new CategoriaInsumoIdDontExistsException();
        }

        Insumo entity = insumoMapper.toEntity(request);
        repository.save(entity);
        return entity;
    }

    public Insumo atualizar(Integer id, InsumoRequestDTO request) {
        if (!repository.existsById(id)) {
            throw new InsumoIdDontExistsException();
        }

        if (!categoriaInsumoRepository.existsById(request.getFkCategoriaInsumo())) {
            throw new CategoriaInsumoIdDontExistsException();
        }

        Insumo entity = insumoMapper.toEntity(request);
        entity.setIdInsumo(id);
        repository.save(entity);
        return entity;
    }

    public void deletar(Integer id) {
        if (!repository.existsById(id)) {
            throw new InsumoIdDontExistsException();
        }

        repository.deleteById(id);
    }
}
