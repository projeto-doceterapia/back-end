package br.com.doceterapia.api.repository;

import br.com.doceterapia.api.dto.ClienteMetricasDTO;
import br.com.doceterapia.api.entity.CategoriaProduto;
import br.com.doceterapia.api.entity.Cliente;
import br.com.doceterapia.api.entity.ItemPedido;
import br.com.doceterapia.api.entity.Pedido;
import br.com.doceterapia.api.entity.Produto;
import br.com.doceterapia.api.enums.ClassificacaoCliente;
import br.com.doceterapia.api.enums.StatusAtivo;
import br.com.doceterapia.api.enums.StatusPedido;
import br.com.doceterapia.api.enums.TipoPessoa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@DisplayName("Testes de persistência para ClienteRepository")
class ClienteRepositoryTest {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ItemPedidoRepository itemPedidoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private CategoriaProdutoRepository categoriaProdutoRepository;

    private Cliente exemplo;
    private Cliente outra;
    private Produto produto;

    private Cliente novoCliente(String nome, String telefone) {
        Cliente cliente = new Cliente();
        cliente.setNome(nome);
        cliente.setTelefone(telefone);
        cliente.setEndereco("Praça da Sé, 100, Sé, São Paulo - SP");
        cliente.setTipoPessoa(TipoPessoa.FISICA);
        cliente.setClassificacaoCliente(ClassificacaoCliente.PADRAO);
        cliente.setStatus(StatusAtivo.ATIVO);
        return clienteRepository.save(cliente);
    }

    private void novoPedido(Cliente cliente, StatusPedido status, LocalDate entrega, String... valoresItens) {
        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setStatusPedido(status);
        pedido.setDataEntrega(entrega);
        pedidoRepository.save(pedido);

        for (String valor : valoresItens) {
            ItemPedido item = new ItemPedido();
            item.setPedido(pedido);
            item.setProduto(produto);
            item.setValorTotal(new BigDecimal(valor));
            itemPedidoRepository.save(item);
        }
    }

    @BeforeEach
    void setUp() {
        exemplo = novoCliente("Cliente Exemplo", "+55 11 90000-0001");
        outra = novoCliente("Outra Pessoa", "(11) 90000-0002");

        CategoriaProduto categoria = new CategoriaProduto();
        categoria.setNome("Bolos");
        categoriaProdutoRepository.save(categoria);
        produto = new Produto();
        produto.setCategoriaProduto(categoria);
        produto.setNome("Bolo de pote");
        produtoRepository.save(produto);
    }

    @Test
    @DisplayName("A busca por nome ignora maiúsculas e aceita parte do nome")
    void buscaPorNomeIgnoraMaiusculas() {
        List<Cliente> resultado = clienteRepository.buscar("EXEM", "");

        assertEquals(1, resultado.size());
        assertEquals("Cliente Exemplo", resultado.get(0).getNome());
    }

    @Test
    @DisplayName("A busca por dígitos acha o telefone com ou sem máscara")
    void buscaPorDigitosDoTelefone() {
        assertEquals("Cliente Exemplo", clienteRepository.buscar("90000-0001", "900000001").get(0).getNome());
        assertEquals("Outra Pessoa", clienteRepository.buscar("900000002", "900000002").get(0).getNome());
    }

    @Test
    @DisplayName("Texto sem dígitos não casa com o telefone")
    void textoSemDigitosNaoCasaPorTelefone() {
        assertTrue(clienteRepository.buscar("ana", "").isEmpty());
    }

    @Test
    @DisplayName("O resultado da busca vem ordenado pelo nome")
    void buscaOrdenaPeloNome() {
        List<Cliente> resultado = clienteRepository.buscar("e", "");

        assertEquals(List.of("Cliente Exemplo", "Outra Pessoa"),
                resultado.stream().map(Cliente::getNome).toList());
    }

    @Test
    @DisplayName("O CEP de 8 dígitos é gravado e lido")
    void gravaCepComOitoDigitos() {
        exemplo.setCep("01001000");
        clienteRepository.saveAndFlush(exemplo);

        assertEquals("01001000", clienteRepository.findById(exemplo.getIdCliente()).orElseThrow().getCep());
    }

    @Test
    @DisplayName("O resumo soma itens, conta pedidos e ignora cancelado e orçamento")
    void resumoAgregaPedidosSemCanceladoNemOrcamento() {
        novoPedido(exemplo, StatusPedido.ENTREGUE, LocalDate.of(2026, 10, 1), "60.00", "40.50");
        novoPedido(exemplo, StatusPedido.AGUARDANDO_SINAL, LocalDate.of(2026, 10, 20), "10.00");
        novoPedido(exemplo, StatusPedido.CANCELADO, LocalDate.of(2026, 10, 28), "500.00");
        novoPedido(exemplo, StatusPedido.ORCAMENTO, LocalDate.of(2026, 11, 5), "300.00");

        List<ClienteMetricasDTO> resumo = pedidoRepository.resumirPorCliente();

        assertEquals(1, resumo.size());
        ClienteMetricasDTO metricas = resumo.get(0);
        assertEquals(exemplo.getIdCliente(), metricas.idCliente());
        assertEquals(2, metricas.totalPedidos());
        assertEquals(0, new BigDecimal("110.50").compareTo(metricas.gastoTotal()));
        assertEquals(LocalDate.of(2026, 10, 20), metricas.ultimoPedido());
    }

    @Test
    @DisplayName("Pedido sem itens conta no total e não soma valor")
    void resumoPedidoSemItens() {
        novoPedido(outra, StatusPedido.ENTREGUE, LocalDate.of(2026, 10, 1));

        ClienteMetricasDTO metricas = pedidoRepository.resumirPorCliente().get(0);

        assertEquals(1, metricas.totalPedidos());
        assertEquals(0, BigDecimal.ZERO.compareTo(metricas.gastoTotal()));
    }

    @Test
    @DisplayName("Cliente sem pedido não aparece nas métricas")
    void resumoClienteSemPedido() {
        assertTrue(pedidoRepository.resumirPorCliente().isEmpty());
        assertNull(clienteRepository.findById(exemplo.getIdCliente()).orElseThrow().getCep());
    }
}
