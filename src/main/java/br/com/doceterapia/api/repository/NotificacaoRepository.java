package br.com.doceterapia.api.repository;

import br.com.doceterapia.api.entity.Notificacao;
import br.com.doceterapia.api.enums.StatusEnvioNotificacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Integer> {

    List<Notificacao> findByLidaFalseOrderByDataCriacaoDesc();

    Optional<Notificacao> findByChaveUnicidade(String chaveUnicidade);

    // Window overlaps [de, ate] when janelaInicio <= ate and janelaFim >= de.
    List<Notificacao> findByStatusEnvioAndJanelaInicioLessThanEqualAndJanelaFimGreaterThanEqual(
            StatusEnvioNotificacao statusEnvio, LocalDateTime ate, LocalDateTime de);
}
