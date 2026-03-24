package school.sptech.projetoindividual.service;

import org.springframework.stereotype.Service;
import school.sptech.projetoindividual.dto.PedidoRequestDTO;
import school.sptech.projetoindividual.dto.PedidoWithClienteResponseDTO;
import school.sptech.projetoindividual.entity.Pedido;
import school.sptech.projetoindividual.exception.PedidoIdDontExistsException;
import school.sptech.projetoindividual.mapper.PedidoMapper;
import school.sptech.projetoindividual.repository.PedidoRepository;

import java.util.List;
import java.util.Optional;

@Service
public class PedidoService {

    private final PedidoRepository repository;

    public PedidoService(PedidoRepository repository) {
        this.repository = repository;
    }

    public List<PedidoWithClienteResponseDTO> listarTodos() {
        return repository.findAllWithCliente();
    }

    public Pedido cadastrar(PedidoRequestDTO request) {
        Pedido pedido = PedidoMapper.toPedido(request);
        repository.save(pedido);
        return pedido;
    }

    public Pedido atualizar(Integer id, PedidoRequestDTO request) {
        Pedido pedido = PedidoMapper.toPedido(request);
        pedido.setIdPedido(id);
        repository.save(pedido);
        return pedido;
    }

    public void deletar(Integer id) {
        repository.deleteById(id);
    }

    public void atualizarStatus(Integer id, Boolean status) {
        Optional<Pedido> pedidoOptional = repository.findById(id);

        if(pedidoOptional.isEmpty()) {
            throw new PedidoIdDontExistsException();
        } else {
            Pedido pedido = pedidoOptional.get();
            pedido.setStatusConcluido(status);
            repository.save(pedido);
        }
    }
}
