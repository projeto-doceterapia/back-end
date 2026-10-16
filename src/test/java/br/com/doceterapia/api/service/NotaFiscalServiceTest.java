package br.com.doceterapia.api.service;

import br.com.doceterapia.api.dto.NotaFiscalStatusResponseDTO;
import br.com.doceterapia.api.enums.StatusNotaFiscal;
import br.com.doceterapia.api.exception.ChaveNotaFiscalInvalidaException;
import br.com.doceterapia.api.repository.MovimentacaoEstoqueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para NotaFiscalService")
class NotaFiscalServiceTest {

    private static final String CHAVE_VALIDA = "35200114200166000187650010000000181000000012";

    @Mock
    private MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

    private NotaFiscalService service;

    @BeforeEach
    void setUp() {
        service = new NotaFiscalService(movimentacaoEstoqueRepository, new ChaveNotaFiscalValidator());
    }

    @Test
    @DisplayName("Chave válida nunca usada retorna INEXISTENTE com lista vazia")
    void inexistente() {
        when(movimentacaoEstoqueRepository.findInsumoIdsByChaveNota(CHAVE_VALIDA)).thenReturn(List.of());

        NotaFiscalStatusResponseDTO resposta = service.consultarStatus(CHAVE_VALIDA);

        assertEquals(StatusNotaFiscal.INEXISTENTE, resposta.getStatus());
        assertTrue(resposta.getInsumoIds().isEmpty());
    }

    @Test
    @DisplayName("Chave com movimentações retorna REGISTRADA com os ids dos insumos")
    void registrada() {
        when(movimentacaoEstoqueRepository.findInsumoIdsByChaveNota(CHAVE_VALIDA)).thenReturn(List.of(1, 2));

        NotaFiscalStatusResponseDTO resposta = service.consultarStatus(CHAVE_VALIDA);

        assertEquals(StatusNotaFiscal.REGISTRADA, resposta.getStatus());
        assertEquals(List.of(1, 2), resposta.getInsumoIds());
    }

    @Test
    @DisplayName("Chave inválida lança 422 sem consultar o repositório")
    void chaveInvalida() {
        assertThrows(ChaveNotaFiscalInvalidaException.class,
                () -> service.consultarStatus("chave-invalida"));

        verify(movimentacaoEstoqueRepository, never()).findInsumoIdsByChaveNota(org.mockito.ArgumentMatchers.any());
    }
}
