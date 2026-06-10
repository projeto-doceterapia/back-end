package br.com.doceterapia.api.mapper;

import br.com.doceterapia.api.dto.PedidoRequestDTO;
import br.com.doceterapia.api.dto.PedidoResponseDTO;
import br.com.doceterapia.api.entity.Pedido;

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
