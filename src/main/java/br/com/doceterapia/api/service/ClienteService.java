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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ClienteService {

    private static final int LOTE_CLIENTES_RESUMO = 1000;

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
        List<Cliente> clientes = ((busca == null || busca.isBlank())
                ? repository.findAll(Sort.by("nome"))
                : repository.buscar(busca.trim(), somenteDigitos(busca))).stream()
                .filter(cliente -> incluirInativos || cliente.getStatus() != StatusAtivo.INATIVO)
                .toList();
        Map<Integer, ClienteMetricasDTO> metricas = metricasDosClientes(clientes, incluirGasto);

        return clientes.stream()
                .map(cliente -> ClienteMapper.toClienteResumoResponse(
                        cliente, metricas.get(cliente.getIdCliente()), incluirGasto))
                .toList();
    }

    // Batches keep the IN list of the aggregate query bounded however many clients the list has.
    private Map<Integer, ClienteMetricasDTO> metricasDosClientes(List<Cliente> clientes, boolean incluirGasto) {
        List<Integer> ids = clientes.stream().map(Cliente::getIdCliente).toList();
        Map<Integer, ClienteMetricasDTO> metricas = new HashMap<>();
        for (int inicio = 0; inicio < ids.size(); inicio += LOTE_CLIENTES_RESUMO) {
            List<Integer> lote = ids.subList(inicio, Math.min(inicio + LOTE_CLIENTES_RESUMO, ids.size()));
            List<ClienteMetricasDTO> parcial = incluirGasto
                    ? pedidoRepository.resumirPorClientes(lote)
                    : pedidoRepository.contarPorClientes(lote);
            parcial.forEach(item -> metricas.put(item.idCliente(), item));
        }
        return metricas;
    }

    public Cliente cadastrar(ClienteRequestDTO request) {
        Cliente cliente = ClienteMapper.toCliente(request);
        if(repository.existsByTelefone(cliente.getTelefone())) {
            throw new TelefoneAlreadyExistsException();
        }

        repository.save(cliente);
        return cliente;
    }

    public Cliente atualizar(Integer id, ClienteRequestDTO request, boolean ehDona) {
        Cliente existente = repository.findById(id).orElseThrow(ClienteIdDontExistsException::new);

        if (repository.existsByTelefoneAndIdClienteNot(request.getTelefone(), id)) {
            throw new TelefoneAlreadyExistsException();
        }

        Cliente cliente = ClienteMapper.toCliente(request);
        cliente.setIdCliente(id);
        // Only the owner may change the status; an employee's value is ignored so the stored one stays.
        if (!ehDona) {
            cliente.setStatus(existente.getStatus());
        }
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
