package br.com.doceterapia.api.mapper;

import br.com.doceterapia.api.dto.ItemPedidoRequestDTO;
import br.com.doceterapia.api.dto.ItemPedidoResponseDTO;
import br.com.doceterapia.api.entity.ItemPedido;
import br.com.doceterapia.api.entity.Pedido;
import br.com.doceterapia.api.entity.Produto;
import br.com.doceterapia.api.exception.PedidoIdDontExistsException;
import br.com.doceterapia.api.exception.ProdutoIdDontExistsException;
import br.com.doceterapia.api.repository.PedidoRepository;
import br.com.doceterapia.api.repository.ProdutoRepository;
import br.com.doceterapia.api.service.precificacao.PrecificacaoService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class ItemPedidoMapper {

    private final ProdutoRepository produtoRepository;
    private final PedidoRepository pedidoRepository;
    private final PrecificacaoService precificacaoService;

    public ItemPedidoMapper(ProdutoRepository produtoRepository, PedidoRepository pedidoRepository,
                            PrecificacaoService precificacaoService) {
        this.produtoRepository = produtoRepository;
        this.pedidoRepository = pedidoRepository;
        this.precificacaoService = precificacaoService;
    }

    public ItemPedido toEntity(ItemPedidoRequestDTO dto) {
        Produto produto = produtoRepository.findById(dto.getFkProduto())
                .orElseThrow(ProdutoIdDontExistsException::new);
        Pedido pedido = pedidoRepository.findById(dto.getFkPedido())
                .orElseThrow(PedidoIdDontExistsException::new);

        ItemPedido entity = new ItemPedido();
        entity.setProduto(produto);
        entity.setPedido(pedido);
        entity.setQuantidade(dto.getQuantidade());
        entity.setValorUnitario(dto.getValorUnitario());
        entity.setPrecoOriginalProduto(produto.getPrecoAtual());

        BigDecimal custoEstimado = precificacaoService.calcular(produto).custoEstimado();
        entity.setCustoEstimadoItem(custoEstimado);

        BigDecimal valorTotal = dto.getValorUnitario().multiply(dto.getQuantidade()).setScale(2, RoundingMode.HALF_UP);
        entity.setValorTotal(valorTotal);
        entity.setMargemLucroItem(custoEstimado == null ? null : valorTotal.subtract(custoEstimado));

        entity.setObservacao(dto.getObservacao());
        return entity;
    }

    public ItemPedidoResponseDTO toResponseDTO(ItemPedido entity) {
        ItemPedidoResponseDTO dto = new ItemPedidoResponseDTO();
        dto.setIdProdutoPedido(entity.getIdProdutoPedido());
        dto.setFkProduto(entity.getProduto().getIdProduto());
        dto.setFkPedido(entity.getPedido().getIdPedido());
        dto.setQuantidade(entity.getQuantidade());
        dto.setPrecoOriginalProduto(entity.getPrecoOriginalProduto());
        dto.setValorUnitario(entity.getValorUnitario());
        dto.setValorTotal(entity.getValorTotal());
        dto.setCustoEstimadoItem(entity.getCustoEstimadoItem());
        dto.setMargemLucroItem(entity.getMargemLucroItem());
        dto.setObservacao(entity.getObservacao());
        return dto;
    }
}
