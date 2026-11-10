package br.com.doceterapia.api.service;

import br.com.doceterapia.api.dto.ConfiguracaoPrecificacaoRequestDTO;
import br.com.doceterapia.api.dto.ConfiguracaoPrecificacaoResponseDTO;
import br.com.doceterapia.api.entity.ConfiguracaoPrecificacao;
import br.com.doceterapia.api.repository.ConfiguracaoPrecificacaoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para ConfiguracaoPrecificacaoService")
class ConfiguracaoPrecificacaoServiceTest {

    @Mock
    private ConfiguracaoPrecificacaoRepository repository;

    @InjectMocks
    private ConfiguracaoPrecificacaoService service;

    @Test
    @DisplayName("Sem linha, o GET devolve zeros sem gravar e o PUT cria uma só")
    void semLinhaDevolvePadraoEPutCria() {
        when(repository.findFirstByOrderByIdConfiguracaoPrecificacaoAsc()).thenReturn(Optional.empty());
        when(repository.save(any(ConfiguracaoPrecificacao.class))).thenAnswer(inv -> inv.getArgument(0));

        ConfiguracaoPrecificacaoResponseDTO padrao = service.buscar();
        verify(repository, never()).save(any());

        ConfiguracaoPrecificacaoResponseDTO salvo = service.atualizar(
                new ConfiguracaoPrecificacaoRequestDTO(new BigDecimal("100.00"), new BigDecimal("10.00")));

        assertThat(padrao.margemPadrao()).isEqualByComparingTo("0");
        assertThat(padrao.percentualIndiretos()).isEqualByComparingTo("0");
        assertThat(salvo.margemPadrao()).isEqualByComparingTo("100.00");
        assertThat(salvo.percentualIndiretos()).isEqualByComparingTo("10.00");
        ArgumentCaptor<ConfiguracaoPrecificacao> captor = ArgumentCaptor.forClass(ConfiguracaoPrecificacao.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getIdConfiguracaoPrecificacao()).isNull();
    }

    @Test
    @DisplayName("Com linha existente, o PUT atualiza a mesma linha")
    void atualizarReaproveitaALinhaExistente() {
        ConfiguracaoPrecificacao existente = new ConfiguracaoPrecificacao();
        existente.setIdConfiguracaoPrecificacao(1);
        when(repository.findFirstByOrderByIdConfiguracaoPrecificacaoAsc()).thenReturn(Optional.of(existente));
        when(repository.save(existente)).thenReturn(existente);

        service.atualizar(new ConfiguracaoPrecificacaoRequestDTO(new BigDecimal("80.00"), new BigDecimal("5.00")));

        assertThat(existente.getMargemPadrao()).isEqualByComparingTo("80.00");
        assertThat(existente.getPercentualIndiretos()).isEqualByComparingTo("5.00");
        assertThat(existente.getIdConfiguracaoPrecificacao()).isEqualTo(1);
    }
}
