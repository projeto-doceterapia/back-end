package br.com.doceterapia.api.mapper;

import br.com.doceterapia.api.dto.PedidoRequestDTO;
import br.com.doceterapia.api.dto.PedidoResponseDTO;
import br.com.doceterapia.api.entity.Pedido;
import br.com.doceterapia.api.exception.ClienteIdDontExistsException;
import br.com.doceterapia.api.repository.ClienteRepository;
import org.springframework.stereotype.Component;

@Component
public class PedidoMapper {

    private final ClienteRepository clienteRepository;

    public PedidoMapper(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public Pedido toPedido(PedidoRequestDTO dto) {
        Pedido pedido = new Pedido();
        pedido.setCliente(clienteRepository.findById(dto.getClienteId())
                .orElseThrow(ClienteIdDontExistsException::new));
        pedido.setTipoPedido(dto.getTipoPedido());
        pedido.setStatusPedido(dto.getStatusPedido());
        pedido.setFormaEntrega(dto.getFormaEntrega());
        pedido.setEnderecoEntrega(dto.getEnderecoEntrega());
        pedido.setDataEntrega(dto.getDataEntrega());
        pedido.setAnotacao(dto.getAnotacao());
        return pedido;
    }

    public PedidoResponseDTO toPedidoResponse(Pedido pedido) {
        PedidoResponseDTO response = new PedidoResponseDTO();
        response.setIdPedido(pedido.getIdPedido());
        response.setClienteId(pedido.getCliente().getIdCliente());
        response.setNomeCliente(pedido.getCliente().getNome());
        response.setTipoPedido(pedido.getTipoPedido());
        response.setStatusPedido(pedido.getStatusPedido());
        response.setFormaEntrega(pedido.getFormaEntrega());
        response.setEnderecoEntrega(pedido.getEnderecoEntrega());
        response.setDataEntrega(pedido.getDataEntrega());
        response.setAnotacao(pedido.getAnotacao());
        response.setDataCriacao(pedido.getDataCriacao());
        response.setDataConfirmacao(pedido.getDataConfirmacao());
        return response;
    }
}
