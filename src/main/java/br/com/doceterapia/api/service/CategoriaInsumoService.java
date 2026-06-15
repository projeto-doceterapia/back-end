package br.com.doceterapia.api.service;

import org.springframework.stereotype.Service;
import br.com.doceterapia.api.dto.CategoriaInsumoRequestDTO;
import br.com.doceterapia.api.entity.CategoriaInsumo;
import br.com.doceterapia.api.exception.CategoriaInsumoIdDontExistsException;
import br.com.doceterapia.api.exception.CategoriaInsumoHasInsumosException;
import br.com.doceterapia.api.exception.CategoriaInsumoNomeAlreadyExistsException;
import br.com.doceterapia.api.mapper.CategoriaInsumoMapper;
import br.com.doceterapia.api.repository.CategoriaInsumoRepository;
import br.com.doceterapia.api.repository.InsumoRepository;

import java.util.List;

@Service
public class CategoriaInsumoService {

    private final CategoriaInsumoRepository repository;
    private final InsumoRepository insumoRepository;

    public CategoriaInsumoService(CategoriaInsumoRepository repository, InsumoRepository insumoRepository) {
        this.repository = repository;
        this.insumoRepository = insumoRepository;
    }

    public List<CategoriaInsumo> listarTodos() {
        return repository.findAll();
    }

    public CategoriaInsumo cadastrar(CategoriaInsumoRequestDTO request) {
        if (repository.existsByNome(request.getNome())) {
            throw new CategoriaInsumoNomeAlreadyExistsException();
        }

        CategoriaInsumo entity = CategoriaInsumoMapper.toEntity(request);
        repository.save(entity);
        return entity;
    }

    public CategoriaInsumo atualizar(Integer id, CategoriaInsumoRequestDTO request) {
        if (!repository.existsById(id)) {
            throw new CategoriaInsumoIdDontExistsException();
        }

        if (repository.existsByNomeAndIdCategoriaInsumoNot(request.getNome(), id)) {
            throw new CategoriaInsumoNomeAlreadyExistsException();
        }

        CategoriaInsumo entity = CategoriaInsumoMapper.toEntity(request);
        entity.setIdCategoriaInsumo(id);
        repository.save(entity);
        return entity;
    }

    public void deletar(Integer id) {
        if (!repository.existsById(id)) {
            throw new CategoriaInsumoIdDontExistsException();
        }

        if (insumoRepository.existsByCategoriaIdCategoriaInsumo(id)) {
            throw new CategoriaInsumoHasInsumosException();
        }

        repository.deleteById(id);
    }
}
