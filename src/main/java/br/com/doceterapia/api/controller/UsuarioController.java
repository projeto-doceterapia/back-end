package br.com.doceterapia.api.controller;

import br.com.doceterapia.api.config.GerenciadorCookieSessao;
import br.com.doceterapia.api.dto.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import br.com.doceterapia.api.entity.Usuario;
import br.com.doceterapia.api.service.UsuarioService;
import br.com.doceterapia.api.swagger.UsuarioControllerOpenApi;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController implements UsuarioControllerOpenApi {

    public static final String COOKIE_NOME = "authToken";

    private final UsuarioService usuarioService;
    private final GerenciadorCookieSessao gerenciadorCookieSessao;

    public UsuarioController(UsuarioService usuarioService, GerenciadorCookieSessao gerenciadorCookieSessao) {
        this.usuarioService = usuarioService;
        this.gerenciadorCookieSessao = gerenciadorCookieSessao;
    }

    @PostMapping
    @Override
    public ResponseEntity<Void> criar(@RequestBody @Valid UsuarioCriacaoDto usuarioCriacaoDto) {
        final Usuario novoUsuario = UsuarioMapper.of(usuarioCriacaoDto);
        this.usuarioService.criar(novoUsuario);
        return ResponseEntity.status(201).build();
    }

    @PostMapping("/login")
    @Override
    public ResponseEntity<UsuarioSessaoDto> login(
            @RequestBody UsuarioLoginDto usuarioLoginDto,
            HttpServletResponse response) {

        final Usuario usuario = UsuarioMapper.of(usuarioLoginDto);
        UsuarioTokenDto autenticado = this.usuarioService.autenticar(usuario);

        response.addHeader(HttpHeaders.SET_COOKIE, gerenciadorCookieSessao.criar(autenticado.getToken()).toString());

        UsuarioSessaoDto sessao = UsuarioMapper.ofSessao(autenticado);
        return ResponseEntity.ok(sessao);
    }

    @PostMapping("/logout")
    @Override
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, gerenciadorCookieSessao.remover().toString());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @Override
    public ResponseEntity<UsuarioLogadoResponseDTO> buscarUsuarioLogado(Authentication authentication) {
        return ResponseEntity.ok(this.usuarioService.buscarUsuarioLogado(authentication.getName()));
    }

    @GetMapping
    @Override
    public ResponseEntity<List<UsuarioListarDto>> listarTodos() {
        List<UsuarioListarDto> usuarios = this.usuarioService.listarTodos();

        if (usuarios.isEmpty()) {
            return ResponseEntity.status(204).build();
        }
        return ResponseEntity.ok(usuarios);
    }
}
