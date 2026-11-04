package br.com.doceterapia.api.service.notificacao.resumo;

import br.com.doceterapia.api.service.notificacao.ParametrosTemplate;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

/**
 * Template parameters for the RESUMO_FIM_DIA notification: the summary date, the
 * orders of the next days grouped by date and the supplies to buy. Both lists are
 * always present (possibly empty); an empty list renders as "nada pendente". The
 * {@code resumo_fim_dia} template of the orchestrator reads ready orders and orders
 * awaiting the down payment as two flat sections, so the groups stay out of the JSON.
 */
public record ParametrosResumoFimDia(
        @JsonFormat(pattern = "dd/MM/yyyy") LocalDate data,
        @JsonIgnore List<GrupoDataResumo> proximosDias,
        @JsonProperty("insumos_comprar") List<ItemResumoInsumo> insumosParaComprar) implements ParametrosTemplate {

    @JsonProperty("pedidos_proximos")
    public List<String> pedidosProximos() {
        return linhasDosGrupos(GrupoDataResumo::prontosParaProduzir);
    }

    @JsonProperty("aguardando_sinal")
    public List<String> aguardandoSinal() {
        return linhasDosGrupos(GrupoDataResumo::aguardandoSinal);
    }

    private List<String> linhasDosGrupos(Function<GrupoDataResumo, List<ItemResumoPedido>> secao) {
        return proximosDias.stream()
                .flatMap(grupo -> secao.apply(grupo).stream())
                .map(ItemResumoPedido::textoComData)
                .toList();
    }
}
