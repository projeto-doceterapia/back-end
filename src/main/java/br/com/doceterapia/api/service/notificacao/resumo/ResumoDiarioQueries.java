package br.com.doceterapia.api.service.notificacao.resumo;

import br.com.doceterapia.api.entity.Insumo;
import br.com.doceterapia.api.entity.Pagamento;
import br.com.doceterapia.api.entity.Pedido;
import br.com.doceterapia.api.enums.StatusPedido;
import br.com.doceterapia.api.repository.InsumoRepository;
import br.com.doceterapia.api.repository.PagamentoRepository;
import br.com.doceterapia.api.repository.PedidoRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Reads the data each daily summary needs and maps it to the
 * compact records used in the template parameters. Keeps every query in one
 * place so the builder and the scheduler stay free of persistence concerns.
 */
@Component
public class ResumoDiarioQueries {

    private final PedidoRepository pedidoRepository;
    private final PagamentoRepository pagamentoRepository;
    private final InsumoRepository insumoRepository;

    public ResumoDiarioQueries(PedidoRepository pedidoRepository,
                               PagamentoRepository pagamentoRepository,
                               InsumoRepository insumoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.insumoRepository = insumoRepository;
    }

    public List<ItemResumoPedido> sinaisParaConferir() {
        return pedidoRepository.findByStatusPedido(StatusPedido.AGUARDANDO_SINAL).stream()
                .map(ResumoDiarioQueries::paraItem)
                .toList();
    }

    public List<ItemResumoPedido> pagamentosFinaisParaConferir() {
        return pagamentoRepository.buscarPagamentosFinaisPendentes().stream()
                .map(Pagamento::getPedido)
                .map(ResumoDiarioQueries::paraItem)
                .toList();
    }

    public List<ItemResumoPedido> entregasHoje(LocalDate hoje) {
        return pedidoRepository.buscarEntregasDoDia(hoje).stream()
                .map(ResumoDiarioQueries::paraItem)
                .toList();
    }

    public List<ItemResumoPedido> pedidosAtrasados(LocalDate hoje) {
        return pedidoRepository.buscarAtrasados(hoje).stream()
                .map(ResumoDiarioQueries::paraItem)
                .toList();
    }

    public List<ItemResumoInsumo> insumosParaComprar() {
        return insumoRepository.buscarNoMinimoOuAbaixo().stream()
                .map(ResumoDiarioQueries::paraItem)
                .toList();
    }

    /**
     * Orders due from {@code de} to {@code ate}, grouped by delivery date and,
     * inside each date, split into orders awaiting the down payment and orders
     * ready to produce. Dates are kept in ascending order.
     */
    public List<GrupoDataResumo> proximosDias(LocalDate de, LocalDate ate) {
        Map<LocalDate, List<Pedido>> porData = pedidoRepository.buscarEntreDatas(de, ate).stream()
                .collect(Collectors.groupingBy(Pedido::getDataEntrega, java.util.LinkedHashMap::new, Collectors.toList()));

        return porData.entrySet().stream()
                .map(entrada -> {
                    List<ItemResumoPedido> sinal = entrada.getValue().stream()
                            .filter(p -> p.getStatusPedido() == StatusPedido.AGUARDANDO_SINAL)
                            .map(ResumoDiarioQueries::paraItem)
                            .toList();
                    List<ItemResumoPedido> prontos = entrada.getValue().stream()
                            .filter(p -> p.getStatusPedido() != StatusPedido.AGUARDANDO_SINAL)
                            .map(ResumoDiarioQueries::paraItem)
                            .toList();
                    return new GrupoDataResumo(entrada.getKey(), prontos, sinal);
                })
                .toList();
    }

    private static ItemResumoPedido paraItem(Pedido pedido) {
        String cliente = pedido.getCliente() != null ? pedido.getCliente().getNome() : null;
        return new ItemResumoPedido(pedido.getIdPedido(), cliente, pedido.getDataEntrega());
    }

    private static ItemResumoInsumo paraItem(Insumo insumo) {
        String unidade = insumo.getUnidade() != null ? insumo.getUnidade().name() : null;
        return new ItemResumoInsumo(insumo.getNome(), insumo.getQuantidadeAtual(),
                insumo.getQuantidadeMinima(), unidade);
    }
}
