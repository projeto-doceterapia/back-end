package br.com.doceterapia.api.service.precificacao;

import br.com.doceterapia.api.dto.ConfiguracaoPrecificacaoResponseDTO;
import br.com.doceterapia.api.entity.Insumo;
import br.com.doceterapia.api.entity.Produto;
import br.com.doceterapia.api.entity.ProdutoInsumo;
import br.com.doceterapia.api.repository.ProdutoInsumoRepository;
import br.com.doceterapia.api.service.ConfiguracaoPrecificacaoService;
import br.com.doceterapia.api.service.ConversaoUnidadeService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Computes product cost and suggested price on every read, so nothing derived is stored
 * and a new purchase price reaches the product immediately.
 */
@Service
public class PrecificacaoService {

    private static final BigDecimal DOIS = BigDecimal.valueOf(2);

    private final ProdutoInsumoRepository produtoInsumoRepository;
    private final ConfiguracaoPrecificacaoService configuracaoService;
    private final ConversaoUnidadeService conversaoUnidadeService;

    public PrecificacaoService(ProdutoInsumoRepository produtoInsumoRepository,
                               ConfiguracaoPrecificacaoService configuracaoService,
                               ConversaoUnidadeService conversaoUnidadeService) {
        this.produtoInsumoRepository = produtoInsumoRepository;
        this.configuracaoService = configuracaoService;
        this.conversaoUnidadeService = conversaoUnidadeService;
    }

    public Map<Integer, PrecificacaoProduto> calcular(List<Produto> produtos) {
        Map<Integer, PrecificacaoProduto> resultado = new HashMap<>();
        if (produtos.isEmpty()) {
            return resultado;
        }

        List<Integer> ids = produtos.stream().map(Produto::getIdProduto).toList();
        Map<Integer, List<ProdutoInsumo>> fichas = produtoInsumoRepository.findComInsumoByProdutoIds(ids).stream()
                .collect(Collectors.groupingBy(linha -> linha.getProduto().getIdProduto()));
        ConfiguracaoPrecificacaoResponseDTO configuracao = configuracaoService.buscar();

        for (Produto produto : produtos) {
            List<ProdutoInsumo> ficha = fichas.getOrDefault(produto.getIdProduto(), List.of());
            resultado.put(produto.getIdProduto(), calcularProduto(produto, ficha, configuracao));
        }
        return resultado;
    }

    public PrecificacaoProduto calcular(Produto produto) {
        return calcular(List.of(produto)).get(produto.getIdProduto());
    }

    private PrecificacaoProduto calcularProduto(Produto produto, List<ProdutoInsumo> ficha,
                                                ConfiguracaoPrecificacaoResponseDTO configuracao) {
        BigDecimal margem = produto.getMargemLucro() != null ? produto.getMargemLucro() : configuracao.margemPadrao();
        BigDecimal custoInsumos = somarCustoDosInsumos(ficha);
        if (custoInsumos == null) {
            return PrecificacaoProduto.indisponivel(margem);
        }

        BigDecimal fatorIndiretos = BigDecimal.ONE.add(configuracao.percentualIndiretos().movePointLeft(2));
        BigDecimal custoEstimado = custoInsumos.multiply(fatorIndiretos).setScale(2, RoundingMode.HALF_UP);
        BigDecimal precoBase = custoEstimado.multiply(BigDecimal.ONE.add(margem.movePointLeft(2)));
        return new PrecificacaoProduto(custoEstimado, arredondarParaCima50Centavos(precoBase), margem);
    }

    private BigDecimal somarCustoDosInsumos(List<ProdutoInsumo> ficha) {
        if (ficha.isEmpty()) {
            return null;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (ProdutoInsumo linha : ficha) {
            Insumo insumo = linha.getInsumo();
            boolean semCusto = insumo.getCustoUnitario() == null;
            if (semCusto || !conversaoUnidadeService.saoCompativeis(linha.getUnidade(), insumo.getUnidade())) {
                return null;
            }
            BigDecimal quantidade = conversaoUnidadeService.converter(
                    linha.getQuantidadeUtilizada(), linha.getUnidade(), insumo.getUnidade());
            total = total.add(quantidade.multiply(insumo.getCustoUnitario()));
        }
        return total;
    }

    private BigDecimal arredondarParaCima50Centavos(BigDecimal valor) {
        return valor.multiply(DOIS).setScale(0, RoundingMode.CEILING).divide(DOIS, 2, RoundingMode.UNNECESSARY);
    }
}
