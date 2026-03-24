import { getClientes, getPedidos, postCliente, postPedido, putPedido, deletePedido, patchStatusPedido } from './services.js'
import {
    abrirModalPorId,
    fecharModalPorId,
    abrirConfirmacao,
    initCheckboxChips,
    resetarExtras,
    popularSelectClientes,
    criarCardPedido,
    mascararTelefone,
    mascararValor
} from './utils.js'

/* ── Estado interno ── */
let _editandoIdPedido  = null
let _editandoIdCliente = null

/* ----------------------------------------------------------------
   FEED — renderizar pedidos
---------------------------------------------------------------- */
async function exibirPedidos() {
    const feed         = document.getElementById('feed')
    const empty        = document.getElementById('feed__empty')
    const disconnected = document.getElementById('feed__disconnected')

    feed.querySelectorAll('.card--dynamic').forEach(c => c.remove())

    try {
        const pedidos = await getPedidos()

        if (pedidos.length === 0) {
            disconnected.style.display = 'none'
            empty.style.display = 'flex'
            return
        }

        empty.style.display = 'none'
        disconnected.style.display = 'none'
        pedidos.forEach(pedido => {
            feed.appendChild(criarCardPedido(pedido, abrirModalEdicao, patchStatusPedido))
        })

    } catch (error) {
        empty.style.display = 'none'
        disconnected.style.display = 'flex'
        console.error('#ERRO ao carregar pedidos:', error)
    }
}

/* ----------------------------------------------------------------
   MODAL DE FORMULÁRIO (cadastrar cliente / registrar pedido)
---------------------------------------------------------------- */
async function abrirModal() {
    document.getElementById('modal_overlay').classList.add('modal-overlay--open')
    await carregarSelectClientes(document.getElementById('ipt_livro'), null)
}

function fecharModal() {
    document.getElementById('modal_overlay').classList.remove('modal-overlay--open')
    resetarExtras()
}

/* ── Tabs ── */
function ativarTab(tabId) {
    const map = { tab_livro: 'secao_livro', tab_leitura: 'secao_leitura' }
    Object.keys(map).forEach(id => {
        const ativo = id === tabId
        document.getElementById(id).classList.toggle('form-steps__btn--active', ativo)
        document.getElementById(id).setAttribute('aria-selected', String(ativo))
        document.getElementById(map[id]).classList.toggle('form-section--active', ativo)
    })
}

/* ── Popula select de clientes ── */
async function carregarSelectClientes(selectEl, idSelecionado) {
    try {
        const clientes = await getClientes()
        popularSelectClientes(selectEl, clientes, idSelecionado)
    } catch (error) {
        console.error('#ERRO ao carregar clientes para select:', error)
    }
}

/* ----------------------------------------------------------------
   CADASTRAR CLIENTE
---------------------------------------------------------------- */
async function cadastrarCliente() {
    const nome     = document.getElementById('ipt_nome').value.trim()
    const telefone = document.getElementById('ipt_autor').value.trim()
    const endereco = document.getElementById('ipt_paginas').value.trim()

    if (!nome || !telefone || !endereco) {
        alert('Preencha todos os campos obrigatórios antes de cadastrar o cliente!')
        return
    }

    try {
        await postCliente({ nome, telefone, endereco })
        alert('Cliente cadastrado com sucesso!')

        ;['ipt_nome', 'ipt_autor', 'ipt_paginas'].forEach(id => {
            document.getElementById(id).value = ''
        })
        resetarExtras()
        fecharModal()
        exibirPedidos()

    } catch (error) {
        console.error('#ERRO ao cadastrar cliente:', error)
        alert('Não foi possível cadastrar o cliente. Tente novamente.')
    }
}

/* ----------------------------------------------------------------
   REGISTRAR PEDIDO
---------------------------------------------------------------- */
async function registrarPedido() {
    const idCliente   = document.getElementById('ipt_livro').value
    const descricao   = document.getElementById('ipt_comentario').value.trim()
    const dataEntrega = document.getElementById('ipt_dataOutraDate').value
    const valor       = document.getElementById('ipt_paginasLidas').value

    if (!idCliente)   { alert('Selecione um cliente antes de registrar o pedido.'); return }
    if (!descricao)   { alert('Descreva o pedido antes de salvar.'); return }
    if (!dataEntrega) { alert('Informe a data de entrega do pedido.'); return }
    if (!valor)       { alert('Informe o valor do pedido.'); return }

    try {
        await postPedido({
            idCliente,
            descricao,
            dataEntrega: new Date(dataEntrega).toISOString(),
            valor: Number(valor)
        })
        alert('Pedido registrado com sucesso!')

        ;['ipt_livro', 'ipt_comentario', 'ipt_dataOutraDate', 'ipt_paginasLidas'].forEach(id => {
            document.getElementById(id).value = ''
        })
        document.getElementById('char_count').textContent = '0'
        fecharModal()
        exibirPedidos()

    } catch (error) {
        console.error('#ERRO ao registrar pedido:', error)
        alert('Não foi possível registrar o pedido. Tente novamente.')
    }
}

/* ----------------------------------------------------------------
   MODAL DE EDIÇÃO
---------------------------------------------------------------- */
async function abrirModalEdicao(pedido) {
    _editandoIdPedido  = pedido.idPedido
    _editandoIdCliente = pedido.idCliente

    const selectClienteEdit = document.getElementById('edit_livro')
    await carregarSelectClientes(selectClienteEdit, pedido.idCliente)

    const dataFormatada = pedido.dataEntrega ? pedido.dataEntrega.substring(0, 10) : ''
    document.getElementById('edit_dataOutraDate').value    = dataFormatada
    document.getElementById('edit_paginasLidas').value     = pedido.valor     || ''
    document.getElementById('edit_comentario').value       = pedido.descricao || ''
    document.getElementById('edit_char_count').textContent = String((pedido.descricao || '').length)

    document.getElementById('edit_overlay').classList.add('edit-overlay--open')
}

function fecharModalEdicao() {
    document.getElementById('edit_overlay').classList.remove('edit-overlay--open')
    _editandoIdPedido  = null
    _editandoIdCliente = null
}

async function salvarEdicaoPedido() {
    const idCliente   = document.getElementById('edit_livro').value
    const valor       = document.getElementById('edit_paginasLidas').value
    const dataEntrega = document.getElementById('edit_dataOutraDate').value
    const descricao   = document.getElementById('edit_comentario').value.trim()

    if (!idCliente)                  { alert('Selecione o cliente vinculado ao pedido.'); return }
    if (!valor || Number(valor) < 0) { alert('Informe o valor do pedido.'); return }
    if (!dataEntrega)                { alert('Informe a data de entrega do pedido.'); return }

    try {
        await putPedido({
            idPedido:  _editandoIdPedido,
            idCliente,
            descricao,
            dataEntrega: new Date(dataEntrega).toISOString(),
            valor: Number(valor)
        })
        alert('Pedido editado com sucesso!')
        fecharModalEdicao()
        exibirPedidos()

    } catch (error) {
        console.error('#ERRO ao editar pedido:', error)
        alert('Não foi possível salvar as alterações. Tente novamente.')
    }
}

async function confirmarDeletarPedido() {
    try {
        await deletePedido(_editandoIdPedido, _editandoIdCliente)
        alert('Pedido excluído com sucesso!')
        fecharModalEdicao()
        exibirPedidos()
    } catch (error) {
        console.error('#ERRO ao deletar pedido:', error)
        alert('Não foi possível excluir o pedido. Tente novamente.')
    }
}

/* ================================================================
   INIT — DOMContentLoaded
================================================================ */
document.addEventListener('DOMContentLoaded', () => {

    /* ── Setup inicial ── */
    initCheckboxChips()
    exibirPedidos()

    /* ── FAB ── */
    document.getElementById('btn_abrirFormulario')
        .addEventListener('click', abrirModal)

    /* ── Form modal — fechar ── */
    document.getElementById('btn_fecharModal')
        .addEventListener('click', fecharModal)
    document.getElementById('btn_cancelarLivro')
        .addEventListener('click', fecharModal)
    document.getElementById('btn_cancelarLeitura')
        .addEventListener('click', fecharModal)
    document.getElementById('modal_overlay')
        .addEventListener('click', e => {
            if (e.target === document.getElementById('modal_overlay')) fecharModal()
        })

    /* ── Tabs ── */
    document.getElementById('tab_livro')
        .addEventListener('click', () => ativarTab('tab_livro'))
    document.getElementById('tab_leitura')
        .addEventListener('click', async () => {
            ativarTab('tab_leitura')
            await carregarSelectClientes(document.getElementById('ipt_livro'), null)
        })

    /* ── Submits ── */
    document.getElementById('ipt_enviarLivro')
        .addEventListener('click', cadastrarCliente)
    document.getElementById('ipt_enviarLeitura')
        .addEventListener('click', registrarPedido)

    /* ── Char counter — form pedido ── */
    document.getElementById('ipt_comentario')
        .addEventListener('input', function () {
            document.getElementById('char_count').textContent = this.value.length
        })

    /* ── Edit modal — fechar ── */
    document.getElementById('btn_fecharEdit')
        .addEventListener('click', fecharModalEdicao)
    document.getElementById('btn_cancelarEdit')
        .addEventListener('click', fecharModalEdicao)
    document.getElementById('edit_overlay')
        .addEventListener('click', e => {
            if (e.target === document.getElementById('edit_overlay')) fecharModalEdicao()
        })

    /* ── Máscara de telefone — cadastro de cliente ── */
    document.getElementById('ipt_autor')
        .addEventListener('input', mascararTelefone)

    /* ── Máscara de valor — form novo pedido ── */
    document.getElementById('ipt_paginasLidas')
        .addEventListener('input', mascararValor)

    /* ── Máscara de valor — modal de edição ── */
    document.getElementById('edit_paginasLidas')
        .addEventListener('input', mascararValor)
    
        /* ── Edit modal — ações ── */
    document.getElementById('btn_salvarEdit')
        .addEventListener('click', salvarEdicaoPedido)
    document.getElementById('btn_deletarLeitura')
        .addEventListener('click', () => abrirConfirmacao(confirmarDeletarPedido))

    /* ── Char counter — edit modal ── */
    document.getElementById('edit_comentario')
        .addEventListener('input', function () {
            document.getElementById('edit_char_count').textContent = this.value.length
        })

    /* ── Help dialog ── */
    document.getElementById('btn_ajuda')
        .addEventListener('click', () => abrirModalPorId(document.getElementById('agenda_help')))
    document.getElementById('btn_fecharAjuda')
        .addEventListener('click', () => fecharModalPorId(document.getElementById('agenda_help')))

    /* ── Confirm dialog — cancelar ── */
    document.getElementById('btn_cancelarConfirma')
        .addEventListener('click', () => fecharModalPorId(document.getElementById('modal_confirma')))

    /* ── Card mock — botão editar ── */
    const btnEditMock = document.getElementById('btn_editMock')
    if (btnEditMock) {
        btnEditMock.addEventListener('click', () => {
            abrirModalEdicao({
                idPedido:    'mock',
                idCliente:   '',
                dataEntrega: new Date().toISOString(),
                valor:       280,
                descricao:   '2 bolos de chocolate belga com ganache, decoração floral em chantilly. Para festa de 30 pessoas.',
                nomeCliente: 'Ana Lima',
                telefone:    '(11) 99876-5432'
            })
        })
    }
})
