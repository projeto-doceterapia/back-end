package br.com.doceterapia.api.mapper;

import br.com.doceterapia.api.dto.PagamentoRequestDTO;
import br.com.doceterapia.api.dto.PagamentoResponseDTO;
import br.com.doceterapia.api.entity.Pagamento;
import org.springframework.stereotype.Component;

@Component
public class PagamentoMapper {

    public Pagamento toEntity(PagamentoRequestDTO dto) {
        Pagamento pagamento = new Pagamento();
        pagamento.setFormaPagamento(dto.getFormaPagamento());
        pagamento.setDataPagamentoSinal(dto.getDataPagamentoSinal());
        pagamento.setDataPagamentoRestante(dto.getDataPagamentoRestante());
        return pagamento;
    }

    public PagamentoResponseDTO toResponseDTO(Pagamento pagamento) {
        PagamentoResponseDTO response = new PagamentoResponseDTO();
        response.setIdPagamento(pagamento.getIdPagamento());
        response.setPedidoId(pagamento.getPedido().getIdPedido());
        response.setValorTotal(pagamento.getValorTotal());
        response.setValorSinal(pagamento.getValorSinal());
        response.setValorRestante(pagamento.getValorRestante());
        response.setFormaPagamento(pagamento.getFormaPagamento());
        response.setStatusPagamento(pagamento.getStatusPagamento());
        response.setDataPagamentoSinal(pagamento.getDataPagamentoSinal());
        response.setDataPagamentoRestante(pagamento.getDataPagamentoRestante());
        return response;
    }
}
