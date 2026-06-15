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
import br.com.doceterapia.api.dto.InsumoRequestDTO;
import br.com.doceterapia.api.dto.InsumoResponseDTO;

import java.util.List;

@Tag(name = "Insumos", description = "Endpoints para gerenciamento de insumos")
public interface InsumoControllerOpenApi {

    @Operation(summary = "Listar todos os insumos", description = "Retorna uma lista com todos os insumos cadastrados no sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Insumos listados com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = InsumoResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<List<InsumoResponseDTO>> listarInsumos();

    @Operation(summary = "Cadastrar novo insumo",
            description = "Cria um novo insumo no sistema com os dados fornecidos")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Insumo cadastrado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = InsumoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos:\n" +
                    "- ID da categoria vazio\n" +
                    "- Nome vazio\n" +
                    "- Quantidade atual inválida\n" +
                    "- Quantidade mínima inválida\n" +
                    "- Unidade vazia"),
            @ApiResponse(responseCode = "404", description = "Categoria de insumo não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<InsumoResponseDTO> cadastrarInsumo(
            @RequestBody(description = "Dados do insumo a ser cadastrado", required = true,
                    content = @Content(schema = @Schema(implementation = InsumoRequestDTO.class)))
            InsumoRequestDTO request);

    @Operation(summary = "Atualizar insumo",
            description = "Atualiza os dados de um insumo existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Insumo atualizado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = InsumoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "404", description = "Insumo com ID fornecido não encontrado ou categoria não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<InsumoResponseDTO> atualizarInsumo(
            @Parameter(description = "ID do insumo a ser atualizado", example = "1")
            Integer id,
            @RequestBody(description = "Novos dados do insumo", required = true,
                    content = @Content(schema = @Schema(implementation = InsumoRequestDTO.class)))
            InsumoRequestDTO request);

    @Operation(summary = "Deletar insumo",
            description = "Remove um insumo do sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Insumo deletado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Insumo com ID fornecido não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> deletarInsumo(
            @Parameter(description = "ID do insumo a ser deletado", example = "1")
            Integer id);
}
