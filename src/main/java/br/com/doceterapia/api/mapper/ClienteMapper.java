package br.com.doceterapia.api.mapper;

import br.com.doceterapia.api.dto.ClienteRequestDTO;
import br.com.doceterapia.api.dto.ClienteMetricasDTO;
import br.com.doceterapia.api.dto.ClienteResponseDTO;
import br.com.doceterapia.api.dto.ClienteResumoResponseDTO;
import br.com.doceterapia.api.entity.Cliente;

import java.math.BigDecimal;

public class ClienteMapper{

    public static Cliente toCliente(ClienteRequestDTO dto) {
        Cliente cliente = new Cliente();
        cliente.setNome(dto.getNome());
        cliente.setTelefone(dto.getTelefone());
        cliente.setEndereco(dto.getEndereco());
        cliente.setCep(dto.getCep());
        cliente.setTipoPessoa(dto.getTipoPessoa());
        cliente.setClassificacaoCliente(dto.getClassificacaoCliente());
        cliente.setStatus(dto.getStatus());
        cliente.setObservacao(dto.getObservacao());
        return cliente;
    }

    public static ClienteResponseDTO toClienteResponse(Cliente cliente) {
        ClienteResponseDTO dto = new ClienteResponseDTO();
        dto.setIdCliente(cliente.getIdCliente());
        dto.setNome(cliente.getNome());
        dto.setTelefone(cliente.getTelefone());
        dto.setEndereco(cliente.getEndereco());
        dto.setCep(cliente.getCep());
        dto.setTipoPessoa(cliente.getTipoPessoa());
        dto.setClassificacaoCliente(cliente.getClassificacaoCliente());
        dto.setStatus(cliente.getStatus());
        dto.setObservacao(cliente.getObservacao());
        return dto;
    }

    public static ClienteResumoResponseDTO toClienteResumoResponse(Cliente cliente, ClienteMetricasDTO metricas,
                                                                   boolean incluirGasto) {
        long totalPedidos = metricas == null ? 0 : metricas.totalPedidos();
        BigDecimal gastoTotal = metricas == null ? BigDecimal.ZERO : metricas.gastoTotal();

        return new ClienteResumoResponseDTO(
                cliente.getIdCliente(),
                cliente.getNome(),
                cliente.getTelefone(),
                cliente.getEndereco(),
                cliente.getCep(),
                cliente.getTipoPessoa(),
                cliente.getClassificacaoCliente(),
                cliente.getStatus(),
                cliente.getObservacao(),
                totalPedidos,
                incluirGasto ? gastoTotal : null,
                metricas == null ? null : metricas.ultimoPedido());
    }
}
