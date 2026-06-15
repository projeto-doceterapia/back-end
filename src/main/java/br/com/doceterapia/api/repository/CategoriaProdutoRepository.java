package br.com.doceterapia.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import br.com.doceterapia.api.entity.CategoriaProduto;

public interface CategoriaProdutoRepository extends JpaRepository<CategoriaProduto, Integer> {

    Boolean existsByNome(String nome);

    Boolean existsByNomeAndIdCategoriaProdutoNot(String nome, Integer idCategoriaProduto);
}
