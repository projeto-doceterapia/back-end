package br.com.doceterapia.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import br.com.doceterapia.api.entity.Pedido;
import br.com.doceterapia.api.enums.StatusPedido;

import java.time.LocalDate;
import java.util.List;
public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    @Query("SELECT p FROM Pedido p JOIN FETCH p.cliente ORDER BY p.statusPedido, p.dataEntrega")
    List<Pedido> findAllWithClienteEager();

    @Query("SELECT COUNT(p) FROM Pedido p WHERE p.dataEntrega = :dataEntrega "
            + "AND p.statusPedido <> br.com.doceterapia.api.enums.StatusPedido.CANCELADO")
    long contarAtivosPorDataEntrega(@Param("dataEntrega") LocalDate dataEntrega);

    List<Pedido> findByStatusPedido(StatusPedido statusPedido);

    @Query("SELECT p FROM Pedido p WHERE p.dataEntrega = :dataEntrega "
            + "AND p.statusPedido NOT IN (br.com.doceterapia.api.enums.StatusPedido.ENTREGUE, "
            + "br.com.doceterapia.api.enums.StatusPedido.CANCELADO) ORDER BY p.idPedido")
    List<Pedido> buscarEntregasDoDia(@Param("dataEntrega") LocalDate dataEntrega);

    @Query("SELECT p FROM Pedido p WHERE p.dataEntrega < :dataReferencia "
            + "AND p.statusPedido NOT IN (br.com.doceterapia.api.enums.StatusPedido.ENTREGUE, "
            + "br.com.doceterapia.api.enums.StatusPedido.CANCELADO) ORDER BY p.dataEntrega")
    List<Pedido> buscarAtrasados(@Param("dataReferencia") LocalDate dataReferencia);

    @Query("SELECT p FROM Pedido p WHERE p.dataEntrega BETWEEN :de AND :ate "
            + "AND p.statusPedido <> br.com.doceterapia.api.enums.StatusPedido.CANCELADO "
            + "ORDER BY p.dataEntrega, p.idPedido")
    List<Pedido> buscarEntreDatas(@Param("de") LocalDate de, @Param("ate") LocalDate ate);
}
