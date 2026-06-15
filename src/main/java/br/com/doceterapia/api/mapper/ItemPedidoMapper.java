package br.com.doceterapia.api.mapper;

import br.com.doceterapia.api.dto.ItemPedidoRequestDTO;
import br.com.doceterapia.api.dto.ItemPedidoResponseDTO;
import br.com.doceterapia.api.entity.ItemPedido;
import br.com.doceterapia.api.entity.Pedido;
import br.com.doceterapia.api.entity.Produto;
import br.com.doceterapia.api.repository.PedidoRepository;
import br.com.doceterapia.api.repository.ProdutoRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ItemPedidoMapper {

    private final ProdutoRepository produtoRepository;
    private final PedidoRepository pedidoRepository;

    public ItemPedidoMapper(ProdutoRepository produtoRepository, PedidoRepository pedidoRepository) {
        this.produtoRepository = produtoRepository;
        this.pedidoRepository = pedidoRepository;
    }

    public ItemPedido toEntity(ItemPedidoRequestDTO dto) {
        Produto produto = produtoRepository.findById(dto.getFkProduto())
                .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + dto.getFkProduto()));
        Pedido pedido = pedidoRepository.findById(dto.getFkPedido())
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado: " + dto.getFkPedido()));

        ItemPedido entity = new ItemPedido();
        entity.setProduto(produto);
        entity.setPedido(pedido);
        entity.setQuantidade(dto.getQuantidade());
        entity.setValorUnitario(dto.getValorUnitario());
        entity.setPrecoOriginalProduto(produto.getPrecoAtual());
        entity.setCustoEstimadoItem(produto.getCustoEstimado());

        BigDecimal valorTotal = dto.getValorUnitario().multiply(dto.getQuantidade());
        entity.setValorTotal(valorTotal);
        entity.setMargemLucroItem(valorTotal.subtract(produto.getCustoEstimado()));

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
