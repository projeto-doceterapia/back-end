package br.com.doceterapia.api.service;

import br.com.doceterapia.api.entity.Insumo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@Service
public class BloqueioInsumoService {

    private final EntityManager entityManager;

    public BloqueioInsumoService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<Insumo> bloquearEmOrdem(Collection<Integer> insumoIds) {
        return insumoIds.stream().distinct().sorted(Comparator.naturalOrder())
                .map(this::buscarComTrava).toList();
    }

    private Insumo buscarComTrava(Integer insumoId) {
        Insumo insumo = entityManager.find(Insumo.class, insumoId, LockModeType.PESSIMISTIC_WRITE);
        if (insumo == null) {
            throw new IllegalStateException("Insumo da ficha técnica não foi encontrado");
        }
        entityManager.refresh(insumo, LockModeType.PESSIMISTIC_WRITE);
        return insumo;
    }
}
