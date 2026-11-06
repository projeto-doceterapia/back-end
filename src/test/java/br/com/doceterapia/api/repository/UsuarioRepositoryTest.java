package br.com.doceterapia.api.repository;

import br.com.doceterapia.api.entity.Usuario;
import br.com.doceterapia.api.enums.Perfil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@DisplayName("Testes de persistência para UsuarioRepository")
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository repository;

    private Usuario novoUsuario(String email) {
        Usuario usuario = new Usuario();
        usuario.setNome("Dona Exemplo");
        usuario.setEmail(email);
        usuario.setSenha("hash-ficticio");
        usuario.setPerfil(Perfil.DONA);
        return usuario;
    }

    @BeforeEach
    void setUp() {
        repository.saveAndFlush(novoUsuario("dona@exemplo.com"));
    }

    @Test
    @DisplayName("A busca de e-mail existente não diferencia maiúsculas")
    void existsByEmailIgnoreCaseIgnoraCaixa() {
        assertTrue(repository.existsByEmailIgnoreCase("DONA@Exemplo.com"));
        assertFalse(repository.existsByEmailIgnoreCase("outra@exemplo.com"));
    }

    @Test
    @DisplayName("O banco recusa e-mail repetido")
    void emailRepetidoViolaRestricaoUnica() {
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(novoUsuario("dona@exemplo.com")));
    }
}
