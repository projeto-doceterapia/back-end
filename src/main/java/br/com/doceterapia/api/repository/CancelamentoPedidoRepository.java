package br.com.doceterapia.api.repository;

import br.com.doceterapia.api.entity.CancelamentoPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;
import java.time.LocalDateTime;

public interface CancelamentoPedidoRepository extends JpaRepository<CancelamentoPedido, Integer> {

    @Query("SELECT c FROM CancelamentoPedido c JOIN FETCH c.pedido p JOIN FETCH p.cliente WHERE c.dataCancelamento >= :inicio AND c.dataCancelamento < :fimExclusivo ORDER BY c.dataCancelamento, c.idCancelamento")
    List<CancelamentoPedido> buscarPorPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fimExclusivo") LocalDateTime fimExclusivo);

    boolean existsByPedidoIdPedido(Integer pedidoId);

    Optional<CancelamentoPedido> findByPedidoIdPedido(Integer pedidoId);

    @Query("SELECT COALESCE(SUM(c.valorRetorno), 0) FROM CancelamentoPedido c JOIN c.pedido p "
            + "WHERE p.dataEntrega BETWEEN :de AND :ate "
            + "AND p.statusPedido = br.com.doceterapia.api.enums.StatusPedido.CANCELADO "
            + "AND c.tipoRetorno = br.com.doceterapia.api.enums.TipoRetorno.DEVOLUCAO")
    BigDecimal somarDevolvido(@Param("de") LocalDate de, @Param("ate") LocalDate ate);
}
