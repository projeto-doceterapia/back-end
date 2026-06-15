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
import br.com.doceterapia.api.dto.ProdutoInsumoRequestDTO;
import br.com.doceterapia.api.dto.ProdutoInsumoResponseDTO;

import java.util.List;

@Tag(name = "Insumos do Produto", description = "Endpoints para gerenciamento de insumos associados a um produto")
public interface ProdutoInsumoControllerOpenApi {

    @Operation(summary = "Listar insumos de um produto", description = "Retorna uma lista com todos os insumos associados a um produto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Insumos listados com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProdutoInsumoResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Produto com ID fornecido não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<List<ProdutoInsumoResponseDTO>> listarInsumosPorProduto(
            @Parameter(description = "ID do produto", example = "1")
            Integer produtoId);

    @Operation(summary = "Associar insumo a um produto",
            description = "Cria um vínculo entre um insumo e um produto")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Insumo associado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProdutoInsumoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos:\n" +
                    "- Quantidade menor ou igual a zero\n" +
                    "- Insumo não informado\n" +
                    "- Unidade não informada"),
            @ApiResponse(responseCode = "404", description = "Produto ou Insumo com ID fornecido não encontrado"),
            @ApiResponse(responseCode = "409", description = "Insumo já está associado a este produto"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<ProdutoInsumoResponseDTO> cadastrarInsumoNoProduto(
            @Parameter(description = "ID do produto", example = "1")
            Integer produtoId,
            @RequestBody(description = "Dados do insumo a ser associado", required = true,
                    content = @Content(schema = @Schema(implementation = ProdutoInsumoRequestDTO.class)))
            ProdutoInsumoRequestDTO request);

    @Operation(summary = "Atualizar insumo associado a um produto",
            description = "Atualiza os dados de um vínculo entre insumo e produto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Insumo atualizado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProdutoInsumoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos:\n" +
                    "- Quantidade menor ou igual a zero\n" +
                    "- Unidade não informada"),
            @ApiResponse(responseCode = "404", description = "Vínculo, Produto ou Insumo com ID fornecido não encontrado"),
            @ApiResponse(responseCode = "409", description = "Insumo já está associado a este produto"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<ProdutoInsumoResponseDTO> atualizarInsumoDoProduto(
            @Parameter(description = "ID do produto", example = "1")
            Integer produtoId,
            @Parameter(description = "ID do vínculo produto-insumo", example = "1")
            Integer id,
            @RequestBody(description = "Novos dados do vínculo", required = true,
                    content = @Content(schema = @Schema(implementation = ProdutoInsumoRequestDTO.class)))
            ProdutoInsumoRequestDTO request);

    @Operation(summary = "Remover insumo de um produto",
            description = "Remove o vínculo entre um insumo e um produto")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Insumo removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Vínculo com ID fornecido não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> deletarInsumoDoProduto(
            @Parameter(description = "ID do produto", example = "1")
            Integer produtoId,
            @Parameter(description = "ID do vínculo produto-insumo a ser removido", example = "1")
            Integer id);
}
