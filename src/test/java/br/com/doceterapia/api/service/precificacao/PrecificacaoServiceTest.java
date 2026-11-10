package br.com.doceterapia.api.service.precificacao;

import br.com.doceterapia.api.dto.ConfiguracaoPrecificacaoResponseDTO;
import br.com.doceterapia.api.entity.Insumo;
import br.com.doceterapia.api.entity.Produto;
import br.com.doceterapia.api.entity.ProdutoInsumo;
import br.com.doceterapia.api.enums.UnidadeMedida;
import br.com.doceterapia.api.repository.ProdutoInsumoRepository;
import br.com.doceterapia.api.service.ConfiguracaoPrecificacaoService;
import br.com.doceterapia.api.service.ConversaoUnidadeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para PrecificacaoService")
class PrecificacaoServiceTest {

    @Mock
    private ProdutoInsumoRepository produtoInsumoRepository;

    @Mock
    private ConfiguracaoPrecificacaoService configuracaoService;

    private PrecificacaoService service;

    private Insumo farinha;
    private Insumo acucar;
    private Insumo ovo;
    private Insumo leite;
    private Insumo chocolate;
    private Insumo manteiga;
    private Insumo leiteCondensado;
    private Insumo pote;
    private Insumo forminha;
    private Insumo caixa;

    @BeforeEach
    void setUp() {
        service = new PrecificacaoService(produtoInsumoRepository, configuracaoService, new ConversaoUnidadeService());
        configurar("100.00", "10.00");

        farinha = insumo(1, UnidadeMedida.KG, "6.5000");
        acucar = insumo(2, UnidadeMedida.KG, "5.2000");
        ovo = insumo(3, UnidadeMedida.UNIDADE, "0.9000");
        leite = insumo(4, UnidadeMedida.L, "4.8000");
        chocolate = insumo(5, UnidadeMedida.KG, "45.9000");
        manteiga = insumo(6, UnidadeMedida.KG, "42.0000");
        leiteCondensado = insumo(7, UnidadeMedida.UNIDADE, "7.2900");
        pote = insumo(8, UnidadeMedida.UNIDADE, "1.3500");
        forminha = insumo(9, UnidadeMedida.UNIDADE, "0.0500");
        caixa = insumo(10, UnidadeMedida.UNIDADE, "2.6180");
    }

    private void configurar(String margemPadrao, String indiretos) {
        when(configuracaoService.buscar()).thenReturn(
                new ConfiguracaoPrecificacaoResponseDTO(new BigDecimal(margemPadrao), new BigDecimal(indiretos)));
    }

    private Insumo insumo(int id, UnidadeMedida unidade, String custo) {
        Insumo insumo = new Insumo();
        insumo.setIdInsumo(id);
        insumo.setUnidade(unidade);
        insumo.setCustoUnitario(custo == null ? null : new BigDecimal(custo));
        return insumo;
    }

    private Produto produto(int id, String margem) {
        Produto produto = new Produto();
        produto.setIdProduto(id);
        produto.setMargemLucro(margem == null ? null : new BigDecimal(margem));
        return produto;
    }

    private ProdutoInsumo linha(Produto produto, Insumo insumo, String quantidade, UnidadeMedida unidade) {
        ProdutoInsumo linha = new ProdutoInsumo();
        linha.setProduto(produto);
        linha.setInsumo(insumo);
        linha.setQuantidadeUtilizada(new BigDecimal(quantidade));
        linha.setUnidade(unidade);
        return linha;
    }

    private void fichaDe(ProdutoInsumo... linhas) {
        when(produtoInsumoRepository.findComInsumoByProdutoIds(anyCollection())).thenReturn(List.of(linhas));
    }

    private Produto boloDePote() {
        Produto produto = produto(1, null);
        fichaDe(
                linha(produto, farinha, "60", UnidadeMedida.G),
                linha(produto, acucar, "50", UnidadeMedida.G),
                linha(produto, ovo, "1", UnidadeMedida.UNIDADE),
                linha(produto, chocolate, "40", UnidadeMedida.G),
                linha(produto, pote, "1", UnidadeMedida.UNIDADE));
        return produto;
    }

    @Test
    @DisplayName("Bolo de pote bate com o cálculo manual")
    void boloDePoteBateComCalculoManual() {
        PrecificacaoProduto resultado = service.calcular(boloDePote());

        assertThat(resultado.custoEstimado()).isEqualByComparingTo("5.21");
        assertThat(resultado.precoSugerido()).isEqualByComparingTo("10.50");
    }

    @Test
    @DisplayName("Brigadeiro com margem própria bate com o cálculo manual")
    void brigadeiroComMargemPropriaBateComCalculoManual() {
        Produto produto = produto(2, "150.00");
        fichaDe(
                linha(produto, leiteCondensado, "0.080", UnidadeMedida.UNIDADE),
                linha(produto, chocolate, "5", UnidadeMedida.G),
                linha(produto, manteiga, "2", UnidadeMedida.G),
                linha(produto, forminha, "1", UnidadeMedida.UNIDADE));

        PrecificacaoProduto resultado = service.calcular(produto);

        assertThat(resultado.custoEstimado()).isEqualByComparingTo("1.04");
        assertThat(resultado.precoSugerido()).isEqualByComparingTo("3.00");
        assertThat(resultado.margemAplicada()).isEqualByComparingTo("150.00");
    }

    @Test
    @DisplayName("Bolo de chocolate de 1 kg bate com o cálculo manual")
    void boloDeChocolateBateComCalculoManual() {
        Produto produto = produto(3, null);
        fichaDe(
                linha(produto, farinha, "300", UnidadeMedida.G),
                linha(produto, acucar, "250", UnidadeMedida.G),
                linha(produto, ovo, "4", UnidadeMedida.UNIDADE),
                linha(produto, leite, "240", UnidadeMedida.ML),
                linha(produto, chocolate, "200", UnidadeMedida.G),
                linha(produto, manteiga, "100", UnidadeMedida.G),
                linha(produto, caixa, "1", UnidadeMedida.UNIDADE));

        PrecificacaoProduto resultado = service.calcular(produto);

        assertThat(resultado.custoEstimado()).isEqualByComparingTo("26.40");
        assertThat(resultado.precoSugerido()).isEqualByComparingTo("53.00");
    }

    @Test
    @DisplayName("Converte a unidade da ficha para a unidade do insumo antes de multiplicar")
    void converteUnidadeDaFichaParaUnidadeDoInsumo() {
        configurar("0.00", "0.00");
        Produto produto = produto(1, null);
        fichaDe(
                linha(produto, farinha, "500", UnidadeMedida.G),
                linha(produto, leite, "250", UnidadeMedida.ML));

        PrecificacaoProduto resultado = service.calcular(produto);

        // 0,5 kg x 6,50 + 0,25 L x 4,80
        assertThat(resultado.custoEstimado()).isEqualByComparingTo("4.45");
    }

    @ParameterizedTest(name = "preço base {0} sugere {1}")
    @CsvSource({"12.50,12.50", "12.51,13.00", "12.01,12.50", "0.00,0.00"})
    @DisplayName("Arredonda para cima ao próximo múltiplo de R$ 0,50")
    void arredondaParaCimaAoProximoMultiploDeCinquentaCentavos(String precoBase, String esperado) {
        configurar("0.00", "0.00");
        Produto produto = produto(1, null);
        BigDecimal custo = new BigDecimal(precoBase);
        Insumo unico = insumo(11, UnidadeMedida.UNIDADE, custo.toPlainString());
        fichaDe(linha(produto, unico, "1", UnidadeMedida.UNIDADE));

        PrecificacaoProduto resultado = service.calcular(produto);

        assertThat(resultado.precoSugerido()).isEqualByComparingTo(esperado);
        assertThat(resultado.precoSugerido().scale()).isEqualTo(2);
    }

    @ParameterizedTest(name = "custo {0} vira {1}")
    @CsvSource({"5.2096,5.21", "5.2050,5.21", "5.2049,5.20"})
    @DisplayName("Arredonda o custo ao centavo com HALF_UP")
    void custoArredondaAoCentavo(String custoInsumos, String esperado) {
        configurar("0.00", "0.00");
        Produto produto = produto(1, null);
        Insumo unico = insumo(11, UnidadeMedida.UNIDADE, custoInsumos);
        fichaDe(linha(produto, unico, "1", UnidadeMedida.UNIDADE));

        PrecificacaoProduto resultado = service.calcular(produto);

        assertThat(resultado.custoEstimado()).isEqualByComparingTo(esperado);
        assertThat(resultado.custoEstimado().scale()).isEqualTo(2);
    }

    @Test
    @DisplayName("Margem do produto prevalece sobre a padrão")
    void margemDoProdutoPrevaleceSobreAPadrao() {
        configurar("100.00", "0.00");
        Produto produto = produto(1, "150.00");
        fichaDe(linha(produto, insumo(11, UnidadeMedida.UNIDADE, "10.0000"), "1", UnidadeMedida.UNIDADE));

        PrecificacaoProduto resultado = service.calcular(produto);

        assertThat(resultado.margemAplicada()).isEqualByComparingTo("150.00");
        assertThat(resultado.precoSugerido()).isEqualByComparingTo("25.00");
    }

    @Test
    @DisplayName("Margem nula usa a margem padrão")
    void margemNulaUsaAPadrao() {
        configurar("100.00", "0.00");
        Produto produto = produto(1, null);
        fichaDe(linha(produto, insumo(11, UnidadeMedida.UNIDADE, "10.0000"), "1", UnidadeMedida.UNIDADE));

        PrecificacaoProduto resultado = service.calcular(produto);

        assertThat(resultado.margemAplicada()).isEqualByComparingTo("100.00");
        assertThat(resultado.precoSugerido()).isEqualByComparingTo("20.00");
    }

    @Test
    @DisplayName("Configuração padrão não altera o custo")
    void configuracaoPadraoNaoAlteraOCusto() {
        configurar("0.00", "0.00");
        Produto produto = produto(1, null);
        fichaDe(
                linha(produto, farinha, "60", UnidadeMedida.G),
                linha(produto, ovo, "1", UnidadeMedida.UNIDADE));

        PrecificacaoProduto resultado = service.calcular(produto);

        // 0,06 kg x 6,50 + 0,90
        assertThat(resultado.custoEstimado()).isEqualByComparingTo("1.29");
        assertThat(resultado.precoSugerido()).isEqualByComparingTo("1.50");
    }

    @Test
    @DisplayName("Custo indisponível devolve nulo sem lançar exceção")
    void custoIndisponivelDevolveNulo() {
        Produto semFicha = produto(1, null);
        Produto semCusto = produto(2, null);
        Produto incompativel = produto(3, null);
        when(produtoInsumoRepository.findComInsumoByProdutoIds(anyCollection())).thenReturn(List.of(
                linha(semCusto, insumo(11, UnidadeMedida.KG, null), "10", UnidadeMedida.G),
                linha(incompativel, ovo, "10", UnidadeMedida.G)));

        Map<Integer, PrecificacaoProduto> resultado = service.calcular(List.of(semFicha, semCusto, incompativel));

        for (int id = 1; id <= 3; id++) {
            assertThat(resultado.get(id).custoEstimado()).isNull();
            assertThat(resultado.get(id).precoSugerido()).isNull();
        }
    }

    @Test
    @DisplayName("Novo preço de compra muda o custo na leitura seguinte")
    void novoPrecoDeCompraMudaOCusto() {
        configurar("0.00", "0.00");
        Produto produto = produto(1, null);
        Insumo unico = insumo(11, UnidadeMedida.KG, "10.0000");
        fichaDe(linha(produto, unico, "1000", UnidadeMedida.G));

        BigDecimal antes = service.calcular(produto).custoEstimado();
        unico.setCustoUnitario(new BigDecimal("12.0000"));
        BigDecimal depois = service.calcular(produto).custoEstimado();

        assertThat(antes).isEqualByComparingTo("10.00");
        assertThat(depois).isEqualByComparingTo("12.00");
    }

    @Test
    @DisplayName("Calcula vários produtos com uma consulta de fichas")
    void calculaVariosProdutosComUmaConsulta() {
        Produto primeiro = produto(1, null);
        Produto segundo = produto(2, null);
        List<ProdutoInsumo> linhas = new ArrayList<>();
        linhas.add(linha(primeiro, ovo, "1", UnidadeMedida.UNIDADE));
        linhas.add(linha(segundo, pote, "1", UnidadeMedida.UNIDADE));
        when(produtoInsumoRepository.findComInsumoByProdutoIds(anyCollection())).thenReturn(linhas);

        Map<Integer, PrecificacaoProduto> resultado = service.calcular(List.of(primeiro, segundo));

        assertThat(resultado.get(1).custoEstimado()).isEqualByComparingTo("0.99");
        assertThat(resultado.get(2).custoEstimado()).isEqualByComparingTo("1.49");
        verify(produtoInsumoRepository, times(1)).findComInsumoByProdutoIds(anyCollection());
        verify(configuracaoService, times(1)).buscar();
    }
}
