package br.com.doceterapia.api.controller;

import br.com.doceterapia.api.dto.NotaFiscalStatusResponseDTO;
import br.com.doceterapia.api.service.NotaFiscalService;
import br.com.doceterapia.api.swagger.NotaFiscalControllerOpenApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("/notas-fiscais")
public class NotaFiscalController implements NotaFiscalControllerOpenApi {

    private final NotaFiscalService service;

    public NotaFiscalController(NotaFiscalService service) {
        this.service = service;
    }

    @GetMapping("/{chave}/status")
    @Override
    public ResponseEntity<NotaFiscalStatusResponseDTO> consultarStatus(@PathVariable String chave) {
        return ResponseEntity.ok(service.consultarStatus(chave));
    }
}
