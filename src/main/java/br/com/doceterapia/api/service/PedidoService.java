package br.com.doceterapia.api.service;

import org.springframework.stereotype.Service;
import br.com.doceterapia.api.dto.PedidoRequestDTO;
import br.com.doceterapia.api.dto.PedidoWithClienteResponseDTO;
import br.com.doceterapia.api.entity.Pedido;
import br.com.doceterapia.api.exception.PedidoIdDontExistsException;
import br.com.doceterapia.api.mapper.PedidoMapper;
import br.com.doceterapia.api.repository.PedidoRepository;

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
