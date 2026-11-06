package br.com.doceterapia.api.service;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import br.com.doceterapia.api.config.GerenciadorTokenJwt;
import br.com.doceterapia.api.dto.UsuarioLogadoResponseDTO;
import br.com.doceterapia.api.dto.UsuarioTokenDto;
import br.com.doceterapia.api.entity.Usuario;
import br.com.doceterapia.api.enums.Perfil;
import br.com.doceterapia.api.enums.ResultadoUsuarioInicial;
import br.com.doceterapia.api.exception.UsuarioEmailAlreadyExistsException;
import br.com.doceterapia.api.exception.UsuarioLogadoNotFoundException;
import br.com.doceterapia.api.repository.UsuarioRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para UsuarioService")
class UsuarioServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private GerenciadorTokenJwt gerenciadorTokenJwt;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;
    private String senhaOriginal = "senha123";
    private String senhaCriptografada = "$2a$10$criptografada";

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("João Silva");
        usuario.setEmail("joao@example.com");
        usuario.setSenha(senhaOriginal);
        usuario.setPerfil(Perfil.FUNCIONARIA);
    }

    @Test
    @DisplayName("Deve criar um usuário com senha criptografada")
    void testCriarUsuario() {
        // Arrange
        when(passwordEncoder.encode(senhaOriginal)).thenReturn(senhaCriptografada);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // Act
        usuarioService.criar(usuario);

        // Assert
        verify(passwordEncoder).encode(senhaOriginal);
        verify(usuarioRepository).save(any(Usuario.class));
        assertEquals(senhaCriptografada, usuario.getSenha());
    }

    @Test
    @DisplayName("Cria usuário com o perfil escolhido e a senha em hash")
    void criarUsuarioGravaOPerfilEscolhido() {
        // Arrange
        usuario.setPerfil(Perfil.FUNCIONARIA);
        when(passwordEncoder.encode(senhaOriginal)).thenReturn(senhaCriptografada);

        // Act
        usuarioService.criar(usuario);

        // Assert
        ArgumentCaptor<Usuario> gravado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(gravado.capture());
        assertEquals(Perfil.FUNCIONARIA, gravado.getValue().getPerfil());
        assertEquals(senhaCriptografada, gravado.getValue().getSenha());
    }

    @Test
    @DisplayName("Deve recusar e-mail já cadastrado sem gravar")
    void criarEmailDuplicadoLancaExcecao() {
        // Arrange
        when(usuarioRepository.existsByEmailIgnoreCase(usuario.getEmail())).thenReturn(true);

        // Act & Assert
        assertThrows(UsuarioEmailAlreadyExistsException.class, () -> usuarioService.criar(usuario));

        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("Deve autenticar usuário e retornar token")
    void testAutenticarUsuario() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        String token = "token_jwt_valido";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(usuarioRepository.findByEmail(usuario.getEmail()))
                .thenReturn(Optional.of(usuario));
        when(gerenciadorTokenJwt.generateToken(authentication))
                .thenReturn(token);

        // Act
        UsuarioTokenDto resultado = usuarioService.autenticar(usuario);

        // Assert
        assertNotNull(resultado);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(usuarioRepository).findByEmail(usuario.getEmail());
        verify(gerenciadorTokenJwt).generateToken(authentication);
    }

    @Test
    @DisplayName("Deve lançar exceção quando email não existir na autenticação")
    void testAutenticarUsuarioEmailNaoExiste() {
        // Arrange
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(usuarioRepository.findByEmail(usuario.getEmail()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> {
            usuarioService.autenticar(usuario);
        });

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(usuarioRepository).findByEmail(usuario.getEmail());
    }

    @Test
    @DisplayName("Deve devolver os dados do usuário logado sem a senha")
    void buscarUsuarioLogadoRetornaDadosSemSenha() {
        // Arrange
        when(usuarioRepository.findByEmail("joao@example.com")).thenReturn(Optional.of(usuario));

        // Act
        UsuarioLogadoResponseDTO resultado = usuarioService.buscarUsuarioLogado("joao@example.com");

        // Assert
        assertEquals(1L, resultado.getId());
        assertEquals("João Silva", resultado.getNome());
        assertEquals("joao@example.com", resultado.getEmail());
        assertEquals(Perfil.FUNCIONARIA, resultado.getPerfil());
        assertTrue(Arrays.stream(UsuarioLogadoResponseDTO.class.getDeclaredFields())
                .noneMatch(campo -> campo.getName().equals("senha")));
    }

    @Test
    @DisplayName("Deve recusar a sessão quando o usuário logado não existir mais")
    void buscarUsuarioLogadoInexistenteLancaExcecao() {
        // Arrange
        when(usuarioRepository.findByEmail("joao@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsuarioLogadoNotFoundException.class,
                () -> usuarioService.buscarUsuarioLogado("joao@example.com"));
    }

    private UsuarioService servicoComValidacaoReal() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        return new UsuarioService(passwordEncoder, usuarioRepository, gerenciadorTokenJwt,
                authenticationManager, validator);
    }

    @Test
    @DisplayName("Tabela vazia e variáveis válidas: grava com a senha codificada")
    void criarUsuarioInicialComTabelaVaziaCriaUsuario() {
        // Arrange
        when(usuarioRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode("SenhaFicticia1")).thenReturn(senhaCriptografada);

        // Act
        ResultadoUsuarioInicial resultado = servicoComValidacaoReal()
                .criarUsuarioInicial("Dona Exemplo", "dona@exemplo.com", "SenhaFicticia1");

        // Assert
        ArgumentCaptor<Usuario> gravado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(gravado.capture());
        assertEquals(ResultadoUsuarioInicial.CRIADO, resultado);
        assertEquals("Dona Exemplo", gravado.getValue().getNome());
        assertEquals("dona@exemplo.com", gravado.getValue().getEmail());
        assertEquals(senhaCriptografada, gravado.getValue().getSenha());
        assertEquals(Perfil.DONA, gravado.getValue().getPerfil());
    }

    @Test
    @DisplayName("Nome ausente ou em branco: usa o nome padrão")
    void criarUsuarioInicialSemNomeUsaPadrao() {
        // Arrange
        when(usuarioRepository.count()).thenReturn(0L);
        UsuarioService servico = servicoComValidacaoReal();

        // Act
        servico.criarUsuarioInicial("", "dona@exemplo.com", "SenhaFicticia1");
        servico.criarUsuarioInicial(null, "dona2@exemplo.com", "SenhaFicticia1");

        // Assert
        ArgumentCaptor<Usuario> gravados = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository, times(2)).save(gravados.capture());
        assertEquals("Dona", gravados.getAllValues().get(0).getNome());
        assertEquals("Dona", gravados.getAllValues().get(1).getNome());
    }

    @Test
    @DisplayName("Com usuários existentes: ignora as variáveis e não grava")
    void criarUsuarioInicialComUsuariosExistentesIgnora() {
        // Arrange
        when(usuarioRepository.count()).thenReturn(1L);

        // Act
        ResultadoUsuarioInicial resultado = servicoComValidacaoReal()
                .criarUsuarioInicial("Dona Exemplo", "dona@exemplo.com", "SenhaFicticia1");

        // Assert
        assertEquals(ResultadoUsuarioInicial.IGNORADO_USUARIOS_EXISTENTES, resultado);
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Tabela vazia e e-mail ou senha em branco: não grava")
    void criarUsuarioInicialComVariaveisAusentesNaoCria() {
        // Arrange
        when(usuarioRepository.count()).thenReturn(0L);
        UsuarioService servico = servicoComValidacaoReal();

        // Act & Assert
        assertEquals(ResultadoUsuarioInicial.VARIAVEIS_AUSENTES,
                servico.criarUsuarioInicial("Dona Exemplo", "", "SenhaFicticia1"));
        assertEquals(ResultadoUsuarioInicial.VARIAVEIS_AUSENTES,
                servico.criarUsuarioInicial("Dona Exemplo", "dona@exemplo.com", " "));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Tabela vazia e variáveis inválidas: não grava")
    void criarUsuarioInicialComVariaveisInvalidasNaoCria() {
        // Arrange
        when(usuarioRepository.count()).thenReturn(0L);
        UsuarioService servico = servicoComValidacaoReal();

        // Act & Assert
        assertEquals(ResultadoUsuarioInicial.VARIAVEIS_INVALIDAS,
                servico.criarUsuarioInicial("Dona Exemplo", "dona@exemplo.com", "123"));
        assertEquals(ResultadoUsuarioInicial.VARIAVEIS_INVALIDAS,
                servico.criarUsuarioInicial("Dona Exemplo", "email-invalido", "SenhaFicticia1"));
        assertEquals(ResultadoUsuarioInicial.VARIAVEIS_INVALIDAS,
                servico.criarUsuarioInicial("Do", "dona@exemplo.com", "SenhaFicticia1"));
        assertEquals(ResultadoUsuarioInicial.VARIAVEIS_INVALIDAS,
                servico.criarUsuarioInicial("D".repeat(51), "dona@exemplo.com", "SenhaFicticia1"));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve listar todos os usuários")
    void testListarTodos() {
        // Arrange
        Usuario usuario2 = new Usuario();
        usuario2.setId(2L);
        usuario2.setNome("Maria Silva");
        usuario2.setEmail("maria@example.com");
        usuario2.setPerfil(Perfil.DONA);

        when(usuarioRepository.findAll()).thenReturn(List.of(usuario, usuario2));

        // Act
        var resultado = usuarioService.listarTodos();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals(Perfil.FUNCIONARIA, resultado.get(0).getPerfil());
        assertEquals(Perfil.DONA, resultado.get(1).getPerfil());
        verify(usuarioRepository).findAll();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver usuários")
    void testListarTodosVazio() {
        // Arrange
        when(usuarioRepository.findAll()).thenReturn(List.of());

        // Act
        var resultado = usuarioService.listarTodos();

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(usuarioRepository).findAll();
    }
}
