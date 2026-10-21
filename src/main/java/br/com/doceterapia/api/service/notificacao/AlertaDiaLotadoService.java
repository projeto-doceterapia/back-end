package br.com.doceterapia.api.service.notificacao;

import br.com.doceterapia.api.entity.ConfiguracaoFarolAgenda;
import br.com.doceterapia.api.entity.ConfiguracaoNotificacao;
import br.com.doceterapia.api.enums.TipoNotificacao;
import br.com.doceterapia.api.repository.ConfiguracaoFarolAgendaRepository;
import br.com.doceterapia.api.repository.PedidoRepository;
import br.com.doceterapia.api.service.ConfiguracaoNotificacaoService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Decides whether a delivery date just became overbooked. Called by
 * {@code PedidoService} after an order's delivery date is persisted, in the same
 * transaction. The alert fires on the movement that makes the active-order count
 * on a date reach or pass {@link ConfiguracaoFarolAgenda#getLimiteVermelho()}; the
 * daily dedup key keeps a single alert per date.
 */
@Service
public class AlertaDiaLotadoService {

    private final ConfiguracaoFarolAgendaRepository farolRepository;
    private final PedidoRepository pedidoRepository;
    private final ConfiguracaoNotificacaoService configuracaoService;
    private final CriacaoNotificacaoService criacaoService;

    public AlertaDiaLotadoService(ConfiguracaoFarolAgendaRepository farolRepository,
                                  PedidoRepository pedidoRepository,
                                  ConfiguracaoNotificacaoService configuracaoService,
                                  CriacaoNotificacaoService criacaoService) {
        this.farolRepository = farolRepository;
        this.pedidoRepository = pedidoRepository;
        this.configuracaoService = configuracaoService;
        this.criacaoService = criacaoService;
    }

    public void avaliar(LocalDate dataEntrega) {
        if (dataEntrega == null) {
            return;
        }

        ConfiguracaoFarolAgenda farol = farolRepository.findAll().stream().findFirst().orElse(null);
        if (farol == null || farol.getLimiteVermelho() == null) {
            return;
        }

        ConfiguracaoNotificacao config = configuracaoService.obterOuCriar();
        if (!Boolean.TRUE.equals(config.getAlertaDiaLotadoAtivo())) {
            return;
        }

        long ativos = pedidoRepository.contarAtivosPorDataEntrega(dataEntrega);
        if (ativos < farol.getLimiteVermelho()) {
            return;
        }

        String chave = "DIA_LOTADO:" + dataEntrega;
        String mensagem = "A data " + dataEntrega + " atingiu " + ativos
                + " pedidos ativos, o limite da agenda.";

        ComandoNotificacao comando = ComandoNotificacao.imediata(
                TipoNotificacao.DIA_LOTADO,
                "Dia lotado",
                mensagem,
                chave,
                "dia_lotado",
                new ParametrosDiaLotado(dataEntrega, ativos),
                null,
                null);
        criacaoService.criar(comando);
    }
}
