package br.com.doceterapia.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import br.com.doceterapia.api.entity.Pedido;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    @Query("SELECT p FROM Pedido p JOIN FETCH p.cliente ORDER BY p.statusPedido, p.dataEntrega")
    List<Pedido> findAllWithClienteEager();
}
