package school.sptech.projetoindividual.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import school.sptech.projetoindividual.dto.PedidoWithClienteResponseDTO;
import school.sptech.projetoindividual.entity.Pedido;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    @Query(value = "SELECT * FROM pedido JOIN cliente ON pedido.fk_cliente = cliente.id_cliente ORDER BY " +
            "status_concluido, data_entrega", nativeQuery = true)
    List<PedidoWithClienteResponseDTO> findAllWithCliente();
}
