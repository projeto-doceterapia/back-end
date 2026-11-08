package br.com.doceterapia.api.service;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import br.com.doceterapia.api.dto.ClienteMetricasDTO;
import br.com.doceterapia.api.dto.ClienteRequestDTO;
import br.com.doceterapia.api.dto.ClienteResumoResponseDTO;
import br.com.doceterapia.api.entity.Cliente;
import br.com.doceterapia.api.enums.StatusAtivo;
import br.com.doceterapia.api.exception.ClienteIdDontExistsException;
import br.com.doceterapia.api.exception.TelefoneAlreadyExistsException;
import br.com.doceterapia.api.mapper.ClienteMapper;
import br.com.doceterapia.api.repository.ClienteRepository;
import br.com.doceterapia.api.repository.PedidoRepository;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ClienteService {

    private final ClienteRepository repository;
    private final PedidoRepository pedidoRepository;

    public ClienteService(ClienteRepository repository, PedidoRepository pedidoRepository) {
        this.repository = repository;
        this.pedidoRepository = pedidoRepository;
    }

    public List<Cliente> listar(String busca) {
        if (busca == null || busca.isBlank()) {
            return repository.findAll();
        }

        return repository.buscar(busca.trim(), somenteDigitos(busca));
    }

    public List<ClienteResumoResponseDTO> resumir(String busca, boolean incluirInativos, boolean incluirGasto) {
        List<Cliente> clientes = (busca == null || busca.isBlank())
                ? repository.findAll(Sort.by("nome"))
                : repository.buscar(busca.trim(), somenteDigitos(busca));
        Map<Integer, ClienteMetricasDTO> metricas = pedidoRepository.resumirPorCliente().stream()
                .collect(Collectors.toMap(ClienteMetricasDTO::idCliente, Function.identity()));

        return clientes.stream()
                .filter(cliente -> incluirInativos || cliente.getStatus() != StatusAtivo.INATIVO)
                .map(cliente -> ClienteMapper.toClienteResumoResponse(
                        cliente, metricas.get(cliente.getIdCliente()), incluirGasto))
                .toList();
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

    // A client with orders cannot be removed without losing history, so deleting only inactivates.
    public void deletar(Integer id) {
        Cliente cliente = repository.findById(id).orElseThrow(ClienteIdDontExistsException::new);

        cliente.setStatus(StatusAtivo.INATIVO);
        repository.save(cliente);
    }

    public Cliente reativar(Integer id) {
        Cliente cliente = repository.findById(id).orElseThrow(ClienteIdDontExistsException::new);

        cliente.setStatus(StatusAtivo.ATIVO);
        return repository.save(cliente);
    }

    private static String somenteDigitos(String texto) {
        return texto.replaceAll("\\D", "");
    }
}
