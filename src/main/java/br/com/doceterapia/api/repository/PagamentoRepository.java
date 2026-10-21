package br.com.doceterapia.api.repository;

import br.com.doceterapia.api.entity.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PagamentoRepository extends JpaRepository<Pagamento, Integer> {

    boolean existsByPedidoIdPedido(Integer pedidoId);

    Optional<Pagamento> findByPedidoIdPedido(Integer pedidoId);

    // Final payments to check: delivered orders whose payment is not yet settled.
    @Query("SELECT pg FROM Pagamento pg JOIN pg.pedido p "
            + "WHERE p.statusPedido = br.com.doceterapia.api.enums.StatusPedido.ENTREGUE "
            + "AND pg.statusPagamento <> br.com.doceterapia.api.enums.StatusPagamento.PAGO_INTEGRAL "
            + "ORDER BY p.idPedido")
    List<Pagamento> buscarPagamentosFinaisPendentes();
}
