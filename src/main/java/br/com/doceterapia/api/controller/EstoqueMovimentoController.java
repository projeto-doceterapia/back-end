package br.com.doceterapia.api.controller;

import br.com.doceterapia.api.dto.MovimentacaoEstoqueRequestDTO;
import br.com.doceterapia.api.dto.MovimentacaoEstoqueResponseDTO;
import br.com.doceterapia.api.exception.IdempotencyKeyAusenteException;
import br.com.doceterapia.api.mapper.MovimentacaoEstoqueMapper;
import br.com.doceterapia.api.service.MovimentacaoEstoqueService;
import br.com.doceterapia.api.service.MovimentacaoEstoqueService.EntradaCompra;
import br.com.doceterapia.api.service.MovimentacaoEstoqueService.ResultadoMovimentacao;
import br.com.doceterapia.api.swagger.EstoqueMovimentoControllerOpenApi;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/estoque/movimentos")
public class EstoqueMovimentoController implements EstoqueMovimentoControllerOpenApi {

    private final MovimentacaoEstoqueService service;
    private final MovimentacaoEstoqueMapper mapper;

    public EstoqueMovimentoController(MovimentacaoEstoqueService service, MovimentacaoEstoqueMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    @Override
    public ResponseEntity<MovimentacaoEstoqueResponseDTO> registrarMovimento(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody MovimentacaoEstoqueRequestDTO request) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IdempotencyKeyAusenteException();
        }

        EntradaCompra comando = mapper.toComando(request, idempotencyKey);
        ResultadoMovimentacao resultado = service.registrarEntradaCompra(comando);

        MovimentacaoEstoqueResponseDTO response =
                mapper.toResponseDTO(resultado.movimentacao(), resultado.duplicado());
        HttpStatus status = resultado.duplicado() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping
    @Override
    public ResponseEntity<List<MovimentacaoEstoqueResponseDTO>> listarMovimentos(
            @RequestParam Integer insumoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime ate) {
        List<MovimentacaoEstoqueResponseDTO> movimentos = service.listarHistorico(insumoId, de, ate).stream()
                .map(movimentacao -> mapper.toResponseDTO(movimentacao, false))
                .toList();
        return ResponseEntity.ok(movimentos);
    }
}
