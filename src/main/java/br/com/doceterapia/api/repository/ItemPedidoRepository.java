package br.com.doceterapia.api.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import br.com.doceterapia.api.entity.ItemPedido;
import br.com.doceterapia.api.enums.StatusPedido;
import br.com.doceterapia.api.repository.projecao.TotalPorMes;
import br.com.doceterapia.api.repository.projecao.TotalPorProduto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Integer> {

    @Query("SELECT i FROM ItemPedido i JOIN FETCH i.pedido p JOIN FETCH p.cliente JOIN FETCH i.produto pr JOIN FETCH pr.categoriaProduto WHERE p.idPedido IN :pedidoIds ORDER BY p.idPedido, i.idProdutoPedido")
    List<ItemPedido> buscarPorPedidos(@Param("pedidoIds") Collection<Integer> pedidoIds);

    List<ItemPedido> findByPedidoIdPedido(Integer pedidoId);

    @Query("SELECT i.pedido.idPedido FROM ItemPedido i WHERE i.idProdutoPedido = :id")
    java.util.Optional<Integer> buscarPedidoId(@Param("id") Integer id);

    @Query("SELECT new br.com.doceterapia.api.repository.projecao.TotalPorProduto("
            + "pr.idProduto, pr.nome, SUM(i.quantidade)) FROM ItemPedido i JOIN i.pedido p JOIN i.produto pr "
            + "WHERE p.dataEntrega BETWEEN :de AND :ate AND p.statusPedido IN :statuses "
            + "GROUP BY pr.idProduto, pr.nome ORDER BY SUM(i.quantidade) DESC, pr.nome")
    List<TotalPorProduto> somarQuantidadePorProduto(@Param("de") LocalDate de, @Param("ate") LocalDate ate,
                                                    @Param("statuses") Collection<StatusPedido> statuses,
                                                    Pageable limite);

    @Query("SELECT new br.com.doceterapia.api.repository.projecao.TotalPorProduto("
            + "pr.idProduto, pr.nome, SUM(i.valorTotal)) FROM ItemPedido i JOIN i.pedido p JOIN i.produto pr "
            + "WHERE p.dataEntrega BETWEEN :de AND :ate AND p.statusPedido IN :statuses "
            + "GROUP BY pr.idProduto, pr.nome ORDER BY SUM(i.valorTotal) DESC, pr.nome")
    List<TotalPorProduto> somarValorPorProduto(@Param("de") LocalDate de, @Param("ate") LocalDate ate,
                                               @Param("statuses") Collection<StatusPedido> statuses,
                                               Pageable limite);

    @Query("SELECT COALESCE(SUM(i.valorTotal), 0) FROM ItemPedido i JOIN i.pedido p "
            + "WHERE p.dataEntrega BETWEEN :de AND :ate AND p.statusPedido IN :statuses")
    BigDecimal somarValor(@Param("de") LocalDate de, @Param("ate") LocalDate ate,
                          @Param("statuses") Collection<StatusPedido> statuses);

    @Query("SELECT new br.com.doceterapia.api.repository.projecao.TotalPorMes("
            + "YEAR(p.dataEntrega), MONTH(p.dataEntrega), SUM(i.valorTotal)) FROM ItemPedido i JOIN i.pedido p "
            + "WHERE p.dataEntrega BETWEEN :de AND :ate AND p.statusPedido IN :statuses "
            + "GROUP BY YEAR(p.dataEntrega), MONTH(p.dataEntrega) "
            + "ORDER BY YEAR(p.dataEntrega), MONTH(p.dataEntrega)")
    List<TotalPorMes> somarValorPorMes(@Param("de") LocalDate de, @Param("ate") LocalDate ate,
                                       @Param("statuses") Collection<StatusPedido> statuses);
}
