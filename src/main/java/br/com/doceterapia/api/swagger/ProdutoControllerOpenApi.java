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
import br.com.doceterapia.api.dto.ProdutoRequestDTO;
import br.com.doceterapia.api.dto.ProdutoResponseDTO;

import java.util.List;

@Tag(name = "Produtos", description = "Endpoints para gerenciamento de produtos")
public interface ProdutoControllerOpenApi {

    @Operation(summary = "Listar todos os produtos", description = "Retorna uma lista com todos os produtos cadastrados no sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produtos listados com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProdutoResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<List<ProdutoResponseDTO>> listarProdutos();

    @Operation(summary = "Cadastrar novo produto",
            description = "Cria um novo produto no sistema com os dados fornecidos")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Produto cadastrado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProdutoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos:\n" +
                    "- Nome vazio\n" +
                    "- Categoria não informada\n" +
                    "- Valores negativos"),
            @ApiResponse(responseCode = "404", description = "Categoria de produto com ID fornecido não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<ProdutoResponseDTO> cadastrarProduto(
            @RequestBody(description = "Dados do produto a ser cadastrado", required = true,
                    content = @Content(schema = @Schema(implementation = ProdutoRequestDTO.class)))
            ProdutoRequestDTO request);

    @Operation(summary = "Atualizar produto",
            description = "Atualiza os dados de um produto existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProdutoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos:\n" +
                    "- Nome vazio\n" +
                    "- Categoria não informada\n" +
                    "- Valores negativos"),
            @ApiResponse(responseCode = "404", description = "Produto com ID fornecido não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<ProdutoResponseDTO> atualizarProduto(
            @Parameter(description = "ID do produto a ser atualizado", example = "1")
            Integer id,
            @RequestBody(description = "Novos dados do produto", required = true,
                    content = @Content(schema = @Schema(implementation = ProdutoRequestDTO.class)))
            ProdutoRequestDTO request);

    @Operation(summary = "Deletar produto",
            description = "Remove um produto do sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Produto deletado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Produto com ID fornecido não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> deletarProduto(
            @Parameter(description = "ID do produto a ser deletado", example = "1")
            Integer id);
}
