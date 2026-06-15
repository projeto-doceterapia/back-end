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
import br.com.doceterapia.api.dto.CategoriaInsumoRequestDTO;
import br.com.doceterapia.api.dto.CategoriaInsumoResponseDTO;

import java.util.List;

@Tag(name = "Categorias de Insumo", description = "Endpoints para gerenciamento de categorias de insumo")
public interface CategoriaInsumoControllerOpenApi {

    @Operation(summary = "Listar todas as categorias de insumo", description = "Retorna uma lista com todas as categorias de insumo cadastradas no sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categorias listadas com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CategoriaInsumoResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<List<CategoriaInsumoResponseDTO>> listarCategoriasInsumo();

    @Operation(summary = "Cadastrar nova categoria de insumo",
            description = "Cria uma nova categoria de insumo no sistema com os dados fornecidos")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Categoria cadastrada com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CategoriaInsumoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos:\n" +
                    "- Nome vazio\n" +
                    "- Descrição muito longa"),
            @ApiResponse(responseCode = "409", description = "Nome de categoria já cadastrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<CategoriaInsumoResponseDTO> cadastrarCategoriaInsumo(
            @RequestBody(description = "Dados da categoria de insumo a ser cadastrada", required = true,
                    content = @Content(schema = @Schema(implementation = CategoriaInsumoRequestDTO.class)))
            CategoriaInsumoRequestDTO request);

    @Operation(summary = "Atualizar categoria de insumo",
            description = "Atualiza os dados de uma categoria de insumo existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria atualizada com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CategoriaInsumoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos:\n" +
                    "- Nome vazio\n" +
                    "- Descrição muito longa"),
            @ApiResponse(responseCode = "404", description = "Categoria com ID fornecido não encontrada"),
            @ApiResponse(responseCode = "409", description = "Nome de categoria já cadastrado para outra categoria"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<CategoriaInsumoResponseDTO> atualizarCategoriaInsumo(
            @Parameter(description = "ID da categoria de insumo a ser atualizada", example = "1")
            Integer id,
            @RequestBody(description = "Novos dados da categoria de insumo", required = true,
                    content = @Content(schema = @Schema(implementation = CategoriaInsumoRequestDTO.class)))
            CategoriaInsumoRequestDTO request);

    @Operation(summary = "Deletar categoria de insumo",
            description = "Remove uma categoria de insumo do sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Categoria deletada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Categoria com ID fornecido não encontrada"),
            @ApiResponse(responseCode = "409", description = "Categoria possui insumos associados e não pode ser excluída"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> deletarCategoriaInsumo(
            @Parameter(description = "ID da categoria de insumo a ser deletada", example = "1")
            Integer id);
}
