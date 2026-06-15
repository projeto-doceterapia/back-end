package br.com.doceterapia.api.service;

import br.com.doceterapia.api.dto.CancelamentoPedidoRequestDTO;
import br.com.doceterapia.api.dto.CancelamentoPedidoResponseDTO;
import br.com.doceterapia.api.entity.CancelamentoPedido;
import br.com.doceterapia.api.entity.Pedido;
import br.com.doceterapia.api.enums.StatusPedido;
import br.com.doceterapia.api.exception.CancelamentoAlreadyExistsException;
import br.com.doceterapia.api.exception.CancelamentoPedidoIdDontExistsException;
import br.com.doceterapia.api.exception.PedidoIdDontExistsException;
import br.com.doceterapia.api.mapper.CancelamentoPedidoMapper;
import br.com.doceterapia.api.repository.CancelamentoPedidoRepository;
import br.com.doceterapia.api.repository.PedidoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CancelamentoPedidoService {

    private final CancelamentoPedidoRepository repository;
    private final PedidoRepository pedidoRepository;
    private final CancelamentoPedidoMapper mapper;
    private final PedidoService pedidoService;

    public CancelamentoPedidoService(CancelamentoPedidoRepository repository,
                                     PedidoRepository pedidoRepository,
                                     CancelamentoPedidoMapper mapper,
                                     PedidoService pedidoService) {
        this.repository = repository;
        this.pedidoRepository = pedidoRepository;
        this.mapper = mapper;
        this.pedidoService = pedidoService;
    }

    public CancelamentoPedidoResponseDTO criar(Integer pedidoId, CancelamentoPedidoRequestDTO dto) {
        if (!pedidoRepository.existsById(pedidoId)) {
            throw new PedidoIdDontExistsException();
        }
        if (repository.existsByPedidoIdPedido(pedidoId)) {
            throw new CancelamentoAlreadyExistsException();
        }
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(PedidoIdDontExistsException::new);
        CancelamentoPedido entity = mapper.toEntity(dto);
        entity.setPedido(pedido);
        entity.setDataCancelamento(LocalDateTime.now());
        repository.save(entity);
        pedidoService.atualizarStatus(pedidoId, StatusPedido.CANCELADO);
        return mapper.toResponseDTO(entity);
    }

    public List<CancelamentoPedidoResponseDTO> listarTodos() {
        return repository.findAll().stream()
                .map(mapper::toResponseDTO)
                .toList();
    }

    public CancelamentoPedidoResponseDTO buscarPorId(Integer id) {
        CancelamentoPedido entity = repository.findById(id)
                .orElseThrow(CancelamentoPedidoIdDontExistsException::new);
        return mapper.toResponseDTO(entity);
    }

    public CancelamentoPedidoResponseDTO buscarPorPedidoId(Integer pedidoId) {
        CancelamentoPedido entity = repository.findByPedidoIdPedido(pedidoId)
                .orElseThrow(CancelamentoPedidoIdDontExistsException::new);
        return mapper.toResponseDTO(entity);
    }

    public CancelamentoPedidoResponseDTO atualizar(Integer id, CancelamentoPedidoRequestDTO dto) {
        CancelamentoPedido entity = repository.findById(id)
                .orElseThrow(CancelamentoPedidoIdDontExistsException::new);
        if (dto.getTipoCancelamento() != null) {
            entity.setTipoCancelamento(dto.getTipoCancelamento());
        }
        if (dto.getTipoRetorno() != null) {
            entity.setTipoRetorno(dto.getTipoRetorno());
        }
        if (dto.getValorRetorno() != null) {
            entity.setValorRetorno(dto.getValorRetorno());
        }
        if (dto.getObservacao() != null) {
            entity.setObservacao(dto.getObservacao());
        }
        repository.save(entity);
        return mapper.toResponseDTO(entity);
    }

    public void deletar(Integer id) {
        if (!repository.existsById(id)) {
            throw new CancelamentoPedidoIdDontExistsException();
        }
        repository.deleteById(id);
    }
}
