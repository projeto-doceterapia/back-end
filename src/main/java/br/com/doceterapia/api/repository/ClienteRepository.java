package br.com.doceterapia.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import br.com.doceterapia.api.entity.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    Boolean existsByTelefone(String telefone);

    Boolean existsByTelefoneAndIdClienteNot(String telefone, Integer idCliente);
}
