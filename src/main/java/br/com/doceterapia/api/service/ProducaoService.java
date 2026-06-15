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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ProducaoService {

    private final ProducaoRepository repository;
    private final ProducaoMapper mapper;
    private final PedidoRepository pedidoRepository;

    private static final Map<StatusProducao, Set<StatusProducao>> TRANSICOES_VALIDAS = Map.of(
            StatusProducao.ESPERANDO_FAZER, Set.of(StatusProducao.EM_PRODUCAO),
            StatusProducao.EM_PRODUCAO, Set.of(StatusProducao.PRONTO_PARA_ENTREGA),
            StatusProducao.PRONTO_PARA_ENTREGA, Set.of(StatusProducao.FINALIZADO),
            StatusProducao.FINALIZADO, Set.of()
    );

    public ProducaoService(ProducaoRepository repository, ProducaoMapper mapper, PedidoRepository pedidoRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.pedidoRepository = pedidoRepository;
    }

    public List<ProducaoResponseDTO> listarTodos() {
        return repository.findAll().stream().map(mapper::toResponseDTO).toList();
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

        if (entity.getStatusProducao() == StatusProducao.FINALIZADO) {
            throw new ProducaoFinalizedException("Produção finalizada não pode ter o status alterado");
        }

        Set<StatusProducao> transicoesPermitidas = TRANSICOES_VALIDAS.get(entity.getStatusProducao());
        if (transicoesPermitidas == null || !transicoesPermitidas.contains(novoStatus)) {
            throw new ProducaoStatusTransitionException(
                    "Transição de " + entity.getStatusProducao() + " para " + novoStatus + " não é permitida");
        }

        entity.setStatusProducao(novoStatus);

        if (novoStatus == StatusProducao.EM_PRODUCAO) {
            entity.setDataInicio(LocalDate.now());
        }

        if (novoStatus == StatusProducao.FINALIZADO) {
            entity.setDataFinalizacao(LocalDate.now());
        }

        repository.save(entity);
        return mapper.toResponseDTO(entity);
    }
}
