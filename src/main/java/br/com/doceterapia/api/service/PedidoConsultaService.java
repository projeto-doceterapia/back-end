package br.com.doceterapia.api.service;

import br.com.doceterapia.api.dto.CancelamentoPedidoResponseDTO;
import br.com.doceterapia.api.dto.ItemPedidoResponseDTO;
import br.com.doceterapia.api.dto.PagamentoResponseDTO;
import br.com.doceterapia.api.dto.PedidoResponseDTO;
import br.com.doceterapia.api.dto.ProducaoResponseDTO;
import br.com.doceterapia.api.enums.StatusPedido;
import br.com.doceterapia.api.enums.StatusProducao;
import br.com.doceterapia.api.exception.FiltroInvalidoException;
import br.com.doceterapia.api.exception.PedidoIdDontExistsException;
import br.com.doceterapia.api.mapper.CancelamentoPedidoMapper;
import br.com.doceterapia.api.mapper.ItemPedidoMapper;
import br.com.doceterapia.api.mapper.PagamentoMapper;
import br.com.doceterapia.api.mapper.PedidoMapper;
import br.com.doceterapia.api.mapper.ProducaoMapper;
import br.com.doceterapia.api.repository.CancelamentoPedidoRepository;
import br.com.doceterapia.api.repository.ItemPedidoRepository;
import br.com.doceterapia.api.repository.PagamentoRepository;
import br.com.doceterapia.api.repository.PedidoRepository;
import br.com.doceterapia.api.repository.ProducaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class PedidoConsultaService {
    private final PedidoRepository pedidos;
    private final ItemPedidoRepository itens;
    private final PagamentoRepository pagamentos;
    private final ProducaoRepository producoes;
    private final CancelamentoPedidoRepository cancelamentos;
    private final PedidoMapper pedidoMapper;
    private final ItemPedidoMapper itemMapper;
    private final PagamentoMapper pagamentoMapper;
    private final ProducaoMapper producaoMapper;
    private final CancelamentoPedidoMapper cancelamentoMapper;

    public PedidoConsultaService(PedidoRepository pedidos, ItemPedidoRepository itens,
                                 PagamentoRepository pagamentos, ProducaoRepository producoes,
                                 CancelamentoPedidoRepository cancelamentos, PedidoMapper pedidoMapper,
                                 ItemPedidoMapper itemMapper, PagamentoMapper pagamentoMapper,
                                 ProducaoMapper producaoMapper, CancelamentoPedidoMapper cancelamentoMapper) {
        this.pedidos = pedidos;
        this.itens = itens;
        this.pagamentos = pagamentos;
        this.producoes = producoes;
        this.cancelamentos = cancelamentos;
        this.pedidoMapper = pedidoMapper;
        this.itemMapper = itemMapper;
        this.pagamentoMapper = pagamentoMapper;
        this.producaoMapper = producaoMapper;
        this.cancelamentoMapper = cancelamentoMapper;
    }
    public List<PedidoResponseDTO> listarPedidos(
            LocalDate de, LocalDate ate, List<StatusPedido> status, Integer clienteId) {
        Collection<StatusPedido> valores = status == null || status.isEmpty()
                ? EnumSet.allOf(StatusPedido.class) : status;
        if (clienteId == null) {
            PeriodoConsulta periodo = PeriodoConsulta.de(de, ate);
            return pedidos.buscarPorPeriodo(periodo.de(), periodo.ate(), valores).stream()
                    .map(pedidoMapper::toPedidoResponse)
                    .toList();
        }
        if ((de == null) != (ate == null)) {
            throw new FiltroInvalidoException("Informe as datas 'de' e 'ate' no formato yyyy-MM-dd.");
        }
        if (de == null) {
            return pedidos.buscarPorCliente(clienteId, valores).stream()
                    .map(pedidoMapper::toPedidoResponse)
                    .toList();
        }
        PeriodoConsulta periodo = PeriodoConsulta.de(de, ate);
        return pedidos.buscarPorClienteEPeriodo(clienteId, periodo.de(), periodo.ate(), valores).stream()
                .map(pedidoMapper::toPedidoResponse)
                .toList();
    }

    public List<PedidoResponseDTO> listarPedidosComParametros(
            LocalDate de, LocalDate ate, String[] status, Integer clienteId) {
        return listarPedidos(de, ate, converterStatus(status, StatusPedido.class), clienteId);
    }

    public PedidoResponseDTO buscarPedido(Integer id) {
        return pedidoMapper.toPedidoResponse(pedidos.buscarDetalhePorId(id)
                .orElseThrow(PedidoIdDontExistsException::new));
    }

    public List<ItemPedidoResponseDTO> listarItens(List<Integer> ids) {
        return itens.buscarPorPedidos(validarIds(ids)).stream().map(itemMapper::toResponseDTO).toList();
    }

    public List<ItemPedidoResponseDTO> listarItensComParametros(String[] ids) {
        return listarItens(converterIds(ids));
    }

    public List<PagamentoResponseDTO> listarPagamentos(List<Integer> ids) {
        return pagamentos.buscarPorPedidos(validarIds(ids)).stream().map(pagamentoMapper::toResponseDTO).toList();
    }

    public List<PagamentoResponseDTO> listarPagamentosComParametros(String[] ids) {
        return listarPagamentos(converterIds(ids));
    }

    public List<ProducaoResponseDTO> listarProducoes(LocalDate de, LocalDate ate, List<StatusProducao> status) {
        PeriodoConsulta periodo = PeriodoConsulta.de(de, ate);
        Collection<StatusProducao> valores = status == null || status.isEmpty()
                ? EnumSet.allOf(StatusProducao.class) : status;
        return producoes.buscarPorPeriodo(periodo.de(), periodo.ate(), valores).stream()
                .map(producaoMapper::toResponseDTO).toList();
    }

    public List<ProducaoResponseDTO> listarProducoesComParametros(LocalDate de, LocalDate ate, String[] status) {
        return listarProducoes(de, ate, converterStatus(status, StatusProducao.class));
    }

    public List<CancelamentoPedidoResponseDTO> listarCancelamentos(LocalDate de, LocalDate ate) {
        PeriodoConsulta periodo = PeriodoConsulta.de(de, ate);
        return cancelamentos.buscarPorPeriodo(periodo.inicio(), periodo.fimExclusivo()).stream()
                .map(cancelamentoMapper::toResponseDTO).toList();
    }

    private List<Integer> validarIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new FiltroInvalidoException("Informe ao menos um pedidoId.");
        }
        List<Integer> distintos = ids.stream().distinct().toList();
        if (distintos.size() > 500) {
            throw new FiltroInvalidoException("Informe no máximo 500 pedidoId por consulta.");
        }
        return distintos;
    }

    private List<Integer> converterIds(String[] valores) {
        List<String> tokens = separarValores(valores);
        try {
            return tokens.stream().map(Integer::valueOf).toList();
        } catch (NumberFormatException exception) {
            throw new FiltroInvalidoException("pedidoId deve conter apenas números inteiros.");
        }
    }

    private <T extends Enum<T>> List<T> converterStatus(String[] valores, Class<T> tipo) {
        try {
            return separarValores(valores).stream().map(valor -> Enum.valueOf(tipo, valor)).toList();
        } catch (IllegalArgumentException exception) {
            throw new FiltroInvalidoException("Status inválido para consulta.");
        }
    }

    private List<String> separarValores(String[] valores) {
        if (valores == null) {
            return List.of();
        }
        return Arrays.stream(valores)
                .filter(Objects::nonNull)
                .flatMap(valor -> Arrays.stream(valor.split(",")))
                .map(String::trim)
                .filter(valor -> !valor.isEmpty())
                .toList();
    }
}
