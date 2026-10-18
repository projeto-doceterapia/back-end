package br.com.doceterapia.api.service;

import br.com.doceterapia.api.config.CatalogoProperties;
import br.com.doceterapia.api.config.InsumoDefaultsProperties;
import br.com.doceterapia.api.dto.ObterOuCriarInsumoRequestDTO;
import br.com.doceterapia.api.entity.CategoriaInsumo;
import br.com.doceterapia.api.entity.Insumo;
import br.com.doceterapia.api.enums.StatusAtivo;
import br.com.doceterapia.api.enums.UnidadeMedida;
import br.com.doceterapia.api.exception.InsumoNomeInvalidoException;
import br.com.doceterapia.api.mapper.InsumoMapper;
import br.com.doceterapia.api.repository.CategoriaInsumoRepository;
import br.com.doceterapia.api.repository.InsumoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes de obter-ou-criar insumo")
class InsumoCatalogoServiceObterOuCriarTest {

    private InsumoRepository repository;
    private CategoriaInsumoRepository categoriaInsumoRepository;
    private InsumoCatalogoService service;

    @BeforeEach
    void setUp() {
        repository = mock(InsumoRepository.class);
        categoriaInsumoRepository = mock(CategoriaInsumoRepository.class);
        NomeNormalizador normalizador = new NomeNormalizador();
        InsumoMapper mapper = new InsumoMapper(categoriaInsumoRepository, normalizador);
        service = new InsumoCatalogoService(repository, categoriaInsumoRepository, mapper,
                normalizador, new CatalogoCursorCodec(), new CatalogoProperties(), new InsumoDefaultsProperties());
    }

    private ObterOuCriarInsumoRequestDTO request(String nome, UnidadeMedida unidade, Integer categoria) {
        ObterOuCriarInsumoRequestDTO dto = new ObterOuCriarInsumoRequestDTO();
        dto.setNome(nome);
        dto.setUnidade(unidade);
        dto.setFkCategoriaInsumo(categoria);
        return dto;
    }

    @Test
    @DisplayName("Retorna insumo existente pelo nome normalizado sem criar")
    void retornaExistente() {
        Insumo existente = new Insumo();
        existente.setIdInsumo(7);
        existente.setNome("Farinha de Trigo");
        when(repository.findByNomeNormalizadoAndUnidadeAndStatus("farinha de trigo", UnidadeMedida.KG, StatusAtivo.ATIVO))
                .thenReturn(Optional.of(existente));

        InsumoCatalogoService.ObterOuCriarResultado resultado =
                service.obterOuCriar(request("  farinha de trigo ", UnidadeMedida.KG, null));

        assertFalse(resultado.criado());
        assertEquals(7, resultado.insumo().getIdInsumo());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Cria insumo na categoria informada")
    void criaNaCategoriaInformada() {
        CategoriaInsumo farinhas = new CategoriaInsumo();
        farinhas.setIdCategoriaInsumo(3);
        farinhas.setNome("Farinhas");
        when(repository.findByNomeNormalizadoAndUnidadeAndStatus(any(), any(), any())).thenReturn(Optional.empty());
        when(categoriaInsumoRepository.findById(3)).thenReturn(Optional.of(farinhas));
        when(repository.save(any(Insumo.class))).thenAnswer(inv -> inv.getArgument(0));

        InsumoCatalogoService.ObterOuCriarResultado resultado =
                service.obterOuCriar(request("Farinha de mandioca", UnidadeMedida.KG, 3));

        assertTrue(resultado.criado());
        assertEquals(farinhas, resultado.insumo().getCategoria());
        assertEquals(StatusAtivo.ATIVO, resultado.insumo().getStatus());
        assertEquals(BigDecimal.ZERO, resultado.insumo().getQuantidadeAtual());
    }

    @Test
    @DisplayName("Categoria inexistente cai na categoria padrão existente")
    void categoriaInexistenteUsaPadrao() {
        CategoriaInsumo padrao = new CategoriaInsumo();
        padrao.setIdCategoriaInsumo(99);
        padrao.setNome("A classificar");
        when(repository.findByNomeNormalizadoAndUnidadeAndStatus(any(), any(), any())).thenReturn(Optional.empty());
        when(categoriaInsumoRepository.findById(555)).thenReturn(Optional.empty());
        when(categoriaInsumoRepository.findAll()).thenReturn(List.of(padrao));
        when(repository.save(any(Insumo.class))).thenAnswer(inv -> inv.getArgument(0));

        InsumoCatalogoService.ObterOuCriarResultado resultado =
                service.obterOuCriar(request("Novo item", UnidadeMedida.UNIDADE, 555));

        assertTrue(resultado.criado());
        assertEquals(padrao, resultado.insumo().getCategoria());
        verify(categoriaInsumoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Categoria padrão ausente é criada")
    void categoriaPadraoCriadaQuandoAusente() {
        when(repository.findByNomeNormalizadoAndUnidadeAndStatus(any(), any(), any())).thenReturn(Optional.empty());
        when(categoriaInsumoRepository.findAll()).thenReturn(List.of());
        when(categoriaInsumoRepository.save(any(CategoriaInsumo.class))).thenAnswer(inv -> inv.getArgument(0));
        when(repository.save(any(Insumo.class))).thenAnswer(inv -> inv.getArgument(0));

        InsumoCatalogoService.ObterOuCriarResultado resultado =
                service.obterOuCriar(request("Sem categoria", UnidadeMedida.KG, null));

        assertTrue(resultado.criado());
        assertEquals("A classificar", resultado.insumo().getCategoria().getNome());
        verify(categoriaInsumoRepository).save(any(CategoriaInsumo.class));
    }

    @Test
    @DisplayName("Criação repetida do mesmo insumo cria apenas uma vez")
    void criacaoRepetida() {
        Insumo criado = new Insumo();
        criado.setIdInsumo(10);
        criado.setNome("Fermento");
        criado.setNomeNormalizado("fermento");
        when(categoriaInsumoRepository.findAll()).thenReturn(List.of());
        when(categoriaInsumoRepository.save(any(CategoriaInsumo.class))).thenAnswer(inv -> inv.getArgument(0));
        when(repository.save(any(Insumo.class))).thenAnswer(inv -> inv.getArgument(0));
        // first call: not found; after creation, the row exists
        when(repository.findByNomeNormalizadoAndUnidadeAndStatus("fermento", UnidadeMedida.UNIDADE, StatusAtivo.ATIVO))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(criado));

        InsumoCatalogoService.ObterOuCriarResultado primeiro =
                service.obterOuCriar(request("Fermento", UnidadeMedida.UNIDADE, null));
        InsumoCatalogoService.ObterOuCriarResultado segundo =
                service.obterOuCriar(request("Fermento", UnidadeMedida.UNIDADE, null));

        assertTrue(primeiro.criado());
        assertFalse(segundo.criado());
        verify(repository, times(1)).save(any(Insumo.class));
    }

    @Test
    @DisplayName("Nome com mais de 45 caracteres lança 422 e não cria")
    void nomeLongoRejeitado() {
        String nomeLongo = "a".repeat(60);

        assertThrows(InsumoNomeInvalidoException.class,
                () -> service.obterOuCriar(request(nomeLongo, UnidadeMedida.KG, null)));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Nome em branco lança 422 e não cria")
    void nomeBrancoRejeitado() {
        assertThrows(InsumoNomeInvalidoException.class,
                () -> service.obterOuCriar(request("   ", UnidadeMedida.KG, null)));

        verify(repository, never()).save(any());
    }
}
