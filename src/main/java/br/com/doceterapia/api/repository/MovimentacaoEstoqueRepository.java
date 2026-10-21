package br.com.doceterapia.api.repository;

import br.com.doceterapia.api.entity.MovimentacaoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Integer> {

    Optional<MovimentacaoEstoque> findByChaveIdempotencia(String chaveIdempotencia);

    boolean existsByInsumoIdInsumo(Integer idInsumo);

    @Query("SELECT DISTINCT m.insumo.idInsumo FROM MovimentacaoEstoque m WHERE m.chaveNota = :chaveNota ORDER BY m.insumo.idInsumo")
    List<Integer> findInsumoIdsByChaveNota(@Param("chaveNota") String chaveNota);

    @Query("""
            SELECT m FROM MovimentacaoEstoque m
            WHERE m.insumo.idInsumo = :idInsumo
              AND (:de IS NULL OR m.ocorridoEm >= :de)
              AND (:ate IS NULL OR m.ocorridoEm <= :ate)
            ORDER BY m.ocorridoEm DESC, m.idMovimentacao DESC
            """)
    List<MovimentacaoEstoque> buscarHistorico(@Param("idInsumo") Integer idInsumo,
                                              @Param("de") LocalDateTime de,
                                              @Param("ate") LocalDateTime ate);
}
