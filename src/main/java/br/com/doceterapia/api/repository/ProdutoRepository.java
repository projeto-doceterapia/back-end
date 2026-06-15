package br.com.doceterapia.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import br.com.doceterapia.api.entity.Produto;

public interface ProdutoRepository extends JpaRepository<Produto, Integer> {

    Boolean existsByCategoriaProdutoIdCategoriaProduto(Integer idCategoriaProduto);
}
