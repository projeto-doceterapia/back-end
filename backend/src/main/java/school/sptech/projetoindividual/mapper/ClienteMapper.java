package school.sptech.projetoindividual.mapper;

import school.sptech.projetoindividual.dto.ClienteRequestDTO;
import school.sptech.projetoindividual.dto.ClienteResponseDTO;
import school.sptech.projetoindividual.entity.Cliente;

public class ClienteMapper{

    public static Cliente toCliente(ClienteRequestDTO dto) {
        Cliente cliente = new Cliente();
        cliente.setNomeCompleto(dto.getNomeCompleto());
        cliente.setTelefone(dto.getTelefone());
        cliente.setEndereco(dto.getEndereco());
        return cliente;
    }

    public static ClienteResponseDTO toClienteResponse(Cliente cliente) {
        ClienteResponseDTO dto = new ClienteResponseDTO();
        dto.setIdCliente(cliente.getIdCliente());
        dto.setNomeCompleto(cliente.getNomeCompleto());
        dto.setTelefone(cliente.getTelefone());
        dto.setEndereco(cliente.getEndereco());
        return dto;
    }
}
