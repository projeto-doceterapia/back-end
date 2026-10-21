package br.com.doceterapia.api.service;

import br.com.doceterapia.api.config.CatalogoProperties;
import br.com.doceterapia.api.config.InsumoDefaultsProperties;
import br.com.doceterapia.api.dto.InsumoCatalogoResponseDTO;
import br.com.doceterapia.api.entity.CategoriaInsumo;
import br.com.doceterapia.api.entity.Insumo;
import br.com.doceterapia.api.enums.StatusAtivo;
import br.com.doceterapia.api.enums.UnidadeMedida;
import br.com.doceterapia.api.mapper.InsumoMapper;
import br.com.doceterapia.api.repository.CategoriaInsumoRepository;
import br.com.doceterapia.api.repository.InsumoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DisplayName("Testes de catálogo de insumos (@DataJpaTest)")
class InsumoCatalogoServiceDataJpaTest {

    @Autowired
    private InsumoRepository insumoRepository;

    @Autowired
    private CategoriaInsumoRepository categoriaInsumoRepository;

    private InsumoCatalogoService service;
    private CategoriaInsumo categoria;

    @BeforeEach
    void setUp() {
        CatalogoProperties catalogoProperties = new CatalogoProperties();
        catalogoProperties.setLimiteMaximo(200);
        InsumoDefaultsProperties defaults = new InsumoDefaultsProperties();
        NomeNormalizador normalizador = new NomeNormalizador();
        InsumoMapper mapper = new InsumoMapper(categoriaInsumoRepository, normalizador);
        service = new InsumoCatalogoService(insumoRepository, categoriaInsumoRepository, mapper,
                normalizador, new CatalogoCursorCodec(), catalogoProperties, defaults);

        categoria = new CategoriaInsumo();
        categoria.setNome("Farinhas");
        categoria = categoriaInsumoRepository.save(categoria);
    }

    private Insumo criar(String nome, StatusAtivo status) {
        Insumo insumo = new Insumo();
        insumo.setNome(nome);
        insumo.setNomeNormalizado(new NomeNormalizador().normalizar(nome));
        insumo.setUnidade(UnidadeMedida.KG);
        insumo.setStatus(status);
        insumo.setQuantidadeAtual(BigDecimal.ZERO);
        insumo.setCategoria(categoria);
        return insumoRepository.save(insumo);
    }

    @Test
    @DisplayName("Exclui insumos inativos do catálogo")
    void excluiInativos() {
        criar("Farinha de trigo", StatusAtivo.ATIVO);
        criar("Açúcar refinado", StatusAtivo.INATIVO);

        InsumoCatalogoResponseDTO resposta = service.listarCatalogo(null, null, null, null);

        assertEquals(1, resposta.getTotal());
        assertEquals(1, resposta.getItens().size());
        assertEquals("Farinha de trigo", resposta.getItens().get(0).getNome());
    }

    @Test
    @DisplayName("Catálogo pequeno cabe em uma página sem cursor")
    void catalogoPequenoUmaPagina() {
        for (int i = 0; i < 80; i++) {
            criar(String.format("Insumo %03d", i), StatusAtivo.ATIVO);
        }

        InsumoCatalogoResponseDTO resposta = service.listarCatalogo(null, null, 200, null);

        assertEquals(80, resposta.getTotal());
        assertEquals(80, resposta.getItens().size());
        assertNull(resposta.getProximoCursor());
    }

    @Test
    @DisplayName("Catálogo grande retorna primeira página e cursor")
    void catalogoGrandeComCursor() {
        for (int i = 0; i < 450; i++) {
            criar(String.format("Insumo %03d", i), StatusAtivo.ATIVO);
        }

        InsumoCatalogoResponseDTO primeira = service.listarCatalogo(null, null, 200, null);

        assertEquals(450, primeira.getTotal());
        assertEquals(200, primeira.getItens().size());
        assertNotNull(primeira.getProximoCursor());

        InsumoCatalogoResponseDTO segunda = service.listarCatalogo(null, null, 200, primeira.getProximoCursor());
        assertEquals(450, segunda.getTotal());
        assertEquals(200, segunda.getItens().size());
        assertNotEquals(primeira.getItens().get(0).getIdInsumo(), segunda.getItens().get(0).getIdInsumo());
    }

    @Test
    @DisplayName("Limite acima do máximo é limitado ao máximo")
    void limiteAcimaDoMaximo() {
        for (int i = 0; i < 250; i++) {
            criar(String.format("Insumo %03d", i), StatusAtivo.ATIVO);
        }

        InsumoCatalogoResponseDTO resposta = service.listarCatalogo(null, null, 1000, null);

        assertEquals(200, resposta.getItens().size());
        assertEquals(250, resposta.getTotal());
    }

    @Test
    @DisplayName("Busca ignora acentos")
    void buscaIgnoraAcentos() {
        criar("Açúcar refinado", StatusAtivo.ATIVO);
        criar("Farinha de trigo", StatusAtivo.ATIVO);

        InsumoCatalogoResponseDTO resposta = service.listarCatalogo("acucar", null, null, null);

        assertEquals(1, resposta.getTotal());
        assertEquals("Açúcar refinado", resposta.getItens().get(0).getNome());
    }

    @Test
    @DisplayName("Busca por termo retorna todos os que contêm a palavra")
    void buscaPorTermo() {
        criar("Farinha de trigo", StatusAtivo.ATIVO);
        criar("Farinha de mandioca", StatusAtivo.ATIVO);
        criar("Açúcar refinado", StatusAtivo.ATIVO);

        InsumoCatalogoResponseDTO resposta = service.listarCatalogo("farinha", null, null, null);

        assertEquals(2, resposta.getTotal());
    }

    @Test
    @DisplayName("Busca trata % como texto literal, não como curinga")
    void buscaEscapaPorcentagem() {
        criar("Leite 50% gordura", StatusAtivo.ATIVO);
        criar("Leite 509 gordura", StatusAtivo.ATIVO);
        criar("Creme de leite", StatusAtivo.ATIVO);

        InsumoCatalogoResponseDTO resposta = service.listarCatalogo("50%", null, null, null);

        assertEquals(1, resposta.getTotal());
        assertEquals("Leite 50% gordura", resposta.getItens().get(0).getNome());
    }

    @Test
    @DisplayName("Busca trata _ como texto literal, não como curinga")
    void buscaEscapaSublinhado() {
        criar("Acucar a_granel", StatusAtivo.ATIVO);
        criar("Acucar abgranel", StatusAtivo.ATIVO);

        InsumoCatalogoResponseDTO resposta = service.listarCatalogo("a_granel", null, null, null);

        assertEquals(1, resposta.getTotal());
        assertEquals("Acucar a_granel", resposta.getItens().get(0).getNome());
    }

    @Test
    @DisplayName("Páginas permanecem estáveis após inserção")
    void paginasEstaveisAposInsercao() {
        for (int i = 0; i < 10; i++) {
            criar(String.format("Insumo %03d", i), StatusAtivo.ATIVO);
        }

        InsumoCatalogoResponseDTO primeira = service.listarCatalogo(null, null, 5, null);
        assertEquals(5, primeira.getItens().size());
        assertNotNull(primeira.getProximoCursor());

        // An insert whose name sorts before the cursor must not shift the next page.
        criar("Insumo 000-a", StatusAtivo.ATIVO);

        InsumoCatalogoResponseDTO segunda = service.listarCatalogo(null, null, 5, primeira.getProximoCursor());
        assertEquals("Insumo 005", segunda.getItens().get(0).getNome());
    }
}
