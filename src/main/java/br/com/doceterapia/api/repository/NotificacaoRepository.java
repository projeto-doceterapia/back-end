package br.com.doceterapia.api.repository;

import br.com.doceterapia.api.entity.Notificacao;
import br.com.doceterapia.api.enums.StatusEnvioNotificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Integer> {

    List<Notificacao> findByLidaFalseOrderByDataCriacaoDesc();

    Optional<Notificacao> findByChaveUnicidade(String chaveUnicidade);

    Optional<Notificacao> findFirstByChaveUnicidadeStartingWithOrderByDataCriacaoDesc(String prefixo);

    // Window overlaps [de, ate] when janelaInicio <= ate and janelaFim >= de.
    List<Notificacao> findByStatusEnvioAndJanelaInicioLessThanEqualAndJanelaFimGreaterThanEqual(
            StatusEnvioNotificacao statusEnvio, LocalDateTime ate, LocalDateTime de);

    @Modifying
    @Query("UPDATE Notificacao n "
            + "SET n.statusEnvio = br.com.doceterapia.api.enums.StatusEnvioNotificacao.NAO_ENVIAR "
            + "WHERE n.statusEnvio = br.com.doceterapia.api.enums.StatusEnvioNotificacao.PENDENTE")
    int descartarPendentes();
}
