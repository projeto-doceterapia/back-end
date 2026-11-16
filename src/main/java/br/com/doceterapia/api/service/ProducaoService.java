package br.com.doceterapia.api.service;

import org.springframework.stereotype.Service;
import br.com.doceterapia.api.dto.ProducaoRequestDTO;
import br.com.doceterapia.api.dto.ProducaoResponseDTO;
import br.com.doceterapia.api.entity.Pedido;
import br.com.doceterapia.api.entity.Producao;
import br.com.doceterapia.api.enums.StatusProducao;
import br.com.doceterapia.api.exception.PedidoAlreadyHasProducaoException;
import br.com.doceterapia.api.exception.PedidoIdDontExistsException;
import br.com.doceterapia.api.exception.ProducaoFinalizedException;
import br.com.doceterapia.api.exception.ProducaoNotFoundException;
import br.com.doceterapia.api.exception.ProducaoStatusTransitionException;
import br.com.doceterapia.api.mapper.ProducaoMapper;
import br.com.doceterapia.api.repository.PedidoRepository;
import br.com.doceterapia.api.repository.ProducaoRepository;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class ProducaoService {

    private final ProducaoRepository repository;
    private final ProducaoMapper mapper;
    private final PedidoRepository pedidoRepository;
    private final PedidoService pedidoService;

    @Autowired
    public ProducaoService(ProducaoRepository repository, ProducaoMapper mapper, PedidoRepository pedidoRepository,
                           PedidoService pedidoService) {
        this.repository = repository;
        this.mapper = mapper;
        this.pedidoRepository = pedidoRepository;
        this.pedidoService = pedidoService;
    }

    public ProducaoService(ProducaoRepository repository, ProducaoMapper mapper, PedidoRepository pedidoRepository) {
        this(repository, mapper, pedidoRepository, null);
    }

    public ProducaoResponseDTO cadastrar(ProducaoRequestDTO request) {
        if (!pedidoRepository.existsById(request.getPedidoId())) {
            throw new PedidoIdDontExistsException();
        }

        if (repository.existsByPedidoIdPedido(request.getPedidoId())) {
            throw new PedidoAlreadyHasProducaoException();
        }

        Producao entity = mapper.toEntity(request);
        repository.save(entity);
        return mapper.toResponseDTO(entity);
    }

    public ProducaoResponseDTO buscarPorId(Integer id) {
        Producao entity = repository.findById(id)
                .orElseThrow(ProducaoNotFoundException::new);
        return mapper.toResponseDTO(entity);
    }

    public ProducaoResponseDTO atualizar(Integer id, ProducaoRequestDTO request) {
        Producao entity = repository.findById(id)
                .orElseThrow(ProducaoNotFoundException::new);

        if (entity.getStatusProducao() == StatusProducao.FINALIZADO) {
            throw new ProducaoFinalizedException("Produção finalizada não pode ser alterada");
        }

        if (!pedidoRepository.existsById(request.getPedidoId())) {
            throw new PedidoIdDontExistsException();
        }

        Pedido pedido = pedidoRepository.findById(request.getPedidoId())
                .orElseThrow(PedidoIdDontExistsException::new);
        entity.setPedido(pedido);
        entity.setDataInicio(request.getDataInicio());
        entity.setDataPrevista(request.getDataPrevista());
        entity.setObservacao(request.getObservacao());

        repository.save(entity);
        return mapper.toResponseDTO(entity);
    }

    public void deletar(Integer id) {
        if (!repository.existsById(id)) {
            throw new ProducaoNotFoundException();
        }
        repository.deleteById(id);
    }

    public ProducaoResponseDTO atualizarStatus(Integer id, StatusProducao novoStatus) {
        Producao entity = repository.findById(id)
                .orElseThrow(ProducaoNotFoundException::new);
        if (pedidoService == null || novoStatus == StatusProducao.ESPERANDO_FAZER) {
            throw new ProducaoStatusTransitionException(
                    "A produção só pode avançar pelas fases operacionais permitidas do pedido");
        }
        pedidoService.atualizarStatus(entity.getPedido().getIdPedido(), statusPedido(novoStatus));
        return mapper.toResponseDTO(repository.findById(id).orElseThrow(ProducaoNotFoundException::new));
    }

    private br.com.doceterapia.api.enums.StatusPedido statusPedido(StatusProducao status) {
        return switch (status) {
            case EM_PRODUCAO -> br.com.doceterapia.api.enums.StatusPedido.EM_PRODUCAO;
            case PRONTO_PARA_ENTREGA -> br.com.doceterapia.api.enums.StatusPedido.AGUARDANDO_ENTREGA;
            case FINALIZADO -> br.com.doceterapia.api.enums.StatusPedido.ENTREGUE;
            case ESPERANDO_FAZER -> throw new ProducaoStatusTransitionException(
                    "A produção só chega a aguardando fazer pelo registro do sinal");
        };
    }
}
