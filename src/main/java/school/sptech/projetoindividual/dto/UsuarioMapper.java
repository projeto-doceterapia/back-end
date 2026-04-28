package school.sptech.projetoindividual.dto;

import school.sptech.projetoindividual.entity.Usuario;

public class UsuarioMapper {

    public static Usuario of(UsuarioCriacaoDto dto) {
        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(dto.getSenha());
        return usuario;
    }

    public static Usuario of(UsuarioLoginDto dto) {
        Usuario usuario = new Usuario();
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(dto.getSenha());
        return usuario;
    }

    public static UsuarioTokenDto of(Usuario usuario, String token) {
        UsuarioTokenDto dto = new UsuarioTokenDto();
        dto.setUserId(usuario.getId());
        dto.setNome(usuario.getNome());
        dto.setEmail(usuario.getEmail());
        dto.setToken(token);
        return dto;
    }

    public static UsuarioSessaoDto ofSessao(UsuarioTokenDto tokenDto) {
        UsuarioSessaoDto dto = new UsuarioSessaoDto();
        dto.setUserId(tokenDto.getUserId());
        dto.setNome(tokenDto.getNome());
        dto.setEmail(tokenDto.getEmail());
        dto.setToken(tokenDto.getToken());
        return dto;
    }

    public static UsuarioListarDto of(Usuario usuario) {
        UsuarioListarDto dto = new UsuarioListarDto();
        dto.setId(usuario.getId());
        dto.setNome(usuario.getNome());
        dto.setEmail(usuario.getEmail());
        return dto;
    }
}
