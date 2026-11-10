package br.com.doceterapia.api.mapper;

import br.com.doceterapia.api.entity.Produto;
import br.com.doceterapia.api.repository.CategoriaProdutoRepository;
import br.com.doceterapia.api.service.precificacao.PrecificacaoProduto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para ProdutoMapper")
class ProdutoMapperTest {

    @Mock
    private CategoriaProdutoRepository categoriaProdutoRepository;

    private boolean abaixo(String precoAtual, String precoSugerido) {
        Produto produto = new Produto();
        produto.setPrecoAtual(precoAtual == null ? null : new BigDecimal(precoAtual));
        BigDecimal sugerido = precoSugerido == null ? null : new BigDecimal(precoSugerido);
        return new ProdutoMapper(categoriaProdutoRepository)
                .toResponseDTO(produto, new PrecificacaoProduto(null, sugerido, null))
                .isPrecoAbaixoDoSugerido();
    }

    @Test
    @DisplayName("Deve indicar preço abaixo do sugerido somente quando os dois existem e o atual é menor")
    void indicaPrecoAbaixoDoSugerido() {
        assertThat(abaixo("10.00", "10.50")).isTrue();
        assertThat(abaixo("10.50", "10.50")).isFalse();
        assertThat(abaixo("11.00", "10.50")).isFalse();
        assertThat(abaixo(null, "10.50")).isFalse();
        assertThat(abaixo("10.00", null)).isFalse();
    }

    @Test
    @DisplayName("Deve copiar custo e preço sugerido calculados para a resposta")
    void copiaValoresCalculados() {
        Produto produto = new Produto();
        PrecificacaoProduto precificacao = new PrecificacaoProduto(new BigDecimal("5.21"), new BigDecimal("10.50"), null);

        var dto = new ProdutoMapper(categoriaProdutoRepository).toResponseDTO(produto, precificacao);

        assertThat(dto.getCustoEstimado()).isEqualByComparingTo("5.21");
        assertThat(dto.getPrecoSugerido()).isEqualByComparingTo("10.50");
    }
}
