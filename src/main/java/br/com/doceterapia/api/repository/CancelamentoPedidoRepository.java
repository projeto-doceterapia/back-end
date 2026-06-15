package br.com.doceterapia.api.repository;

import br.com.doceterapia.api.entity.CancelamentoPedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CancelamentoPedidoRepository extends JpaRepository<CancelamentoPedido, Integer> {

    boolean existsByPedidoIdPedido(Integer pedidoId);

    Optional<CancelamentoPedido> findByPedidoIdPedido(Integer pedidoId);
}
