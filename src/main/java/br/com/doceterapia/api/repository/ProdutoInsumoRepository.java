package br.com.doceterapia.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import br.com.doceterapia.api.entity.ProdutoInsumo;

import java.util.List;

public interface ProdutoInsumoRepository extends JpaRepository<ProdutoInsumo, Integer> {

    List<ProdutoInsumo> findByProdutoIdProduto(Integer produtoId);

    boolean existsByProdutoIdProduto(Integer produtoId);

    boolean existsByInsumoIdInsumo(Integer insumoId);

    boolean existsByProdutoIdProdutoAndInsumoIdInsumo(Integer produtoId, Integer insumoId);
}
