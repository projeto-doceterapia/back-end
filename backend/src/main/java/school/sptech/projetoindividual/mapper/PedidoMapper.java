package school.sptech.projetoindividual.mapper;

import school.sptech.projetoindividual.dto.PedidoRequestDTO;
import school.sptech.projetoindividual.dto.PedidoResponseDTO;
import school.sptech.projetoindividual.entity.Pedido;

public class PedidoMapper {

    public static Pedido toPedido(PedidoRequestDTO request){
        Pedido pedido = new Pedido();
        pedido.setFkCliente(request.getFkCliente());
        pedido.setDescricao(request.getDescricao());
        pedido.setDataEntrega(request.getDataEntrega());
        pedido.setValor(request.getValor());
        return pedido;
    }

    public static PedidoResponseDTO toPedidoResponse(Pedido pedido) {
        PedidoResponseDTO response = new PedidoResponseDTO();
        response.setIdPedido(pedido.getIdPedido());
        response.setFkCliente(pedido.getFkCliente());
        response.setDescricao(pedido.getDescricao());
        response.setDataEntrega(pedido.getDataEntrega());
        response.setValor(pedido.getValor());
        response.setStatusConcluido(pedido.getStatusConcluido());
        return response;
    }
}
