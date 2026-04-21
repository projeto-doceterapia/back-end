package school.sptech.projetoindividual.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import school.sptech.projetoindividual.dto.UsuarioCriacaoDto;
import school.sptech.projetoindividual.dto.UsuarioListarDto;
import school.sptech.projetoindividual.dto.UsuarioLoginDto;
import school.sptech.projetoindividual.dto.UsuarioSessaoDto;

import java.util.List;

@Tag(name = "Usuários", description = "Endpoints para autenticação e gerenciamento de usuários")
public interface UsuarioControllerOpenApi {

    @Operation(
            summary = "Cadastrar novo usuário",
            description = "Cria um novo usuário no sistema com os dados fornecidos. A senha é armazenada com hash BCrypt."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos:\n" +
                    "- Nome com menos de 3 ou mais de 50 caracteres\n" +
                    "- E-mail inválido\n" +
                    "- Senha com menos de 6 ou mais de 20 caracteres"),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @SecurityRequirement(name = "Bearer")
    ResponseEntity<Void> criar(
            @RequestBody(description = "Dados do usuário a ser cadastrado", required = true,
                    content = @Content(schema = @Schema(implementation = UsuarioCriacaoDto.class)))
            UsuarioCriacaoDto usuarioCriacaoDto);

    @Operation(
            summary = "Autenticar usuário",
            description = "Valida as credenciais do usuário e emite um token JWT via cookie HttpOnly (`authToken`). " +
                    "O token não é retornado no body — ele é enviado no header `Set-Cookie` e gerenciado " +
                    "automaticamente pelo browser. Utilize o endpoint `/usuarios/logout` para encerrar a sessão."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso. Token enviado via cookie `Set-Cookie`.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UsuarioSessaoDto.class))),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas (e-mail ou senha incorretos)"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<UsuarioSessaoDto> login(
            @RequestBody(description = "Credenciais do usuário", required = true,
                    content = @Content(schema = @Schema(implementation = UsuarioLoginDto.class)))
            UsuarioLoginDto usuarioLoginDto,
            HttpServletResponse response);

    @Operation(
            summary = "Encerrar sessão",
            description = "Invalida a sessão do usuário removendo o cookie `authToken` do browser (Max-Age=0). " +
                    "O token JWT permanece tecnicamente válido até expirar, por isso use tokens de curta duração em produção."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Logout realizado com sucesso. Cookie removido."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> logout(HttpServletResponse response);

    @Operation(
            summary = "Listar todos os usuários",
            description = "Retorna uma lista com todos os usuários cadastrados. Requer autenticação via token JWT."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuários listados com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UsuarioListarDto.class))),
            @ApiResponse(responseCode = "204", description = "Nenhum usuário cadastrado"),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @SecurityRequirement(name = "Bearer")
    ResponseEntity<List<UsuarioListarDto>> listarTodos();
}
