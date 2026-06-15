package br.com.doceterapia.api.repository;

import br.com.doceterapia.api.entity.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Integer> {

    List<Notificacao> findByLidaFalseOrderByDataCriacaoDesc();
}
