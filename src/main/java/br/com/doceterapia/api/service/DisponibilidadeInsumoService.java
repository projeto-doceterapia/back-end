package br.com.doceterapia.api.service;

import br.com.doceterapia.api.entity.Insumo;
import br.com.doceterapia.api.entity.Pedido;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class DisponibilidadeInsumoService {
    private final ReservaInsumoService reservaInsumoService;

    public DisponibilidadeInsumoService(ReservaInsumoService reservaInsumoService) {
        this.reservaInsumoService = reservaInsumoService;
    }

    public BigDecimal disponivel(Insumo insumo, Pedido pedidoEmAnalise) {
        Integer pedidoIgnorado = pedidoEmAnalise != null ? pedidoEmAnalise.getIdPedido() : null;
        return reservaInsumoService.disponivelPorInsumo(java.util.List.of(insumo), pedidoIgnorado)
                .get(insumo.getIdInsumo());
    }
}
