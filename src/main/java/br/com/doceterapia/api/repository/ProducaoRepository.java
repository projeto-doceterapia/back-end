package br.com.doceterapia.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import br.com.doceterapia.api.entity.Producao;

import java.util.Optional;

public interface ProducaoRepository extends JpaRepository<Producao, Integer> {

    Optional<Producao> findByPedidoIdPedido(Integer pedidoId);

    boolean existsByPedidoIdPedido(Integer pedidoId);
}
