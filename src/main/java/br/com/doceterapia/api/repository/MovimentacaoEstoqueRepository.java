package br.com.doceterapia.api.repository;

import br.com.doceterapia.api.entity.MovimentacaoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.doceterapia.api.repository.projecao.TotalPorMes;

import java.math.BigDecimal;
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
            WHERE m.ocorridoEm >= :inicio AND m.ocorridoEm < :fimExclusivo
              AND (:insumoId IS NULL OR m.insumo.idInsumo = :insumoId)
            ORDER BY m.ocorridoEm DESC, m.idMovimentacao DESC
            """)
    List<MovimentacaoEstoque> buscarPorPeriodo(@Param("insumoId") Integer insumoId,
                                               @Param("inicio") LocalDateTime inicio,
                                               @Param("fimExclusivo") LocalDateTime fimExclusivo);

    // Only purchases carry a unit cost that reflects money spent; adjustments and consumption do not.
    @Query("SELECT COALESCE(SUM(m.quantidade * m.custoUnitario), 0) FROM MovimentacaoEstoque m "
            + "WHERE m.tipo = br.com.doceterapia.api.enums.TipoMovimentacao.ENTRADA_COMPRA "
            + "AND m.custoUnitario IS NOT NULL AND m.ocorridoEm >= :inicio AND m.ocorridoEm < :fimExclusivo")
    BigDecimal somarGastosCompra(@Param("inicio") LocalDateTime inicio,
                                 @Param("fimExclusivo") LocalDateTime fimExclusivo);

    // Purchases grouped by month, the same cost basis as the period spending.
    @Query("SELECT new br.com.doceterapia.api.repository.projecao.TotalPorMes("
            + "YEAR(m.ocorridoEm), MONTH(m.ocorridoEm), SUM(m.quantidade * m.custoUnitario)) "
            + "FROM MovimentacaoEstoque m "
            + "WHERE m.tipo = br.com.doceterapia.api.enums.TipoMovimentacao.ENTRADA_COMPRA "
            + "AND m.custoUnitario IS NOT NULL AND m.ocorridoEm >= :inicio AND m.ocorridoEm < :fimExclusivo "
            + "GROUP BY YEAR(m.ocorridoEm), MONTH(m.ocorridoEm)")
    List<TotalPorMes> somarGastosCompraPorMes(@Param("inicio") LocalDateTime inicio,
                                              @Param("fimExclusivo") LocalDateTime fimExclusivo);
}
