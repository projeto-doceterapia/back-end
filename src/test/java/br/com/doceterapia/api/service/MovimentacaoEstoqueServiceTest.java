package br.com.doceterapia.api.service;

import br.com.doceterapia.api.entity.Insumo;
import br.com.doceterapia.api.entity.MovimentacaoEstoque;
import br.com.doceterapia.api.enums.OrigemMovimentacao;
import br.com.doceterapia.api.enums.StatusAtivo;
import br.com.doceterapia.api.enums.TipoMovimentacao;
import br.com.doceterapia.api.enums.UnidadeMedida;
import br.com.doceterapia.api.exception.ChaveIdempotenciaConflitanteException;
import br.com.doceterapia.api.exception.InsumoInativoException;
import br.com.doceterapia.api.exception.UnidadeIncompativelException;
import br.com.doceterapia.api.repository.MovimentacaoEstoqueRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para MovimentacaoEstoqueService")
class MovimentacaoEstoqueServiceTest {

    @Mock
    private MovimentacaoEstoqueRepository repository;

    @Mock
    private EntityManager entityManager;

    private final ConversaoUnidadeService conversaoUnidadeService = new ConversaoUnidadeService();
    private final ChaveNotaFiscalValidator chaveNotaFiscalValidator = new ChaveNotaFiscalValidator();

    // Runs each TransactionTemplate callback inline without a real transaction;
    // exceptions still propagate so rollback-based behavior is preserved.
    private final PlatformTransactionManager transactionManager = new PlatformTransactionManager() {
        @Override
        public TransactionStatus getTransaction(TransactionDefinition definition) {
            return new SimpleTransactionStatus();
        }

        @Override
        public void commit(TransactionStatus status) {
        }

        @Override
        public void rollback(TransactionStatus status) {
        }
    };

    private MovimentacaoEstoqueService service;

    private Insumo insumoKg;

    @BeforeEach
    void setUp() {
        service = new MovimentacaoEstoqueService(repository, conversaoUnidadeService, chaveNotaFiscalValidator,
                entityManager, transactionManager);

        insumoKg = new Insumo();
        insumoKg.setIdInsumo(1);
        insumoKg.setUnidade(UnidadeMedida.KG);
        insumoKg.setStatus(StatusAtivo.ATIVO);
        insumoKg.setQuantidadeAtual(BigDecimal.ZERO);
    }

    private MovimentacaoEstoqueService.EntradaCompra entrada(BigDecimal quantidade, UnidadeMedida unidade,
                                                             BigDecimal valorLiquido, String chave) {
        return new MovimentacaoEstoqueService.EntradaCompra(
                1, quantidade, unidade, valorLiquido, OrigemMovimentacao.NOTA_IMAGEM,
                null, null, chave, null);
    }

    @Test
    @DisplayName("Deve registrar entrada de compra atualizando o saldo sob lock")
    void registraEntradaAtualizandoSaldo() {
        insumoKg.setQuantidadeAtual(BigDecimal.valueOf(2));
        when(repository.findByChaveIdempotencia("k1")).thenReturn(Optional.empty());
        when(entityManager.find(Insumo.class, 1)).thenReturn(insumoKg);

        MovimentacaoEstoqueService.ResultadoMovimentacao resultado =
                service.registrarEntradaCompra(entrada(BigDecimal.valueOf(3), UnidadeMedida.KG, null, "k1"));

        assertFalse(resultado.duplicado());
        assertEquals(0, BigDecimal.valueOf(5).compareTo(insumoKg.getQuantidadeAtual()));
        assertEquals(TipoMovimentacao.ENTRADA_COMPRA, resultado.movimentacao().getTipo());
        verify(entityManager, times(2)).lock(insumoKg, LockModeType.PESSIMISTIC_WRITE);
        verify(repository).saveAndFlush(any(MovimentacaoEstoque.class));
    }
    @Test
    @DisplayName("Deve definir custo unitário a partir do valor líquido (2 KG por 15.00 = 7.5000)")
    void custoDeItemPrecificado() {
        when(repository.findByChaveIdempotencia("k1")).thenReturn(Optional.empty());
        when(entityManager.find(Insumo.class, 1)).thenReturn(insumoKg);

        service.registrarEntradaCompra(
                entrada(BigDecimal.valueOf(2), UnidadeMedida.KG, new BigDecimal("15.00"), "k1"));

        assertEquals(new BigDecimal("7.5000"), insumoKg.getCustoUnitario());
    }

    @Test
    @DisplayName("Deve calcular custo por unidade do insumo convertendo 500 G com valor 4.00 (= 8.0000)")
    void custoComConversao() {
        when(repository.findByChaveIdempotencia("k1")).thenReturn(Optional.empty());
        when(entityManager.find(Insumo.class, 1)).thenReturn(insumoKg);

        service.registrarEntradaCompra(
                entrada(BigDecimal.valueOf(500), UnidadeMedida.G, new BigDecimal("4.00"), "k1"));

        assertEquals(new BigDecimal("8.0000"), insumoKg.getCustoUnitario());
        assertEquals(0, new BigDecimal("0.5").compareTo(insumoKg.getQuantidadeAtual()));
    }

    @Test
    @DisplayName("Deve arredondar o custo com HALF_EVEN na escala 4")
    void custoComArredondamento() {
        when(repository.findByChaveIdempotencia("k1")).thenReturn(Optional.empty());
        when(entityManager.find(Insumo.class, 1)).thenReturn(insumoKg);

        // 10.00 / 3 = 3.33333... -> HALF_EVEN escala 4 = 3.3333
        service.registrarEntradaCompra(
                entrada(BigDecimal.valueOf(3), UnidadeMedida.KG, new BigDecimal("10.00"), "k1"));

        assertEquals(new BigDecimal("3.3333"), insumoKg.getCustoUnitario());
    }

    @Test
    @DisplayName("Item sem preço não altera o custo do insumo")
    void itemSemPreco() {
        insumoKg.setCustoUnitario(new BigDecimal("9.0000"));
        when(repository.findByChaveIdempotencia("k1")).thenReturn(Optional.empty());
        when(entityManager.find(Insumo.class, 1)).thenReturn(insumoKg);

        MovimentacaoEstoqueService.ResultadoMovimentacao resultado =
                service.registrarEntradaCompra(entrada(BigDecimal.valueOf(3), UnidadeMedida.KG, null, "k1"));

        assertEquals(new BigDecimal("9.0000"), insumoKg.getCustoUnitario());
        assertNull(resultado.movimentacao().getCustoUnitario());
    }

    @Test
    @DisplayName("Mesma chave e mesmo conteúdo retorna duplicado sem novo efeito")
    void duplicadaMesmoConteudo() {
        MovimentacaoEstoque original = new MovimentacaoEstoque();
        original.setChaveIdempotencia("k1");
        when(repository.findByChaveIdempotencia("k1"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(original));
        when(entityManager.find(Insumo.class, 1)).thenReturn(insumoKg);

        MovimentacaoEstoqueService.EntradaCompra comando =
                entrada(BigDecimal.valueOf(3), UnidadeMedida.KG, null, "k1");
        MovimentacaoEstoque criada = service.registrarEntradaCompra(comando).movimentacao();
        original.setHashConteudo(criada.getHashConteudo());

        MovimentacaoEstoqueService.ResultadoMovimentacao segunda = service.registrarEntradaCompra(comando);

        assertTrue(segunda.duplicado());
    }

    @Test
    @DisplayName("Mesma chave com conteúdo diferente lança conflito")
    void conflitoConteudoDiferente() {
        MovimentacaoEstoque existente = new MovimentacaoEstoque();
        existente.setChaveIdempotencia("k1");
        existente.setHashConteudo("hash-antigo");
        when(repository.findByChaveIdempotencia("k1")).thenReturn(Optional.of(existente));

        assertThrows(ChaveIdempotenciaConflitanteException.class,
                () -> service.registrarEntradaCompra(entrada(BigDecimal.valueOf(12), UnidadeMedida.KG, null, "k1")));

        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Insumo inexistente ou inativo lança 404")
    void insumoInativo() {
        insumoKg.setStatus(StatusAtivo.INATIVO);
        when(repository.findByChaveIdempotencia("k1")).thenReturn(Optional.empty());
        when(entityManager.find(Insumo.class, 1)).thenReturn(insumoKg);

        assertThrows(InsumoInativoException.class,
                () -> service.registrarEntradaCompra(entrada(BigDecimal.valueOf(1), UnidadeMedida.KG, null, "k1")));
    }

    @Test
    @DisplayName("Unidade incompatível lança 422")
    void unidadeIncompativel() {
        when(repository.findByChaveIdempotencia("k1")).thenReturn(Optional.empty());
        when(entityManager.find(Insumo.class, 1)).thenReturn(insumoKg);

        assertThrows(UnidadeIncompativelException.class,
                () -> service.registrarEntradaCompra(entrada(BigDecimal.valueOf(1), UnidadeMedida.L, null, "k1")));
    }

    @Test
    @DisplayName("Saldo inicial cria movimentação SALDO_INICIAL e soma ao saldo")
    void saldoInicial() {
        insumoKg.setQuantidadeAtual(BigDecimal.ZERO);

        MovimentacaoEstoque movimentacao = service.registrarSaldoInicial(insumoKg, BigDecimal.valueOf(4));

        assertEquals(TipoMovimentacao.SALDO_INICIAL, movimentacao.getTipo());
        assertEquals(OrigemMovimentacao.MANUAL, movimentacao.getOrigem());
        assertEquals(0, BigDecimal.valueOf(4).compareTo(insumoKg.getQuantidadeAtual()));
        verify(entityManager).lock(insumoKg, LockModeType.PESSIMISTIC_WRITE);
    }

    @Test
    @DisplayName("Ajuste cria movimentação AJUSTE com diferença assinada")
    void ajuste() {
        insumoKg.setQuantidadeAtual(BigDecimal.valueOf(10));

        MovimentacaoEstoque movimentacao = service.registrarAjuste(insumoKg, BigDecimal.valueOf(-3));

        assertEquals(TipoMovimentacao.AJUSTE, movimentacao.getTipo());
        assertEquals(0, BigDecimal.valueOf(-3).compareTo(movimentacao.getQuantidade()));
        assertEquals(0, BigDecimal.valueOf(7).compareTo(insumoKg.getQuantidadeAtual()));
    }
}
