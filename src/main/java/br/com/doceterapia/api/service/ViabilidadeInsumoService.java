package br.com.doceterapia.api.service;

import br.com.doceterapia.api.dto.FaltaInsumoDTO;
import br.com.doceterapia.api.entity.Insumo;
import br.com.doceterapia.api.entity.ItemPedido;
import br.com.doceterapia.api.entity.Pedido;
import br.com.doceterapia.api.entity.ProdutoInsumo;
import br.com.doceterapia.api.exception.InsumosInsuficientesException;
import br.com.doceterapia.api.repository.ProdutoInsumoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ViabilidadeInsumoService {
    private static final int ESCALA_RESPOSTA = 3;

    private final ProdutoInsumoRepository produtoInsumoRepository;
    private final ConversaoUnidadeService conversaoUnidadeService;
    private final DisponibilidadeInsumoService disponibilidadeInsumoService;

    public ViabilidadeInsumoService(ProdutoInsumoRepository produtoInsumoRepository,
                                    ConversaoUnidadeService conversaoUnidadeService,
                                    DisponibilidadeInsumoService disponibilidadeInsumoService) {
        this.produtoInsumoRepository = produtoInsumoRepository;
        this.conversaoUnidadeService = conversaoUnidadeService;
        this.disponibilidadeInsumoService = disponibilidadeInsumoService;
    }

    public void exigirViabilidade(Pedido pedido, Collection<ItemPedido> itens, boolean confirmarComFalta,
                                  String mensagem) {
        List<ProdutoInsumo> fichas = produtoInsumoRepository.findComInsumoByProdutoIds(itens.stream()
                .map(item -> item.getProduto().getIdProduto()).collect(Collectors.toSet()));
        Map<Integer, Insumo> insumos = fichas.stream()
                .map(ProdutoInsumo::getInsumo)
                .collect(Collectors.toMap(Insumo::getIdInsumo, Function.identity(), (primeiro, segundo) -> primeiro));
        Map<Integer, BigDecimal> necessidades = fichas.stream()
                .collect(Collectors.groupingBy(linha -> linha.getInsumo().getIdInsumo(), Collectors.reducing(BigDecimal.ZERO,
                        linha -> quantidadeConvertida(itens, linha), BigDecimal::add)));
        List<FaltaInsumoDTO> faltas = necessidades.entrySet().stream()
                .map(entry -> falta(insumos.get(entry.getKey()), entry.getValue(), pedido))
                .filter(java.util.Objects::nonNull)
                .sorted(Comparator.comparing(FaltaInsumoDTO::nome))
                .toList();
        if (!faltas.isEmpty() && !confirmarComFalta) {
            throw new InsumosInsuficientesException(mensagem, faltas);
        }
        if (!faltas.isEmpty()) {
            pedido.setConfirmadoComFaltaInsumo(true);
        }
    }

    private BigDecimal quantidadeConvertida(Collection<ItemPedido> itens, ProdutoInsumo linha) {
        return itens.stream().filter(item -> item.getProduto().getIdProduto().equals(linha.getProduto().getIdProduto()))
                .map(item -> conversaoUnidadeService.converter(item.getQuantidade().multiply(linha.getQuantidadeUtilizada()),
                        linha.getUnidade(), linha.getInsumo().getUnidade()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private FaltaInsumoDTO falta(Insumo insumo, BigDecimal necessario, Pedido pedido) {
        BigDecimal disponivel = disponibilidadeInsumoService.disponivel(insumo, pedido);
        if (necessario.compareTo(disponivel) <= 0) return null;
        return new FaltaInsumoDTO(insumo.getIdInsumo(), insumo.getNome(),
                necessario.setScale(ESCALA_RESPOSTA, RoundingMode.UP),
                disponivel.setScale(ESCALA_RESPOSTA, RoundingMode.HALF_EVEN), insumo.getUnidade());
    }
}
