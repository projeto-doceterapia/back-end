package br.com.doceterapia.api.exception;

import br.com.doceterapia.api.dto.FaltaInsumoDTO;

import java.util.List;

public class InsumosInsuficientesException extends RegraNegocioException {
    private final List<FaltaInsumoDTO> faltas;

    public InsumosInsuficientesException(String mensagem, List<FaltaInsumoDTO> faltas) {
        super(mensagem);
        this.faltas = List.copyOf(faltas);
    }

    public List<FaltaInsumoDTO> getFaltas() {
        return faltas;
    }
}
