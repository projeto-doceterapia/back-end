package br.com.doceterapia.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import br.com.doceterapia.api.entity.Cliente;

import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    Boolean existsByTelefone(String telefone);

    Boolean existsByTelefoneAndIdClienteNot(String telefone, Integer idCliente);

    // Stored phones may carry "+", spaces, hyphens and parentheses, so only the digits are compared.
    @Query("""
            SELECT c FROM Cliente c
            WHERE LOWER(c.nome) LIKE LOWER(CONCAT('%', :texto, '%'))
               OR (:digitos <> '' AND REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(c.telefone, '+', ''), ' ', ''), '-', ''), '(', ''), ')', '')
                   LIKE CONCAT('%', :digitos, '%'))
            ORDER BY c.nome
            """)
    List<Cliente> buscar(@Param("texto") String texto, @Param("digitos") String digitos);
}
