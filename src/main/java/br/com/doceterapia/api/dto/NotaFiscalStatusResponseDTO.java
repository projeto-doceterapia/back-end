package br.com.doceterapia.api.dto;

import br.com.doceterapia.api.enums.StatusNotaFiscal;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Status de registro de uma nota fiscal no estoque")
public class NotaFiscalStatusResponseDTO {

    @Schema(description = "Status da nota fiscal", example = "REGISTRADA")
    private StatusNotaFiscal status;

    @Schema(description = "IDs dos insumos já registrados para a chave", example = "[1, 2]")
    private List<Integer> insumoIds;

    public NotaFiscalStatusResponseDTO() {
    }

    public NotaFiscalStatusResponseDTO(StatusNotaFiscal status, List<Integer> insumoIds) {
        this.status = status;
        this.insumoIds = insumoIds;
    }

    public StatusNotaFiscal getStatus() {
        return status;
    }

    public void setStatus(StatusNotaFiscal status) {
        this.status = status;
    }

    public List<Integer> getInsumoIds() {
        return insumoIds;
    }

    public void setInsumoIds(List<Integer> insumoIds) {
        this.insumoIds = insumoIds;
    }
}
