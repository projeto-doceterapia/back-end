package br.com.doceterapia.api.service;

import jakarta.validation.Validator;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import br.com.doceterapia.api.config.GerenciadorTokenJwt;
import br.com.doceterapia.api.dto.UsuarioCriacaoDto;
import br.com.doceterapia.api.dto.UsuarioListarDto;
import br.com.doceterapia.api.dto.UsuarioLogadoResponseDTO;
import br.com.doceterapia.api.dto.UsuarioMapper;
import br.com.doceterapia.api.dto.UsuarioTokenDto;
import br.com.doceterapia.api.entity.Usuario;
import br.com.doceterapia.api.enums.ResultadoUsuarioInicial;
import br.com.doceterapia.api.exception.UsuarioEmailAlreadyExistsException;
import br.com.doceterapia.api.exception.UsuarioLogadoNotFoundException;
import br.com.doceterapia.api.repository.UsuarioRepository;

import java.util.List;

@Service
public class UsuarioService {

    private static final String NOME_USUARIO_INICIAL_PADRAO = "Dona";

    private final PasswordEncoder passwordEncoder;
    private final UsuarioRepository usuarioRepository;
    private final GerenciadorTokenJwt gerenciadorTokenJwt;
    private final AuthenticationManager authenticationManager;
    private final Validator validator;

    public UsuarioService(PasswordEncoder passwordEncoder,
                          UsuarioRepository usuarioRepository,
                          GerenciadorTokenJwt gerenciadorTokenJwt,
                          AuthenticationManager authenticationManager,
                          Validator validator) {
        this.passwordEncoder = passwordEncoder;
        this.usuarioRepository = usuarioRepository;
        this.gerenciadorTokenJwt = gerenciadorTokenJwt;
        this.authenticationManager = authenticationManager;
        this.validator = validator;
    }

    public void criar(Usuario novoUsuario) {
        if (usuarioRepository.existsByEmailIgnoreCase(novoUsuario.getEmail())) {
            throw new UsuarioEmailAlreadyExistsException();
        }

        String senhaCriptografada = passwordEncoder.encode(novoUsuario.getSenha());
        novoUsuario.setSenha(senhaCriptografada);
        this.usuarioRepository.save(novoUsuario);
    }

    /**
     * Creates the first user from startup settings, only while the table is empty.
     * A blank name counts as absent, because a container variable passed through
     * empty would otherwise fail validation instead of using the default.
     */
    @Transactional
    public ResultadoUsuarioInicial criarUsuarioInicial(String nome, String email, String senha) {
        if (usuarioRepository.count() > 0) {
            return ResultadoUsuarioInicial.IGNORADO_USUARIOS_EXISTENTES;
        }
        if (isBlank(email) || isBlank(senha)) {
            return ResultadoUsuarioInicial.VARIAVEIS_AUSENTES;
        }

        UsuarioCriacaoDto dados = new UsuarioCriacaoDto();
        dados.setNome(isBlank(nome) ? NOME_USUARIO_INICIAL_PADRAO : nome);
        dados.setEmail(email);
        dados.setSenha(senha);
        if (!validator.validate(dados).isEmpty()) {
            return ResultadoUsuarioInicial.VARIAVEIS_INVALIDAS;
        }

        criar(UsuarioMapper.of(dados));
        return ResultadoUsuarioInicial.CRIADO;
    }

    private static boolean isBlank(String valor) {
        return valor == null || valor.isBlank();
    }

    public UsuarioTokenDto autenticar(Usuario usuario) {
        final UsernamePasswordAuthenticationToken credentials =
                new UsernamePasswordAuthenticationToken(usuario.getEmail(), usuario.getSenha());

        final Authentication authentication = this.authenticationManager.authenticate(credentials);

        Usuario usuarioAutenticado = usuarioRepository.findByEmail(usuario.getEmail())
                .orElseThrow(() -> new ResponseStatusException(404, "Email do usuário não cadastrado", null));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        final String token = gerenciadorTokenJwt.generateToken(authentication);

        return UsuarioMapper.of(usuarioAutenticado, token);
    }

    public UsuarioLogadoResponseDTO buscarUsuarioLogado(String email) {
        return usuarioRepository.findByEmail(email)
                .map(UsuarioMapper::toUsuarioLogadoResponse)
                .orElseThrow(UsuarioLogadoNotFoundException::new);
    }

    public List<UsuarioListarDto> listarTodos() {
        return usuarioRepository.findAll().stream().map(UsuarioMapper::of).toList();
    }
}
