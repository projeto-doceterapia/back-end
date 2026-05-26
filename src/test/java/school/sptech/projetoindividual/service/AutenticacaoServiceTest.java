package school.sptech.projetoindividual.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import school.sptech.projetoindividual.dto.UsuarioDetalhesDto;
import school.sptech.projetoindividual.entity.Usuario;
import school.sptech.projetoindividual.repository.UsuarioRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para AutenticacaoService")
class AutenticacaoServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private AutenticacaoService autenticacaoService;

    private Usuario usuario;
    private String email = "joao@example.com";

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("João Silva");
        usuario.setEmail(email);
        usuario.setSenha("senha123");
    }

    @Test
    @DisplayName("Deve carregar usuário por email com sucesso")
    void testLoadUserByUsername() {
        // Arrange
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));

        // Act
        UserDetails resultado = autenticacaoService.loadUserByUsername(email);

        // Assert
        assertNotNull(resultado);
        assertInstanceOf(UsuarioDetalhesDto.class, resultado);
        verify(usuarioRepository).findByEmail(email);
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não é encontrado")
    void testLoadUserByUsernameNaoEncontrado() {
        // Arrange
        String emailInexistente = "inexistente@example.com";
        when(usuarioRepository.findByEmail(emailInexistente)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            autenticacaoService.loadUserByUsername(emailInexistente);
        });

        verify(usuarioRepository).findByEmail(emailInexistente);
    }

    @Test
    @DisplayName("Deve lançar exceção com mensagem apropriada quando usuário não existe")
    void testLoadUserByUsernameExceptionMessage() {
        // Arrange
        String emailInexistente = "teste@example.com";
        when(usuarioRepository.findByEmail(emailInexistente)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            autenticacaoService.loadUserByUsername(emailInexistente);
        });

        assertTrue(exception.getMessage().contains(emailInexistente));
        assertTrue(exception.getMessage().contains("não encontrado"));
    }

    @Test
    @DisplayName("Deve retornar UserDetails com dados corretos do usuário")
    void testLoadUserByUsernameReturnsCorrectUserDetails() {
        // Arrange
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));

        // Act
        UserDetails resultado = autenticacaoService.loadUserByUsername(email);

        // Assert
        assertNotNull(resultado);
        assertEquals(email, resultado.getUsername());
        verify(usuarioRepository).findByEmail(email);
    }

    @Test
    @DisplayName("Deve carregar usuário com email contendo caracteres especiais")
    void testLoadUserByUsernameEmailEspecial() {
        // Arrange
        String emailEspecial = "usuario+teste@example.co.uk";
        Usuario usuarioEspecial = new Usuario();
        usuarioEspecial.setId(2L);
        usuarioEspecial.setNome("User Teste");
        usuarioEspecial.setEmail(emailEspecial);
        usuarioEspecial.setSenha("senha456");

        when(usuarioRepository.findByEmail(emailEspecial)).thenReturn(Optional.of(usuarioEspecial));

        // Act
        UserDetails resultado = autenticacaoService.loadUserByUsername(emailEspecial);

        // Assert
        assertNotNull(resultado);
        assertEquals(emailEspecial, resultado.getUsername());
        verify(usuarioRepository).findByEmail(emailEspecial);
    }

    @Test
    @DisplayName("Deve ser case-sensitive na busca por email")
    void testLoadUserByUsernameCase() {
        // Arrange
        String emailMinusculo = "joao@example.com";
        String emailMaiusculo = "JOAO@EXAMPLE.COM";

        when(usuarioRepository.findByEmail(emailMinusculo)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findByEmail(emailMaiusculo)).thenReturn(Optional.empty());

        // Act & Assert
        assertNotNull(autenticacaoService.loadUserByUsername(emailMinusculo));
        assertThrows(UsernameNotFoundException.class, () -> {
            autenticacaoService.loadUserByUsername(emailMaiusculo);
        });

        verify(usuarioRepository).findByEmail(emailMinusculo);
        verify(usuarioRepository).findByEmail(emailMaiusculo);
    }
}
