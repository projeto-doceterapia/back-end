package br.com.doceterapia.api.service;

import br.com.doceterapia.api.dto.ConfiguracaoPrecificacaoRequestDTO;
import br.com.doceterapia.api.dto.ConfiguracaoPrecificacaoResponseDTO;
import br.com.doceterapia.api.entity.ConfiguracaoPrecificacao;
import br.com.doceterapia.api.mapper.ConfiguracaoPrecificacaoMapper;
import br.com.doceterapia.api.repository.ConfiguracaoPrecificacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfiguracaoPrecificacaoService {

    private final ConfiguracaoPrecificacaoRepository repository;

    public ConfiguracaoPrecificacaoService(ConfiguracaoPrecificacaoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public ConfiguracaoPrecificacaoResponseDTO buscar() {
        return ConfiguracaoPrecificacaoMapper.toResponseDTO(obterOuPadrao());
    }

    @Transactional
    public ConfiguracaoPrecificacaoResponseDTO atualizar(ConfiguracaoPrecificacaoRequestDTO request) {
        ConfiguracaoPrecificacao entity = obterOuPadrao();
        entity.setMargemPadrao(request.margemPadrao());
        entity.setPercentualIndiretos(request.percentualIndiretos());
        return ConfiguracaoPrecificacaoMapper.toResponseDTO(repository.save(entity));
    }

    // The local profile has no seeded row, so a missing row reads as zeros and the first PUT creates it.
    private ConfiguracaoPrecificacao obterOuPadrao() {
        return repository.findFirstByOrderByIdConfiguracaoPrecificacaoAsc()
                .orElseGet(ConfiguracaoPrecificacao::new);
    }
}
