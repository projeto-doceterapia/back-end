package br.com.doceterapia.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import br.com.doceterapia.api.entity.Producao;

import java.util.Optional;
import java.util.List;
import java.util.Collection;
import java.time.LocalDate;
import br.com.doceterapia.api.enums.StatusProducao;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProducaoRepository extends JpaRepository<Producao, Integer> {

    @Query("SELECT pr FROM Producao pr JOIN FETCH pr.pedido p JOIN FETCH p.cliente WHERE p.dataEntrega BETWEEN :de AND :ate AND pr.statusProducao IN :status ORDER BY p.dataEntrega, pr.idProducao")
    List<Producao> buscarPorPeriodo(@Param("de") LocalDate de, @Param("ate") LocalDate ate, @Param("status") Collection<StatusProducao> status);

    Optional<Producao> findByPedidoIdPedido(Integer pedidoId);

    boolean existsByPedidoIdPedido(Integer pedidoId);
}
