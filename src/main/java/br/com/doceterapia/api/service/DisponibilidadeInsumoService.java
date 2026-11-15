package br.com.doceterapia.api.service;

import br.com.doceterapia.api.entity.Insumo;
import br.com.doceterapia.api.entity.Pedido;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class DisponibilidadeInsumoService {
    public BigDecimal disponivel(Insumo insumo, Pedido pedidoEmAnalise) {
        return insumo.getQuantidadeAtual();
    }
}
