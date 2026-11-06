package br.com.doceterapia.api.repository;

import br.com.doceterapia.api.entity.ConfiguracaoNotificacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfiguracaoNotificacaoRepository extends JpaRepository<ConfiguracaoNotificacao, Integer> {

    Optional<ConfiguracaoNotificacao> findFirstByOrderByIdConfiguracaoNotificacaoAsc();
}
