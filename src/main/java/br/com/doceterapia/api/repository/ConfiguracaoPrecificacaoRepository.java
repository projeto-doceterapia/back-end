package br.com.doceterapia.api.repository;

import br.com.doceterapia.api.entity.ConfiguracaoPrecificacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfiguracaoPrecificacaoRepository extends JpaRepository<ConfiguracaoPrecificacao, Integer> {

    Optional<ConfiguracaoPrecificacao> findFirstByOrderByIdConfiguracaoPrecificacaoAsc();
}
