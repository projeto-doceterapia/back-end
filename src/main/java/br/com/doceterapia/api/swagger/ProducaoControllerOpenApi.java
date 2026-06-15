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
import br.com.doceterapia.api.dto.ProducaoRequestDTO;
import br.com.doceterapia.api.dto.ProducaoResponseDTO;
import br.com.doceterapia.api.enums.StatusProducao;

import java.util.List;

@Tag(name = "Produção", description = "Endpoints para gerenciamento de produção")
public interface ProducaoControllerOpenApi {

    @Operation(summary = "Listar todas as produções", description = "Retorna uma lista com todas as produções cadastradas no sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produções listadas com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProducaoResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<List<ProducaoResponseDTO>> listarProducoes();

    @Operation(summary = "Buscar produção por ID", description = "Retorna uma produção pelo seu ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produção encontrada com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProducaoResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Produção não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<ProducaoResponseDTO> buscarProducao(
            @Parameter(description = "ID da produção", example = "1") Integer id);

    @Operation(summary = "Cadastrar nova produção",
            description = "Cria uma nova produção no sistema com os dados fornecidos")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Produção cadastrada com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProducaoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "404", description = "Pedido com ID fornecido não encontrado"),
            @ApiResponse(responseCode = "409", description = "Pedido já possui uma produção cadastrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<ProducaoResponseDTO> cadastrarProducao(
            @RequestBody(description = "Dados da produção a ser cadastrada", required = true,
                    content = @Content(schema = @Schema(implementation = ProducaoRequestDTO.class)))
            ProducaoRequestDTO request);

    @Operation(summary = "Atualizar produção",
            description = "Atualiza os dados de uma produção existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produção atualizada com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProducaoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Produção finalizada não pode ser alterada ou dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Produção ou Pedido com ID fornecido não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<ProducaoResponseDTO> atualizarProducao(
            @Parameter(description = "ID da produção a ser atualizada", example = "1")
            Integer id,
            @RequestBody(description = "Novos dados da produção", required = true,
                    content = @Content(schema = @Schema(implementation = ProducaoRequestDTO.class)))
            ProducaoRequestDTO request);

    @Operation(summary = "Deletar produção",
            description = "Remove uma produção do sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Produção deletada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Produção com ID fornecido não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<Void> deletarProducao(
            @Parameter(description = "ID da produção a ser deletada", example = "1")
            Integer id);

    @Operation(summary = "Atualizar status da produção",
            description = "Atualiza o status de uma produção seguindo o fluxo: ESPERANDO_FAZER → EM_PRODUCAO → PRONTO_PARA_ENTREGA → FINALIZADO")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProducaoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Transição de status inválida ou produção já finalizada"),
            @ApiResponse(responseCode = "404", description = "Produção com ID fornecido não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<ProducaoResponseDTO> atualizarStatus(
            @Parameter(description = "ID da produção", example = "1")
            Integer id,
            @Parameter(description = "Novo status da produção", example = "EM_PRODUCAO")
            StatusProducao statusProducao);
}
