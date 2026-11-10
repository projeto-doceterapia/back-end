package br.com.doceterapia.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import br.com.doceterapia.api.entity.ProdutoInsumo;

import java.util.Collection;
import java.util.List;

public interface ProdutoInsumoRepository extends JpaRepository<ProdutoInsumo, Integer> {

    List<ProdutoInsumo> findByProdutoIdProduto(Integer produtoId);

    @Query("select pi from ProdutoInsumo pi join fetch pi.insumo where pi.produto.idProduto in :ids")
    List<ProdutoInsumo> findComInsumoByProdutoIds(@Param("ids") Collection<Integer> ids);

    boolean existsByProdutoIdProduto(Integer produtoId);

    boolean existsByInsumoIdInsumo(Integer insumoId);

    boolean existsByProdutoIdProdutoAndInsumoIdInsumo(Integer produtoId, Integer insumoId);
}
