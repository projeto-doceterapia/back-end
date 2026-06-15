package br.com.doceterapia.api.repository;

import br.com.doceterapia.api.entity.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PagamentoRepository extends JpaRepository<Pagamento, Integer> {

    boolean existsByPedidoIdPedido(Integer pedidoId);

    Optional<Pagamento> findByPedidoIdPedido(Integer pedidoId);
}
