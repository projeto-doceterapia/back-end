package br.com.doceterapia.api.repository.projecao;

import br.com.doceterapia.api.enums.StatusPedido;

public record ContagemPorStatus(StatusPedido status, long quantidade) {
}
