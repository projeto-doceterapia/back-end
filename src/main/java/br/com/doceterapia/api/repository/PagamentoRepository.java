package br.com.doceterapia.api.repository;

import br.com.doceterapia.api.entity.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import br.com.doceterapia.api.enums.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PagamentoRepository extends JpaRepository<Pagamento, Integer> {

    boolean existsByPedidoIdPedido(Integer pedidoId);

    Optional<Pagamento> findByPedidoIdPedido(Integer pedidoId);

    @Query("SELECT p.pedido.idPedido FROM Pagamento p WHERE p.idPagamento = :id")
    Optional<Integer> buscarPedidoId(@Param("id") Integer id);

    // Final payments to check: delivered orders whose payment is not yet settled.
    @Query("SELECT pg FROM Pagamento pg JOIN pg.pedido p "
            + "WHERE p.statusPedido = br.com.doceterapia.api.enums.StatusPedido.ENTREGUE "
            + "AND pg.statusPagamento <> br.com.doceterapia.api.enums.StatusPagamento.PAGO_INTEGRAL "
            + "ORDER BY p.idPedido")
    List<Pagamento> buscarPagamentosFinaisPendentes();

    // A payment counts as received only once its date is filled.
    @Query("SELECT COALESCE(SUM(CASE WHEN pg.dataPagamentoSinal IS NOT NULL THEN pg.valorSinal ELSE 0 END), 0) "
            + "+ COALESCE(SUM(CASE WHEN pg.dataPagamentoRestante IS NOT NULL THEN pg.valorRestante ELSE 0 END), 0) "
            + "FROM Pagamento pg JOIN pg.pedido p WHERE p.dataEntrega BETWEEN :de AND :ate "
            + "AND p.statusPedido IN :statuses")
    BigDecimal somarPago(@Param("de") LocalDate de, @Param("ate") LocalDate ate,
                         @Param("statuses") Collection<StatusPedido> statuses);
}
