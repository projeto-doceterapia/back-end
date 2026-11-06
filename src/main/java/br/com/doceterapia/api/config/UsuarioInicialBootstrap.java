package br.com.doceterapia.api.config;

import br.com.doceterapia.api.enums.ResultadoUsuarioInicial;
import br.com.doceterapia.api.service.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Creates the first user on startup. It never fails the startup, so a bad setting
 * cannot put a container with a restart policy into a loop, and it logs only the
 * outcome: the values of the settings must stay out of the log.
 */
@Component
public class UsuarioInicialBootstrap implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(UsuarioInicialBootstrap.class);

    private final UsuarioService usuarioService;
    private final String nome;
    private final String email;
    private final String senha;

    public UsuarioInicialBootstrap(UsuarioService usuarioService,
                                   @Value("${bootstrap.usuario-inicial.nome:}") String nome,
                                   @Value("${bootstrap.usuario-inicial.email:}") String email,
                                   @Value("${bootstrap.usuario-inicial.senha:}") String senha) {
        this.usuarioService = usuarioService;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
    }

    @Override
    public void run(ApplicationArguments args) {
        registrar(usuarioService.criarUsuarioInicial(nome, email, senha));
    }

    private void registrar(ResultadoUsuarioInicial resultado) {
        switch (resultado) {
            case CRIADO -> LOGGER.info(
                    "Initial user created from ADMIN_EMAIL. Remove ADMIN_SENHA from the environment.");
            case VARIAVEIS_AUSENTES -> LOGGER.warn(
                    "No users found and ADMIN_EMAIL/ADMIN_SENHA are not set: nobody can sign in. "
                            + "Set both variables and restart the back-end.");
            case VARIAVEIS_INVALIDAS -> LOGGER.error(
                    "ADMIN_NOME, ADMIN_EMAIL or ADMIN_SENHA is invalid (name with 3 to 50 characters, valid e-mail, "
                            + "password with 6 to 20 characters); initial user not created.");
            case IGNORADO_USUARIOS_EXISTENTES -> registrarUsuariosExistentes();
        }
    }

    private void registrarUsuariosExistentes() {
        if (senha.isBlank()) {
            LOGGER.debug("Users already exist; initial user not needed.");
        } else {
            LOGGER.warn("Users already exist; ADMIN_EMAIL and ADMIN_SENHA are ignored. "
                    + "Remove ADMIN_SENHA from the environment.");
        }
    }
}
