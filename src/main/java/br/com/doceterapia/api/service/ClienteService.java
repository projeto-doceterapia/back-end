package br.com.doceterapia.api.service;

import org.springframework.stereotype.Service;
import br.com.doceterapia.api.dto.ClienteRequestDTO;
import br.com.doceterapia.api.entity.Cliente;
import br.com.doceterapia.api.exception.ClienteIdDontExistsException;
import br.com.doceterapia.api.exception.TelefoneAlreadyExistsException;
import br.com.doceterapia.api.mapper.ClienteMapper;
import br.com.doceterapia.api.repository.ClienteRepository;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository repository;

    public ClienteService(ClienteRepository repository) {
        this.repository = repository;
    }

    public List<Cliente> listarTodos() {
        return repository.findAll();
    }

    public Cliente cadastrar(ClienteRequestDTO request) {
        Cliente cliente = ClienteMapper.toCliente(request);
        if(repository.existsByTelefone(cliente.getTelefone())) {
            throw new TelefoneAlreadyExistsException();
        }

        repository.save(cliente);
        return cliente;
    }

    public Cliente atualizar(Integer id, ClienteRequestDTO request) {
        if (!repository.existsById(id)) {
            throw new ClienteIdDontExistsException();
        }

        if (repository.existsByTelefoneAndIdClienteNot(request.getTelefone(), id)) {
            throw new TelefoneAlreadyExistsException();
        }

        Cliente cliente = ClienteMapper.toCliente(request);
        cliente.setIdCliente(id);
        repository.save(cliente);
        return cliente;
    }

    public void deletar(Integer id) {
        if (!repository.existsById(id)) {
            throw new ClienteIdDontExistsException();
        }

        repository.deleteById(id);
    }
}
