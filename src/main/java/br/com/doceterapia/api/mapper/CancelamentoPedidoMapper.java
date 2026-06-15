package br.com.doceterapia.api.mapper;

import br.com.doceterapia.api.dto.CancelamentoPedidoRequestDTO;
import br.com.doceterapia.api.dto.CancelamentoPedidoResponseDTO;
import br.com.doceterapia.api.entity.CancelamentoPedido;
import org.springframework.stereotype.Component;

@Component
public class CancelamentoPedidoMapper {

    public CancelamentoPedido toEntity(CancelamentoPedidoRequestDTO dto) {
        CancelamentoPedido entity = new CancelamentoPedido();
        entity.setTipoCancelamento(dto.getTipoCancelamento());
        entity.setTipoRetorno(dto.getTipoRetorno());
        entity.setValorRetorno(dto.getValorRetorno());
        entity.setObservacao(dto.getObservacao());
        return entity;
    }

    public CancelamentoPedidoResponseDTO toResponseDTO(CancelamentoPedido entity) {
        CancelamentoPedidoResponseDTO response = new CancelamentoPedidoResponseDTO();
        response.setIdCancelamento(entity.getIdCancelamento());
        response.setPedidoId(entity.getPedido().getIdPedido());
        response.setTipoCancelamento(entity.getTipoCancelamento());
        response.setTipoRetorno(entity.getTipoRetorno());
        response.setValorRetorno(entity.getValorRetorno());
        response.setDataCancelamento(entity.getDataCancelamento());
        response.setObservacao(entity.getObservacao());
        return response;
    }
}
