package br.com.doceterapia.api.service;

import br.com.doceterapia.api.dto.NotaFiscalStatusResponseDTO;
import br.com.doceterapia.api.enums.StatusNotaFiscal;
import br.com.doceterapia.api.exception.ChaveNotaFiscalInvalidaException;
import br.com.doceterapia.api.repository.MovimentacaoEstoqueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotaFiscalService {

    private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;
    private final ChaveNotaFiscalValidator chaveNotaFiscalValidator;

    public NotaFiscalService(MovimentacaoEstoqueRepository movimentacaoEstoqueRepository,
                             ChaveNotaFiscalValidator chaveNotaFiscalValidator) {
        this.movimentacaoEstoqueRepository = movimentacaoEstoqueRepository;
        this.chaveNotaFiscalValidator = chaveNotaFiscalValidator;
    }

    @Transactional(readOnly = true)
    public NotaFiscalStatusResponseDTO consultarStatus(String chave) {
        if (!chaveNotaFiscalValidator.isValida(chave)) {
            throw new ChaveNotaFiscalInvalidaException();
        }

        List<Integer> insumoIds = movimentacaoEstoqueRepository.findInsumoIdsByChaveNota(chave);
        StatusNotaFiscal status = insumoIds.isEmpty() ? StatusNotaFiscal.INEXISTENTE : StatusNotaFiscal.REGISTRADA;
        return new NotaFiscalStatusResponseDTO(status, insumoIds);
    }
}
