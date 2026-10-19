package br.com.doceterapia.api.service.notificacao.resumo;

import br.com.doceterapia.api.enums.TipoNotificacao;
import br.com.doceterapia.api.service.notificacao.ComandoNotificacao;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Builds the {@link ComandoNotificacao} for each daily summary from the query
 * results. Every section is rendered, and an empty section becomes the text
 * "nada pendente" so a summary is always meaningful even on a quiet day.
 */
@Component
public class ResumoDiarioBuilder {

    private static final String NADA_PENDENTE = "nada pendente";

    public ComandoNotificacao inicioDia(LocalDate dia, LocalTime horario, ParametrosResumoInicioDia parametros) {
        String mensagem = String.join("\n",
                "Resumo de início do dia " + dia + ":",
                "Sinais a conferir: " + resumoPedidos(parametros.sinaisParaConferir()),
                "Pagamentos finais a conferir: " + resumoPedidos(parametros.pagamentosFinaisParaConferir()),
                "Entregas de hoje: " + resumoPedidos(parametros.entregasHoje()),
                "Pedidos atrasados: " + resumoPedidos(parametros.pedidosAtrasados()));

        return ComandoNotificacao.resumo(
                TipoNotificacao.RESUMO_INICIO_DIA,
                "Resumo de início do dia",
                mensagem,
                "RESUMO_INICIO_DIA:" + dia,
                "resumo_inicio_dia",
                parametros,
                horario);
    }

    public ComandoNotificacao fimDia(LocalDate dia, LocalTime horario, ParametrosResumoFimDia parametros) {
        String mensagem = String.join("\n",
                "Resumo de fim do dia " + dia + ":",
                "Próximos dias: " + resumoProximosDias(parametros.proximosDias()),
                "Insumos para comprar: " + resumoInsumos(parametros.insumosParaComprar()));

        return ComandoNotificacao.resumo(
                TipoNotificacao.RESUMO_FIM_DIA,
                "Resumo de fim do dia",
                mensagem,
                "RESUMO_FIM_DIA:" + dia,
                "resumo_fim_dia",
                parametros,
                horario);
    }

    private String resumoPedidos(List<ItemResumoPedido> itens) {
        if (itens.isEmpty()) {
            return NADA_PENDENTE;
        }
        return String.valueOf(itens.size());
    }

    private String resumoInsumos(List<ItemResumoInsumo> itens) {
        if (itens.isEmpty()) {
            return NADA_PENDENTE;
        }
        return String.valueOf(itens.size());
    }

    private String resumoProximosDias(List<GrupoDataResumo> grupos) {
        if (grupos.isEmpty()) {
            return NADA_PENDENTE;
        }
        return String.valueOf(grupos.size());
    }
}
