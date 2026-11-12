package br.com.doceterapia.api.service;

import br.com.doceterapia.api.enums.StatusPedido;
import br.com.doceterapia.api.exception.StatusTransitionInvalidException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class MaquinaStatusPedido {

    private static final Map<StatusPedido, Set<StatusPedido>> TRANSICOES_VALIDAS = Map.ofEntries(
            Map.entry(StatusPedido.ORCAMENTO, Set.of(StatusPedido.AGUARDANDO_SINAL, StatusPedido.CANCELADO)),
            Map.entry(StatusPedido.AGUARDANDO_SINAL, Set.of(StatusPedido.ESPERANDO_FAZER, StatusPedido.CANCELADO)),
            Map.entry(StatusPedido.ESPERANDO_FAZER, Set.of(StatusPedido.EM_PRODUCAO, StatusPedido.CANCELADO)),
            Map.entry(StatusPedido.EM_PRODUCAO, Set.of(StatusPedido.AGUARDANDO_ENTREGA, StatusPedido.CANCELADO)),
            Map.entry(StatusPedido.AGUARDANDO_ENTREGA, Set.of(StatusPedido.ENTREGUE, StatusPedido.CANCELADO)),
            Map.entry(StatusPedido.ENTREGUE, Set.of())
    );

    public void validar(StatusPedido atual, StatusPedido novo) {
        Set<StatusPedido> permitidas = TRANSICOES_VALIDAS.get(atual);
        if (permitidas == null || !permitidas.contains(novo)) {
            throw new StatusTransitionInvalidException(
                    "Transição de " + atual + " para " + novo + " não é permitida");
        }
    }
}
