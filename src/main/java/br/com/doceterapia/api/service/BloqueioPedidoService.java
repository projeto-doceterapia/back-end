package br.com.doceterapia.api.service;

import br.com.doceterapia.api.entity.Pedido;
import br.com.doceterapia.api.exception.PedidoIdDontExistsException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Service;

@Service
public class BloqueioPedidoService {

    private final EntityManager entityManager;

    public BloqueioPedidoService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Pedido buscar(Integer id) {
        Pedido pedido = entityManager.find(Pedido.class, id, LockModeType.PESSIMISTIC_WRITE);
        if (pedido == null) {
            throw new PedidoIdDontExistsException();
        }
        entityManager.refresh(pedido, LockModeType.PESSIMISTIC_WRITE);
        return pedido;
    }
}
