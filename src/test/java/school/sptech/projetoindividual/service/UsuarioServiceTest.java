package school.sptech.projetoindividual.service;

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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import school.sptech.projetoindividual.config.GerenciadorTokenJwt;
import school.sptech.projetoindividual.dto.UsuarioTokenDto;
import school.sptech.projetoindividual.entity.Usuario;
import school.sptech.projetoindividual.repository.UsuarioRepository;

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
