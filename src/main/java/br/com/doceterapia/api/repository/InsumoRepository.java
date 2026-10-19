package br.com.doceterapia.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import br.com.doceterapia.api.entity.Insumo;
import br.com.doceterapia.api.enums.StatusAtivo;
import br.com.doceterapia.api.enums.UnidadeMedida;

import java.util.List;
import java.util.Optional;

public interface InsumoRepository extends JpaRepository<Insumo, Integer>, JpaSpecificationExecutor<Insumo> {

    Boolean existsByCategoriaIdCategoriaInsumo(Integer idCategoriaInsumo);

    Optional<Insumo> findByNomeNormalizadoAndUnidadeAndStatus(String nomeNormalizado, UnidadeMedida unidade, StatusAtivo status);

    List<Insumo> findByNomeNormalizadoIsNull();

    // Supplies to buy: active supplies whose balance is at or below the minimum.
    @Query("SELECT i FROM Insumo i WHERE i.status = br.com.doceterapia.api.enums.StatusAtivo.ATIVO "
            + "AND i.quantidadeMinima IS NOT NULL AND i.quantidadeAtual IS NOT NULL "
            + "AND i.quantidadeAtual <= i.quantidadeMinima ORDER BY i.nome")
    List<Insumo> buscarNoMinimoOuAbaixo();
}
