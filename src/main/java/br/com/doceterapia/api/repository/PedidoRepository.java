package br.com.doceterapia.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import br.com.doceterapia.api.dto.PedidoWithClienteResponseDTO;
import br.com.doceterapia.api.entity.Pedido;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    @Query(value = "SELECT * FROM pedido JOIN cliente ON pedido.fk_cliente = cliente.id_cliente ORDER BY " +
            "status_concluido, data_entrega", nativeQuery = true)
    List<PedidoWithClienteResponseDTO> findAllWithCliente();
}
