package br.com.doceterapia.api.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import br.com.doceterapia.api.dto.CategoriaProdutoRequestDTO;
import br.com.doceterapia.api.dto.CategoriaProdutoResponseDTO;

import java.util.List;

@Tag(name = "Categorias de Produto", description = "Endpoints para gerenciamento de categorias de produto")
public interface CategoriaProdutoControllerOpenApi {

    @Operation(summary = "Listar todas as categorias de produto", description = "Retorna uma lista com todas as categorias de produto cadastradas no sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categorias listadas com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CategoriaProdutoResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<List<CategoriaProdutoResponseDTO>> listarCategorias();

    @Operation(summary = "Cadastrar nova categoria de produto",
            description = "Cria uma nova categoria de produto no sistema com os dados fornecidos")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Categoria cadastrada com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CategoriaProdutoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos:\n" +
                    "- Nome vazio"),
            @ApiResponse(responseCode = "409", description = "Nome de categoria já cadastrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<CategoriaProdutoResponseDTO> cadastrarCategoria(
            @RequestBody(description = "Dados da categoria a ser cadastrada", required = true,
                    content = @Content(schema = @Schema(implementation = CategoriaProdutoRequestDTO.class)))
            CategoriaProdutoRequestDTO request);

    @Operation(summary = "Atualizar categoria de produto",
            description = "Atualiza os dados de uma categoria de produto existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria atualizada com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CategoriaProdutoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos:\n" +
                    "- Nome vazio"),
            @ApiResponse(responseCode = "404", description = "Categoria com ID fornecido não encontrada"),
            @ApiResponse(responseCode = "409", description = "Nome de categoria já cadastrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<CategoriaProdutoResponseDTO> atualizarCategoria(
            @Parameter(description = "ID da categoria a ser atualizada", example = "1")
            Integer id,
            @RequestBody(description = "Novos dados da categoria", required = true,
                    content = @Content(schema = @Schema(implementation = CategoriaProdutoRequestDTO.class)))
            CategoriaProdutoRequestDTO request);

    @Operation(summary = "Deletar categoria de produto",
            description = "Remove uma categoria de produto do sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Categoria deletada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Categoria com ID fornecido não encontrada"),
            @ApiResponse(responseCode = "409", description = "Categoria possui produtos associados e não pode ser removida"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> deletarCategoria(
            @Parameter(description = "ID da categoria a ser deletada", example = "1")
            Integer id);
}
