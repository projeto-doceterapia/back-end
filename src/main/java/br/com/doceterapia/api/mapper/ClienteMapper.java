package br.com.doceterapia.api.mapper;

import br.com.doceterapia.api.dto.ClienteRequestDTO;
import br.com.doceterapia.api.dto.ClienteResponseDTO;
import br.com.doceterapia.api.entity.Cliente;

public class ClienteMapper{

    public static Cliente toCliente(ClienteRequestDTO dto) {
        Cliente cliente = new Cliente();
        cliente.setNome(dto.getNome());
        cliente.setTelefone(dto.getTelefone());
        cliente.setEndereco(dto.getEndereco());
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
        dto.setTipoPessoa(cliente.getTipoPessoa());
        dto.setClassificacaoCliente(cliente.getClassificacaoCliente());
        dto.setStatus(cliente.getStatus());
        dto.setObservacao(cliente.getObservacao());
        return dto;
    }
}
