package br.com.doceterapia.api.config;

import br.com.doceterapia.api.enums.ResultadoUsuarioInicial;
import br.com.doceterapia.api.service.UsuarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
@DisplayName("Testes para UsuarioInicialBootstrap")
class UsuarioInicialBootstrapTest {

    private static final String NOME = "Nome Ficticio Unico";
    private static final String EMAIL = "dona.unica@exemplo.com";
    private static final String SENHA = "SenhaFicticiaUnica1";

    @Mock
    private UsuarioService usuarioService;

    private UsuarioInicialBootstrap bootstrap(String nome, String email, String senha) {
        return new UsuarioInicialBootstrap(usuarioService, nome, email, senha);
    }

    @Test
    @DisplayName("Repassa nome, e-mail e senha ao service")
    void repassaVariaveisAoService() {
        when(usuarioService.criarUsuarioInicial(NOME, EMAIL, SENHA)).thenReturn(ResultadoUsuarioInicial.CRIADO);

        bootstrap(NOME, EMAIL, SENHA).run(null);

        verify(usuarioService).criarUsuarioInicial(NOME, EMAIL, SENHA);
    }

    @Test
    @DisplayName("Tabela vazia sem variáveis: avisa citando ADMIN_EMAIL e ADMIN_SENHA e segue no ar")
    void variaveisAusentesRegistraAviso(CapturedOutput saida) {
        when(usuarioService.criarUsuarioInicial("", "", "")).thenReturn(ResultadoUsuarioInicial.VARIAVEIS_AUSENTES);

        bootstrap("", "", "").run(null);

        assertThat(saida).contains("WARN", "ADMIN_EMAIL", "ADMIN_SENHA");
    }

    @Test
    @DisplayName("Variáveis inválidas: registra erro citando as três variáveis")
    void variaveisInvalidasRegistraErro(CapturedOutput saida) {
        when(usuarioService.criarUsuarioInicial(NOME, EMAIL, SENHA)).thenReturn(ResultadoUsuarioInicial.VARIAVEIS_INVALIDAS);

        bootstrap(NOME, EMAIL, SENHA).run(null);

        assertThat(saida).contains("ERROR", "ADMIN_NOME", "ADMIN_EMAIL", "ADMIN_SENHA");
    }

    @Test
    @DisplayName("Usuários existentes com senha preenchida: pede para remover a variável")
    void usuariosExistentesComSenhaRegistraAviso(CapturedOutput saida) {
        when(usuarioService.criarUsuarioInicial(NOME, EMAIL, SENHA))
                .thenReturn(ResultadoUsuarioInicial.IGNORADO_USUARIOS_EXISTENTES);

        bootstrap(NOME, EMAIL, SENHA).run(null);

        assertThat(saida).contains("WARN", "Remove ADMIN_SENHA");
    }

    @Test
    @DisplayName("Usuários existentes sem variáveis: não avisa")
    void usuariosExistentesSemVariaveisNaoAvisa(CapturedOutput saida) {
        when(usuarioService.criarUsuarioInicial("", "", ""))
                .thenReturn(ResultadoUsuarioInicial.IGNORADO_USUARIOS_EXISTENTES);

        bootstrap("", "", "").run(null);

        assertThat(saida).doesNotContain("Remove ADMIN_SENHA");
    }

    @Test
    @DisplayName("Em nenhum resultado a senha, o e-mail ou o nome vão para o log")
    void senhaNuncaApareceNoLog(CapturedOutput saida) {
        for (ResultadoUsuarioInicial resultado : ResultadoUsuarioInicial.values()) {
            when(usuarioService.criarUsuarioInicial(NOME, EMAIL, SENHA)).thenReturn(resultado);

            bootstrap(NOME, EMAIL, SENHA).run(null);
        }

        assertThat(saida).contains("Initial user created");
        assertThat(saida).doesNotContain(SENHA, EMAIL, NOME);
    }
}
