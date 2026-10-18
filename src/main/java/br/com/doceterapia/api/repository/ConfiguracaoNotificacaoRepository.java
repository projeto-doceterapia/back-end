package br.com.doceterapia.api.repository;

import br.com.doceterapia.api.entity.ConfiguracaoNotificacao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfiguracaoNotificacaoRepository extends JpaRepository<ConfiguracaoNotificacao, Integer> {
}
