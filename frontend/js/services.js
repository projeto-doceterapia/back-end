import { ENV } from "./config.js"

/**
 * Busca todos os clientes.
 * @returns {Promise<Array>} lista de clientes
 */
export async function getClientes() {
    const url = `${ENV.API_URL}/clientes`

    try {
        const response = await fetch(url, {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' },
            cache: 'no-store'
        })

        if (response.status === 204) return []

        if (!response.ok) {
            throw new Error(`Erro ${response.status} ao listar clientes`)
        }

        return await response.json()

    } catch (error) {
        throw error
    }
}

/**
 * Cadastra um novo cliente.
 * @param {{ nome: string, telefone: string, endereco: string }} payload
 */
export async function postCliente({ nome, telefone, endereco }) {
    const url = `${ENV.API_URL}/clientes`

    try {
        const response = await fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ 
                nomeCompleto: nome,
                telefone: telefone,
                endereco: endereco })
        })

        if (!response.ok) {
            throw new Error(`Erro ${response.status} ao cadastrar cliente`)
        }

        return true

    } catch (error) {
        throw error
    }
}

/**
 * Busca todos os pedidos.
 * @returns {Promise<Array>} lista de pedidos
 */
export async function getPedidos() {
    const url = `${ENV.API_URL}/pedidos`

    try {
        const response = await fetch(url, {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' },
            cache: 'no-store'
        })

        if (response.status === 204) return []

        if (!response.ok) {
            throw new Error(`Erro ${response.status} ao listar pedidos`)
        }

        return await response.json()

    } catch (error) {
        throw error
    }
}

/**
 * Registra um novo pedido.
 * @param {{ idCliente: string, descricao: string, dataEntrega: string, valor: number }} payload
 */
export async function postPedido({ idCliente, descricao, dataEntrega, valor }) {
    const url = `${ENV.API_URL}/pedidos`

    try {
        const response = await fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ 
                fkCliente: idCliente,
                descricao: descricao,
                dataEntrega: dataEntrega,
                valor: valor })
        })

        if (!response.ok) {
            throw new Error(`Erro ${response.status} ao registrar pedido`)
        }

        return true

    } catch (error) {
        throw error
    }
}

/**
 * Edita um pedido existente.
 * @param {{ idPedido: string, idCliente: string, descricao: string, dataEntrega: string, valor: number }} payload
 */
export async function putPedido({ idPedido, idCliente, descricao, dataEntrega, valor }) {
    const url = `${ENV.API_URL}/pedidos/${idPedido}`

    try {
        const response = await fetch(url, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ 
                fkCliente: idCliente,
                descricao: descricao,
                dataEntrega: dataEntrega,
                valor: valor })
        })

        if (!response.ok) {
            throw new Error(`Erro ${response.status} ao editar pedido`)
        }

        return true

    } catch (error) {
        throw error
    }
}

/**
 * Exclui um pedido.
 * @param {string} idPedido
 * @param {string} idCliente
 */
export async function deletePedido(idPedido, idCliente) {
    const url = `${ENV.API_URL}/pedidos/${idPedido}`

    try {
        const response = await fetch(url, {
            method: 'DELETE',
            headers: { 'Content-Type': 'application/json' },
        })

        if (!response.ok) {
            throw new Error(`Erro ${response.status} ao deletar pedido`)
        }

        return true

    } catch (error) {
        throw error
    }
}

/**
 * Atualiza o status de conclusão de um pedido.
 * @param {string}  idPedido
 * @param {boolean} concluido
 */
export async function patchStatusPedido(idPedido, concluido) {
    const url = `${ENV.API_URL}/pedidos/status?id=${idPedido}&status=${concluido}`

    try {
        const response = await fetch(url, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json' },
            params: JSON.stringify({
                id: idPedido,
                status: concluido })
        })

        if (!response.ok) {
            throw new Error(`Erro ${response.status} ao atualizar status do pedido`)
        }

        return true

    } catch (error) {
        throw error
    }
}
