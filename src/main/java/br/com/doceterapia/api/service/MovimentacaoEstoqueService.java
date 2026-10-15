package br.com.doceterapia.api.service;

import br.com.doceterapia.api.entity.Insumo;
import br.com.doceterapia.api.entity.MovimentacaoEstoque;
import br.com.doceterapia.api.enums.OrigemMovimentacao;
import br.com.doceterapia.api.enums.StatusAtivo;
import br.com.doceterapia.api.enums.TipoMovimentacao;
import br.com.doceterapia.api.enums.UnidadeMedida;
import br.com.doceterapia.api.exception.ChaveIdempotenciaConflitanteException;
import br.com.doceterapia.api.exception.InsumoInativoException;
import br.com.doceterapia.api.repository.MovimentacaoEstoqueRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Owns every change to a supply balance. All balance writes go through
 * {@link #aplicarMovimentacao} so the pessimistic lock and future invariants
 * (e.g. the minimum-crossing alert) live in a single place.
 */
@Service
public class MovimentacaoEstoqueService {

    private static final int ESCALA_CUSTO = 4;

    private final MovimentacaoEstoqueRepository repository;
    private final ConversaoUnidadeService conversaoUnidadeService;
    private final EntityManager entityManager;
    private final TransactionTemplate transacao;
    private final TransactionTemplate transacaoNova;

    public MovimentacaoEstoqueService(MovimentacaoEstoqueRepository repository,
                                      ConversaoUnidadeService conversaoUnidadeService,
                                      EntityManager entityManager,
                                      PlatformTransactionManager transactionManager) {
        this.repository = repository;
        this.conversaoUnidadeService = conversaoUnidadeService;
        this.entityManager = entityManager;
        this.transacao = new TransactionTemplate(transactionManager);
        this.transacaoNova = new TransactionTemplate(transactionManager);
        this.transacaoNova.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    /**
     * Idempotent purchase entry. A repeated key with the same content returns
     * the original movement flagged as duplicate; a repeated key with different
     * content is a conflict. Concurrent requests with the same key race on the
     * unique index: the loser catches the constraint violation and resolves it
     * in a fresh transaction, because the failed insert left the original one
     * marked rollback-only.
     */
    public ResultadoMovimentacao registrarEntradaCompra(EntradaCompra comando) {
        String hash = calcularHash(comando);

        try {
            return transacao.execute(status -> inserirEntrada(comando, hash));
        } catch (DataIntegrityViolationException e) {
            return transacaoNova.execute(status -> resolverAposCorrida(comando, hash));
        }
    }

    private ResultadoMovimentacao inserirEntrada(EntradaCompra comando, String hash) {
        Optional<MovimentacaoEstoque> existente = repository.findByChaveIdempotencia(comando.chaveIdempotencia());
        if (existente.isPresent()) {
            return resolverDuplicada(existente.get(), hash);
        }

        Insumo insumo = carregarInsumoAtivoComLock(comando.insumoId());
        BigDecimal convertida = conversaoUnidadeService.converter(
                comando.quantidadeInformada(), comando.unidadeInformada(), insumo.getUnidade());
        BigDecimal custo = calcularCustoUnitario(comando.valorLiquido(), convertida);

        MovimentacaoEstoque movimentacao = montar(insumo, TipoMovimentacao.ENTRADA_COMPRA, convertida,
                comando.quantidadeInformada(), comando.unidadeInformada(), custo, comando.origem(),
                comando.chaveNota(), comando.fluxoId(), comando.chaveIdempotencia(), hash,
                comando.ocorridoEm() != null ? comando.ocorridoEm() : LocalDateTime.now());

        aplicarMovimentacao(insumo, movimentacao, custo);

        return new ResultadoMovimentacao(movimentacao, false);
    }

    private ResultadoMovimentacao resolverAposCorrida(EntradaCompra comando, String hash) {
        MovimentacaoEstoque existente = repository.findByChaveIdempotencia(comando.chaveIdempotencia())
                .orElseThrow(() -> new IllegalStateException(
                        "Chave de idempotência violou a restrição única mas não foi encontrada"));
        return resolverDuplicada(existente, hash);
    }

    /**
     * Initial balance movement created when a supply is created with a
     * non-zero quantity (origin MANUAL).
     */
    @Transactional
    public MovimentacaoEstoque registrarSaldoInicial(Insumo insumo, BigDecimal quantidade) {
        MovimentacaoEstoque movimentacao = montar(insumo, TipoMovimentacao.SALDO_INICIAL, quantidade,
                quantidade, insumo.getUnidade(), null, OrigemMovimentacao.MANUAL,
                null, null, null, calcularHashManual(insumo, TipoMovimentacao.SALDO_INICIAL, quantidade),
                LocalDateTime.now());
        aplicarMovimentacao(insumo, movimentacao, null);
        return movimentacao;
    }

    /**
     * Manual adjustment movement created when a supply balance is changed
     * through PUT /insumos (origin MANUAL, signed difference).
     */
    @Transactional
    public MovimentacaoEstoque registrarAjuste(Insumo insumo, BigDecimal diferenca) {
        MovimentacaoEstoque movimentacao = montar(insumo, TipoMovimentacao.AJUSTE, diferenca,
                diferenca, insumo.getUnidade(), null, OrigemMovimentacao.MANUAL,
                null, null, null, calcularHashManual(insumo, TipoMovimentacao.AJUSTE, diferenca),
                LocalDateTime.now());
        aplicarMovimentacao(insumo, movimentacao, null);
        return movimentacao;
    }

    /**
     * Adjusts a supply to a target balance. The signed difference is computed
     * from the balance read AFTER the row lock is taken, so concurrent targets
     * never apply a stale delta. Returns the recorded movement, or empty when
     * the target already matches the current balance.
     */
    @Transactional
    public Optional<MovimentacaoEstoque> registrarAjusteParaAlvo(Insumo insumo, BigDecimal quantidadeAlvo) {
        entityManager.lock(insumo, LockModeType.PESSIMISTIC_WRITE);
        entityManager.refresh(insumo);

        BigDecimal saldoAtual = insumo.getQuantidadeAtual() != null ? insumo.getQuantidadeAtual() : BigDecimal.ZERO;
        BigDecimal diferenca = quantidadeAlvo.subtract(saldoAtual);
        if (diferenca.signum() == 0) {
            return Optional.empty();
        }
        return Optional.of(registrarAjuste(insumo, diferenca));
    }

    /**
     * The single place that mutates a supply balance. Holds a pessimistic
     * write lock on the supply row so concurrent movements never lose updates.
     */
    private void aplicarMovimentacao(Insumo insumo, MovimentacaoEstoque movimentacao, BigDecimal custoNovo) {
        entityManager.lock(insumo, LockModeType.PESSIMISTIC_WRITE);

        BigDecimal saldoAtual = insumo.getQuantidadeAtual() != null ? insumo.getQuantidadeAtual() : BigDecimal.ZERO;
        insumo.setQuantidadeAtual(saldoAtual.add(movimentacao.getQuantidade()));

        if (custoNovo != null) {
            insumo.setCustoUnitario(custoNovo);
        }

        repository.saveAndFlush(movimentacao);
    }

    private Insumo carregarInsumoAtivoComLock(Integer insumoId) {
        Insumo insumo = entityManager.find(Insumo.class, insumoId);
        if (insumo == null) {
            throw new InsumoInativoException();
        }
        // Lock then refresh so the row lock is held with SELECT ... FOR UPDATE
        // semantics without the loader's follow-on locking, and the balance is
        // re-read from the committed state under the lock.
        entityManager.lock(insumo, LockModeType.PESSIMISTIC_WRITE);
        entityManager.refresh(insumo);
        if (insumo.getStatus() == StatusAtivo.INATIVO) {
            throw new InsumoInativoException();
        }
        return insumo;
    }

    private ResultadoMovimentacao resolverDuplicada(MovimentacaoEstoque existente, String hash) {
        if (!existente.getHashConteudo().equals(hash)) {
            throw new ChaveIdempotenciaConflitanteException();
        }
        return new ResultadoMovimentacao(existente, true);
    }

    private BigDecimal calcularCustoUnitario(BigDecimal valorLiquido, BigDecimal quantidadeConvertida) {
        if (valorLiquido == null) {
            return null;
        }
        return valorLiquido.divide(quantidadeConvertida, ESCALA_CUSTO, RoundingMode.HALF_EVEN);
    }

    private MovimentacaoEstoque montar(Insumo insumo, TipoMovimentacao tipo, BigDecimal quantidade,
                                       BigDecimal quantidadeInformada, UnidadeMedida unidadeInformada,
                                       BigDecimal custoUnitario, OrigemMovimentacao origem, String chaveNota,
                                       String fluxoId, String chaveIdempotencia, String hash,
                                       LocalDateTime ocorridoEm) {
        MovimentacaoEstoque movimentacao = new MovimentacaoEstoque();
        movimentacao.setInsumo(insumo);
        movimentacao.setTipo(tipo);
        movimentacao.setQuantidade(quantidade);
        movimentacao.setQuantidadeInformada(quantidadeInformada);
        movimentacao.setUnidadeInformada(unidadeInformada);
        movimentacao.setCustoUnitario(custoUnitario);
        movimentacao.setOrigem(origem);
        movimentacao.setChaveNota(chaveNota);
        movimentacao.setFluxoId(fluxoId);
        movimentacao.setChaveIdempotencia(chaveIdempotencia);
        movimentacao.setHashConteudo(hash);
        movimentacao.setOcorridoEm(ocorridoEm);
        movimentacao.setCriadoEm(LocalDateTime.now());
        return movimentacao;
    }

    private String calcularHash(EntradaCompra comando) {
        String canonico = String.join("|",
                String.valueOf(comando.insumoId()),
                normalizar(comando.quantidadeInformada()),
                String.valueOf(comando.unidadeInformada()),
                comando.valorLiquido() != null ? normalizar(comando.valorLiquido()) : "",
                comando.chaveNota() != null ? comando.chaveNota() : "",
                comando.fluxoId() != null ? comando.fluxoId() : "");
        return sha256(canonico);
    }

    private String calcularHashManual(Insumo insumo, TipoMovimentacao tipo, BigDecimal quantidade) {
        String canonico = String.join("|",
                String.valueOf(insumo.getIdInsumo()),
                String.valueOf(tipo),
                normalizar(quantidade));
        return sha256(canonico);
    }

    private String normalizar(BigDecimal valor) {
        return valor.stripTrailingZeros().toPlainString();
    }

    private String sha256(String conteudo) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(conteudo.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                hex.append(Character.forDigit((b >> 4) & 0xF, 16));
                hex.append(Character.forDigit(b & 0xF, 16));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível na JVM", e);
        }
    }

    public record EntradaCompra(
            Integer insumoId,
            BigDecimal quantidadeInformada,
            UnidadeMedida unidadeInformada,
            BigDecimal valorLiquido,
            OrigemMovimentacao origem,
            String chaveNota,
            String fluxoId,
            String chaveIdempotencia,
            LocalDateTime ocorridoEm
    ) {
    }

    public record ResultadoMovimentacao(MovimentacaoEstoque movimentacao, boolean duplicado) {
    }
}
