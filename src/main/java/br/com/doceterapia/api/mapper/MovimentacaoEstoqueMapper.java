package br.com.doceterapia.api.mapper;

import br.com.doceterapia.api.dto.MovimentacaoEstoqueRequestDTO;
import br.com.doceterapia.api.dto.MovimentacaoEstoqueResponseDTO;
import br.com.doceterapia.api.entity.MovimentacaoEstoque;
import br.com.doceterapia.api.service.MovimentacaoEstoqueService.EntradaCompra;
import org.springframework.stereotype.Component;

@Component
public class MovimentacaoEstoqueMapper {

    public EntradaCompra toComando(MovimentacaoEstoqueRequestDTO request, String chaveIdempotencia) {
        return new EntradaCompra(
                request.getInsumoId(),
                request.getQuantidadeInformada(),
                request.getUnidadeInformada(),
                request.getValorLiquido(),
                request.getOrigem(),
                request.getChaveNota(),
                request.getFluxoId(),
                chaveIdempotencia,
                request.getOcorridoEm());
    }

    public MovimentacaoEstoqueResponseDTO toResponseDTO(MovimentacaoEstoque entity, boolean duplicado) {
        MovimentacaoEstoqueResponseDTO dto = new MovimentacaoEstoqueResponseDTO();
        dto.setIdMovimentacao(entity.getIdMovimentacao());
        if (entity.getInsumo() != null) {
            dto.setInsumoId(entity.getInsumo().getIdInsumo());
            dto.setSaldoAtual(entity.getInsumo().getQuantidadeAtual());
        }
        dto.setTipo(entity.getTipo());
        dto.setQuantidade(entity.getQuantidade());
        dto.setQuantidadeInformada(entity.getQuantidadeInformada());
        dto.setUnidadeInformada(entity.getUnidadeInformada());
        dto.setCustoUnitario(entity.getCustoUnitario());
        dto.setOrigem(entity.getOrigem());
        dto.setChaveNota(entity.getChaveNota());
        dto.setFluxoId(entity.getFluxoId());
        dto.setDuplicado(duplicado);
        dto.setOcorridoEm(entity.getOcorridoEm());
        return dto;
    }
}
