package school.sptech.projetoindividual.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import school.sptech.projetoindividual.entity.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    Boolean existsByTelefone(String telefone);
}
