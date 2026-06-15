package br.com.doceterapia.api.service;

import org.springframework.stereotype.Service;
import br.com.doceterapia.api.dto.ItemPedidoRequestDTO;
import br.com.doceterapia.api.entity.ItemPedido;
import br.com.doceterapia.api.exception.ItemPedidoIdDontExistsException;
import br.com.doceterapia.api.exception.ProdutoIdDontExistsException;
import br.com.doceterapia.api.exception.PedidoIdDontExistsException;
import br.com.doceterapia.api.mapper.ItemPedidoMapper;
import br.com.doceterapia.api.repository.ItemPedidoRepository;
import br.com.doceterapia.api.repository.ProdutoRepository;
import br.com.doceterapia.api.repository.PedidoRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ItemPedidoService {

    private final ItemPedidoRepository repository;
    private final ItemPedidoMapper mapper;
    private final ProdutoRepository produtoRepository;
    private final PedidoRepository pedidoRepository;

    public ItemPedidoService(ItemPedidoRepository repository, ItemPedidoMapper mapper,
                             ProdutoRepository produtoRepository, PedidoRepository pedidoRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.produtoRepository = produtoRepository;
        this.pedidoRepository = pedidoRepository;
    }

    public List<ItemPedido> listarTodos() {
        return repository.findAll();
    }

    public ItemPedido cadastrar(ItemPedidoRequestDTO request) {
        if (!produtoRepository.existsById(request.getFkProduto())) {
            throw new ProdutoIdDontExistsException();
        }

        if (!pedidoRepository.existsById(request.getFkPedido())) {
            throw new PedidoIdDontExistsException();
        }

        ItemPedido entity = mapper.toEntity(request);
        repository.save(entity);
        return entity;
    }

    public ItemPedido atualizar(Integer id, ItemPedidoRequestDTO request) {
        if (!repository.existsById(id)) {
            throw new ItemPedidoIdDontExistsException();
        }

        if (!produtoRepository.existsById(request.getFkProduto())) {
            throw new ProdutoIdDontExistsException();
        }

        if (!pedidoRepository.existsById(request.getFkPedido())) {
            throw new PedidoIdDontExistsException();
        }

        ItemPedido entity = repository.findById(id).orElseThrow(ItemPedidoIdDontExistsException::new);
        entity.setQuantidade(request.getQuantidade());
        entity.setValorUnitario(request.getValorUnitario());
        entity.setObservacao(request.getObservacao());

        BigDecimal valorTotal = request.getValorUnitario().multiply(request.getQuantidade());
        entity.setValorTotal(valorTotal);
        entity.setMargemLucroItem(valorTotal.subtract(entity.getCustoEstimadoItem()));

        repository.save(entity);
        return entity;
    }

    public void deletar(Integer id) {
        if (!repository.existsById(id)) {
            throw new ItemPedidoIdDontExistsException();
        }

        repository.deleteById(id);
    }
}
