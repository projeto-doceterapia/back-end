package br.com.doceterapia.api.mapper;

import br.com.doceterapia.api.dto.ProducaoRequestDTO;
import br.com.doceterapia.api.dto.ProducaoResponseDTO;
import br.com.doceterapia.api.entity.Pedido;
import br.com.doceterapia.api.entity.Producao;
import br.com.doceterapia.api.enums.StatusProducao;
import br.com.doceterapia.api.exception.PedidoIdDontExistsException;
import br.com.doceterapia.api.repository.PedidoRepository;
import org.springframework.stereotype.Component;

@Component
public class ProducaoMapper {

    private final PedidoRepository pedidoRepository;

    public ProducaoMapper(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    public Producao toEntity(ProducaoRequestDTO dto) {
        Pedido pedido = pedidoRepository.findById(dto.getPedidoId())
                .orElseThrow(PedidoIdDontExistsException::new);

        Producao entity = new Producao();
        entity.setPedido(pedido);
        entity.setDataInicio(dto.getDataInicio());
        entity.setDataPrevista(dto.getDataPrevista());
        entity.setStatusProducao(StatusProducao.ESPERANDO_FAZER);
        entity.setObservacao(dto.getObservacao());
        return entity;
    }

    public ProducaoResponseDTO toResponseDTO(Producao entity) {
        ProducaoResponseDTO dto = new ProducaoResponseDTO();
        dto.setIdProducao(entity.getIdProducao());
        dto.setPedidoId(entity.getPedido().getIdPedido());
        dto.setDataInicio(entity.getDataInicio());
        dto.setDataPrevista(entity.getDataPrevista());
        dto.setDataFinalizacao(entity.getDataFinalizacao());
        dto.setStatusProducao(entity.getStatusProducao());
        dto.setObservacao(entity.getObservacao());
        return dto;
    }
}
