package br.com.doceterapia.api.service;

import br.com.doceterapia.api.config.CatalogoProperties;
import br.com.doceterapia.api.config.InsumoDefaultsProperties;
import br.com.doceterapia.api.dto.InsumoCatalogoItemDTO;
import br.com.doceterapia.api.dto.InsumoCatalogoResponseDTO;
import br.com.doceterapia.api.dto.ObterOuCriarInsumoRequestDTO;
import br.com.doceterapia.api.entity.CategoriaInsumo;
import br.com.doceterapia.api.entity.Insumo;
import br.com.doceterapia.api.enums.StatusAtivo;
import br.com.doceterapia.api.exception.InsumoNomeInvalidoException;
import br.com.doceterapia.api.mapper.InsumoMapper;
import br.com.doceterapia.api.repository.CategoriaInsumoRepository;
import br.com.doceterapia.api.repository.InsumoCatalogoSpecs;
import br.com.doceterapia.api.repository.InsumoRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Supply catalog for the assistant and the idempotent get-or-create entry point.
 * Kept apart from {@link InsumoService} so the catalog work does not
 * collide with the stock-ledger changes to that class.
 */
@Service
public class InsumoCatalogoService {

    private static final int NOME_TAMANHO_MAXIMO = 45;

    private final InsumoRepository repository;
    private final CategoriaInsumoRepository categoriaInsumoRepository;
    private final InsumoMapper insumoMapper;
    private final NomeNormalizador nomeNormalizador;
    private final CatalogoCursorCodec cursorCodec;
    private final CatalogoProperties catalogoProperties;
    private final InsumoDefaultsProperties defaultsProperties;

    public InsumoCatalogoService(InsumoRepository repository,
                                 CategoriaInsumoRepository categoriaInsumoRepository,
                                 InsumoMapper insumoMapper,
                                 NomeNormalizador nomeNormalizador,
                                 CatalogoCursorCodec cursorCodec,
                                 CatalogoProperties catalogoProperties,
                                 InsumoDefaultsProperties defaultsProperties) {
        this.repository = repository;
        this.categoriaInsumoRepository = categoriaInsumoRepository;
        this.insumoMapper = insumoMapper;
        this.nomeNormalizador = nomeNormalizador;
        this.cursorCodec = cursorCodec;
        this.catalogoProperties = catalogoProperties;
        this.defaultsProperties = defaultsProperties;
    }

    @Transactional(readOnly = true)
    public InsumoCatalogoResponseDTO listarCatalogo(String q, Integer categoria, Integer limite, String cursor) {
        int limiteEfetivo = resolverLimite(limite);
        CatalogoCursorCodec.Cursor cursorDecodificado = cursorCodec.decodificar(cursor);

        Specification<Insumo> filtros = montarFiltros(q, categoria);
        long total = repository.count(filtros);

        Specification<Insumo> pagina = filtros;
        if (cursorDecodificado != null) {
            pagina = pagina.and(InsumoCatalogoSpecs.depoisDoCursor(cursorDecodificado));
        }

        Sort ordem = Sort.by("nomeNormalizado").ascending().and(Sort.by("idInsumo").ascending());
        List<Insumo> encontrados = repository.findAll(pagina, PageRequest.of(0, limiteEfetivo + 1, ordem)).getContent();

        boolean temProxima = encontrados.size() > limiteEfetivo;
        List<Insumo> itensPagina = temProxima ? encontrados.subList(0, limiteEfetivo) : encontrados;

        List<InsumoCatalogoItemDTO> itens = new ArrayList<>(itensPagina.size());
        for (Insumo insumo : itensPagina) {
            itens.add(insumoMapper.toCatalogoItemDTO(insumo));
        }

        String proximoCursor = null;
        if (temProxima) {
            Insumo ultimo = itensPagina.get(itensPagina.size() - 1);
            proximoCursor = cursorCodec.codificar(ultimo.getNomeNormalizado(), ultimo.getIdInsumo());
        }

        return new InsumoCatalogoResponseDTO(itens, total, proximoCursor);
    }

    @Transactional
    public ObterOuCriarResultado obterOuCriar(ObterOuCriarInsumoRequestDTO request) {
        String nome = request.getNome() == null ? null : request.getNome().trim();
        validarNome(nome);

        String nomeNormalizado = nomeNormalizador.normalizar(nome);

        return repository
                .findByNomeNormalizadoAndUnidadeAndStatus(nomeNormalizado, request.getUnidade(), StatusAtivo.ATIVO)
                .map(existente -> new ObterOuCriarResultado(existente, false))
                .orElseGet(() -> new ObterOuCriarResultado(criar(request, nome, nomeNormalizado), true));
    }

    private Insumo criar(ObterOuCriarInsumoRequestDTO request, String nome, String nomeNormalizado) {
        Insumo entity = new Insumo();
        entity.setNome(nome);
        entity.setNomeNormalizado(nomeNormalizado);
        entity.setUnidade(request.getUnidade());
        entity.setStatus(StatusAtivo.ATIVO);
        entity.setQuantidadeAtual(BigDecimal.ZERO);
        entity.setCategoria(resolverCategoria(request.getFkCategoriaInsumo()));
        return repository.save(entity);
    }

    private CategoriaInsumo resolverCategoria(Integer categoriaId) {
        if (categoriaId != null) {
            CategoriaInsumo categoria = categoriaInsumoRepository.findById(categoriaId).orElse(null);
            if (categoria != null) {
                return categoria;
            }
        }
        return categoriaPadrao();
    }

    private CategoriaInsumo categoriaPadrao() {
        String nomePadrao = defaultsProperties.getCategoriaPadrao();
        return categoriaInsumoRepository.findAll().stream()
                .filter(categoria -> nomePadrao.equals(categoria.getNome()))
                .findFirst()
                .orElseGet(() -> {
                    CategoriaInsumo nova = new CategoriaInsumo();
                    nova.setNome(nomePadrao);
                    return categoriaInsumoRepository.save(nova);
                });
    }

    private void validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new InsumoNomeInvalidoException("Nome é obrigatório");
        }
        if (nome.length() > NOME_TAMANHO_MAXIMO) {
            throw new InsumoNomeInvalidoException("Nome não pode exceder 45 caracteres");
        }
    }

    private int resolverLimite(Integer limite) {
        int maximo = catalogoProperties.getLimiteMaximo();
        if (limite == null || limite <= 0 || limite > maximo) {
            return maximo;
        }
        return limite;
    }

    private Specification<Insumo> montarFiltros(String q, Integer categoria) {
        Specification<Insumo> filtros = InsumoCatalogoSpecs.ativos();

        if (q != null && !q.isBlank()) {
            String normalizado = nomeNormalizador.normalizar(q);
            if (!normalizado.isBlank()) {
                List<String> palavras = List.of(normalizado.split(" "));
                filtros = filtros.and(InsumoCatalogoSpecs.nomeContemPalavras(palavras));
            }
        }

        if (categoria != null) {
            filtros = filtros.and(InsumoCatalogoSpecs.daCategoria(categoria));
        }

        return filtros;
    }

    public record ObterOuCriarResultado(Insumo insumo, boolean criado) {
    }
}
