package br.com.doceterapia.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import br.com.doceterapia.api.dto.ClienteMetricasDTO;
import br.com.doceterapia.api.entity.Pedido;
import br.com.doceterapia.api.enums.StatusPedido;

import br.com.doceterapia.api.repository.projecao.ContagemPorDia;
import br.com.doceterapia.api.repository.projecao.ContagemPorStatus;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    @Query("SELECT p FROM Pedido p JOIN FETCH p.cliente WHERE p.dataEntrega BETWEEN :de AND :ate AND p.statusPedido IN :status ORDER BY p.statusPedido, p.dataEntrega, p.idPedido")
    List<Pedido> buscarPorPeriodo(@Param("de") LocalDate de, @Param("ate") LocalDate ate, @Param("status") Collection<StatusPedido> status);
    @Query("SELECT p FROM Pedido p JOIN FETCH p.cliente c WHERE c.idCliente = :clienteId AND p.statusPedido IN :status ORDER BY p.statusPedido, p.dataEntrega, p.idPedido")
    List<Pedido> buscarPorCliente(@Param("clienteId") Integer clienteId, @Param("status") Collection<StatusPedido> status);
    @Query("SELECT p FROM Pedido p JOIN FETCH p.cliente c WHERE c.idCliente = :clienteId AND p.dataEntrega BETWEEN :de AND :ate AND p.statusPedido IN :status ORDER BY p.statusPedido, p.dataEntrega, p.idPedido")
    List<Pedido> buscarPorClienteEPeriodo(@Param("clienteId") Integer clienteId, @Param("de") LocalDate de, @Param("ate") LocalDate ate, @Param("status") Collection<StatusPedido> status);
    @Query("SELECT p FROM Pedido p JOIN FETCH p.cliente WHERE p.idPedido = :id")
    java.util.Optional<Pedido> buscarDetalhePorId(@Param("id") Integer id);

    @Query("SELECT COUNT(p) FROM Pedido p WHERE p.dataEntrega = :dataEntrega "
            + "AND p.statusPedido <> br.com.doceterapia.api.enums.StatusPedido.CANCELADO")
    long contarAtivosPorDataEntrega(@Param("dataEntrega") LocalDate dataEntrega);

    List<Pedido> findByStatusPedido(StatusPedido statusPedido);

    // Quotes and cancelled orders are not sales, so they stay out of the client totals.
    @Query("SELECT new br.com.doceterapia.api.dto.ClienteMetricasDTO(p.cliente.idCliente, COUNT(DISTINCT p.idPedido), "
            + "SUM(i.valorTotal), MAX(p.dataEntrega)) "
            + "FROM Pedido p LEFT JOIN ItemPedido i ON i.pedido = p "
            + "WHERE p.statusPedido NOT IN (br.com.doceterapia.api.enums.StatusPedido.CANCELADO, "
            + "br.com.doceterapia.api.enums.StatusPedido.ORCAMENTO) "
            + "GROUP BY p.cliente.idCliente")
    List<ClienteMetricasDTO> resumirPorCliente();

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

    @Query("SELECT new br.com.doceterapia.api.repository.projecao.ContagemPorStatus(p.statusPedido, COUNT(p)) "
            + "FROM Pedido p WHERE p.dataEntrega BETWEEN :de AND :ate GROUP BY p.statusPedido")
    List<ContagemPorStatus> contarPorStatus(@Param("de") LocalDate de, @Param("ate") LocalDate ate);

    @Query("SELECT COUNT(p) FROM Pedido p WHERE p.dataEntrega BETWEEN :de AND :ate "
            + "AND p.statusPedido IN :statuses")
    long contarPorStatusIn(@Param("de") LocalDate de, @Param("ate") LocalDate ate,
                           @Param("statuses") Collection<StatusPedido> statuses);

    @Query("SELECT COUNT(p) FROM Pedido p WHERE p.dataEntrega BETWEEN :de AND :ate "
            + "AND p.statusPedido <> br.com.doceterapia.api.enums.StatusPedido.CANCELADO")
    long contarAtivosEntre(@Param("de") LocalDate de, @Param("ate") LocalDate ate);

    @Query("SELECT new br.com.doceterapia.api.repository.projecao.ContagemPorDia(p.dataEntrega, COUNT(p)) "
            + "FROM Pedido p WHERE p.dataEntrega BETWEEN :de AND :ate "
            + "AND p.statusPedido <> br.com.doceterapia.api.enums.StatusPedido.CANCELADO "
            + "GROUP BY p.dataEntrega ORDER BY p.dataEntrega")
    List<ContagemPorDia> contarAtivosPorDia(@Param("de") LocalDate de, @Param("ate") LocalDate ate);

    @Query("SELECT new br.com.doceterapia.api.repository.projecao.ContagemPorDia(p.dataEntrega, COUNT(p)) "
            + "FROM Pedido p WHERE p.dataEntrega BETWEEN :de AND :ate "
            + "AND p.statusPedido <> br.com.doceterapia.api.enums.StatusPedido.CANCELADO "
            + "GROUP BY p.dataEntrega HAVING COUNT(p) >= :limite ORDER BY p.dataEntrega")
    List<ContagemPorDia> buscarDiasLotados(@Param("de") LocalDate de, @Param("ate") LocalDate ate,
                                           @Param("limite") long limite);
}
