package school.sptech.projetoindividual.service;

import org.springframework.stereotype.Service;
import school.sptech.projetoindividual.dto.ClienteRequestDTO;
import school.sptech.projetoindividual.entity.Cliente;
import school.sptech.projetoindividual.exception.TelefoneAlreadyExistsException;
import school.sptech.projetoindividual.mapper.ClienteMapper;
import school.sptech.projetoindividual.repository.ClienteRepository;

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
}
