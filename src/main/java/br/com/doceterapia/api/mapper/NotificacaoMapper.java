package br.com.doceterapia.api.mapper;

import br.com.doceterapia.api.dto.NotificacaoRequestDTO;
import br.com.doceterapia.api.dto.NotificacaoResponseDTO;
import br.com.doceterapia.api.entity.Insumo;
import br.com.doceterapia.api.entity.Notificacao;
import br.com.doceterapia.api.entity.Pedido;
import br.com.doceterapia.api.exception.InsumoIdDontExistsException;
import br.com.doceterapia.api.exception.PedidoIdDontExistsException;
import br.com.doceterapia.api.repository.InsumoRepository;
import br.com.doceterapia.api.repository.PedidoRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class NotificacaoMapper {

    private final PedidoRepository pedidoRepository;
    private final InsumoRepository insumoRepository;

    public NotificacaoMapper(PedidoRepository pedidoRepository, InsumoRepository insumoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.insumoRepository = insumoRepository;
    }

    public Notificacao toEntity(NotificacaoRequestDTO dto) {
        Notificacao entity = new Notificacao();
        entity.setTipo(dto.getTipo());
        entity.setTitulo(dto.getTitulo());
        entity.setMensagem(dto.getMensagem());
        entity.setDataCriacao(LocalDateTime.now());
        entity.setLida(false);

        if (dto.getFkPedido() != null) {
            Pedido pedido = pedidoRepository.findById(dto.getFkPedido())
                    .orElseThrow(PedidoIdDontExistsException::new);
            entity.setPedido(pedido);
        }

        if (dto.getFkInsumo() != null) {
            Insumo insumo = insumoRepository.findById(dto.getFkInsumo())
                    .orElseThrow(InsumoIdDontExistsException::new);
            entity.setInsumo(insumo);
        }

        return entity;
    }

    public NotificacaoResponseDTO toResponseDTO(Notificacao entity) {
        NotificacaoResponseDTO dto = new NotificacaoResponseDTO();
        dto.setIdNotificacao(entity.getIdNotificacao());
        dto.setTipo(entity.getTipo());
        dto.setTitulo(entity.getTitulo());
        dto.setMensagem(entity.getMensagem());
        dto.setDataCriacao(entity.getDataCriacao());
        dto.setLida(entity.getLida());
        dto.setFkPedido(entity.getPedido() != null ? entity.getPedido().getIdPedido() : null);
        dto.setFkInsumo(entity.getInsumo() != null ? entity.getInsumo().getIdInsumo() : null);
        return dto;
    }
}
