package br.com.doceterapia.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    @Test
    @DisplayName("Deve listar todos os usuários")
    void testListarTodos() {
        // Arrange
        Usuario usuario2 = new Usuario();
        usuario2.setId(2L);
        usuario2.setNome("Maria Silva");
        usuario2.setEmail("maria@example.com");

        when(usuarioRepository.findAll()).thenReturn(List.of(usuario, usuario2));

        // Act
        var resultado = usuarioService.listarTodos();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
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
