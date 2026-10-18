package br.com.doceterapia.api.service.notificacao;

import br.com.doceterapia.api.entity.Insumo;
import br.com.doceterapia.api.entity.Pedido;
import br.com.doceterapia.api.enums.TipoNotificacao;

import java.time.LocalTime;

/**
 * Typed request to create an automatic notification, consumed by the single
 * creation service. Tasks for immediate alerts (ESTOQUE_BAIXO, DIA_LOTADO) and
 * daily summaries (RESUMO_INICIO_DIA, RESUMO_FIM_DIA) build one of these.
 *
 * <p>{@code janela} selects how the delivery window is computed:
 * {@link Janela#IMEDIATA} starts at the current instant, {@link Janela#RESUMO}
 * starts at {@code horarioResumo} on the current day. In both cases the window
 * is moved to the next business start when it falls outside business hours or
 * working days.
 */
public record ComandoNotificacao(
        TipoNotificacao tipo,
        String titulo,
        String mensagem,
        String chaveUnicidade,
        String templateKey,
        ParametrosTemplate parametros,
        Janela janela,
        LocalTime horarioResumo,
        Pedido pedido,
        Insumo insumo) {

    public enum Janela {
        IMEDIATA,
        RESUMO
    }

    public static ComandoNotificacao imediata(
            TipoNotificacao tipo,
            String titulo,
            String mensagem,
            String chaveUnicidade,
            String templateKey,
            ParametrosTemplate parametros,
            Pedido pedido,
            Insumo insumo) {
        return new ComandoNotificacao(tipo, titulo, mensagem, chaveUnicidade,
                templateKey, parametros, Janela.IMEDIATA, null, pedido, insumo);
    }

    public static ComandoNotificacao resumo(
            TipoNotificacao tipo,
            String titulo,
            String mensagem,
            String chaveUnicidade,
            String templateKey,
            ParametrosTemplate parametros,
            LocalTime horarioResumo) {
        return new ComandoNotificacao(tipo, titulo, mensagem, chaveUnicidade,
                templateKey, parametros, Janela.RESUMO, horarioResumo, null, null);
    }
}
