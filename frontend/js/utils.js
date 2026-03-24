/**
 * Abre um <dialog> nativo pelo elemento.
 * @param {HTMLDialogElement} el
 */
export function abrirModalPorId(el) {
    el.showModal()
}

/**
 * Fecha um <dialog> nativo pelo elemento.
 * @param {HTMLDialogElement} el
 */
export function fecharModalPorId(el) {
    el.close()
}

/**
 * Exibe o modal de confirmação e executa a função passada ao confirmar.
 * @param {Function} fn  função a executar caso o usuário confirme
 */
export function abrirConfirmacao(fn) {
    const modal  = document.getElementById('modal_confirma')
    const botao  = document.getElementById('button_confirma')

    const novoBtn = botao.cloneNode(true)
    botao.parentNode.replaceChild(novoBtn, botao)

    novoBtn.addEventListener('click', () => {
        fn()
        fecharModalPorId(modal)
    })

    abrirModalPorId(modal)
}

/**
 * Converte uma string ISO 8601 para o formato dd/mm/aaaa.
 * @param {string} isoString
 * @returns {string}
 */
export function formatarData(isoString) {
    const [y, m, d] = isoString.split('T')[0].split('-')
    return `${d}/${m}/${y}`
}

/**
 * Formata um número como valor monetário em reais.
 * @param {number|string} valor
 * @returns {string} ex.: "R$ 150,00"
 */
export function formatarValor(valor) {
    const num = parseFloat(valor)
    if (isNaN(num)) return 'R$ —'
    return `R$ ${num.toFixed(2).replace('.', ',')}`
}

/**
 * Aplica máscara de telefone brasileiro em tempo real durante a digitação.
 * Suporta os formatos:
 *   - Celular: (XX) XXXXX-XXXX  (9 dígitos após o DDD)
 *   - Fixo:    (XX) XXXX-XXXX   (8 dígitos após o DDD)
 *
 * Uso: vincular ao evento 'input' do campo telefone.
 * @param {Event} event  evento 'input' do elemento <input>
 */
export function mascararTelefone(event) {
    const input = event.target

    const posicaoCursor = input.selectionStart
    const apenasDigitos = input.value.replace(/\D/g, '').slice(0, 11)

    let formatado = ''

    if (apenasDigitos.length === 0) {
        formatado = ''
    } else if (apenasDigitos.length <= 2) {
        formatado = `(${apenasDigitos}`
    } else if (apenasDigitos.length <= 6) {
        formatado = `(${apenasDigitos.slice(0, 2)}) ${apenasDigitos.slice(2)}`
    } else if (apenasDigitos.length <= 10) {
        formatado = `(${apenasDigitos.slice(0, 2)}) ${apenasDigitos.slice(2, 6)}-${apenasDigitos.slice(6)}`
    } else {
        formatado = `(${apenasDigitos.slice(0, 2)}) ${apenasDigitos.slice(2, 7)}-${apenasDigitos.slice(7)}`
    }

    const digitosAntesCursor = input.value.slice(0, posicaoCursor).replace(/\D/g, '').length
    input.value = formatado

    let novaPosicao = 0
    let digitosContados = 0
    while (novaPosicao < formatado.length && digitosContados < digitosAntesCursor) {
        if (/\d/.test(formatado[novaPosicao])) digitosContados++
        novaPosicao++
    }
    input.setSelectionRange(novaPosicao, novaPosicao)
}

/**
 * Aplica máscara de valor monetário em tempo real durante a digitação.
 * Formata sempre com ponto separando exatamente dois decimais.
 * Ex.: digitando "1", "15", "150", "1509" resulta em "0.01", "0.15", "1.50", "15.09"
 *
 * Uso: vincular ao evento 'input' do campo valor.
 * @param {Event} event  evento 'input' do elemento <input>
 */
export function mascararValor(event) {
    const input = event.target

    const apenasDigitos = input.value.replace(/\D/g, '').slice(0, 13)

    if (apenasDigitos.length === 0) {
        input.value = ''
        return
    }

    const inteiros = apenasDigitos.slice(0, -2).replace(/^0+(?=\d)/, '') || '0'
    const decimais = apenasDigitos.slice(-2)

    input.value = `${inteiros}.${decimais}`
}

/**
 * Inicializa os checkbox-chips: sincroniza o estado visual (.checkbox-chip--selected)
 * com o estado real do <input type="checkbox"> interno.
 * Mantida por compatibilidade com eventuais chips no formulário.
 */
export function initCheckboxChips() {
    document.querySelectorAll('.checkbox-chip').forEach(chip => {
        const input = chip.querySelector('input[type="checkbox"]')

        chip.addEventListener('click', () => {
            input.checked = !input.checked
            chip.classList.toggle('checkbox-chip--selected', input.checked)
        })
    })
}

/**
 * Reseta campos extras do formulário de cliente (chips, etc.).
 * Substitui resetarGeneros — mantém a assinatura para uso interno.
 */
export function resetarExtras() {
    document.querySelectorAll('#generos_livro .checkbox-chip').forEach(chip => {
        chip.classList.remove('checkbox-chip--selected')
        const input = chip.querySelector('input')
        if (input) input.checked = false
    })
}

/**
 * Popula um <select> com os clientes retornados pela API.
 * @param {HTMLSelectElement} selectEl
 * @param {Array}             clientes        array de clientes { idCliente, nome, telefone }
 * @param {string|null}       idSelecionado   idCliente a pré-selecionar (opcional)
 */
export function popularSelectClientes(selectEl, clientes, idSelecionado = null) {
    selectEl.innerHTML = ''

    const placeholder = document.createElement('option')
    placeholder.value = ''

    if (clientes.length === 0) {
        placeholder.textContent = 'Nenhum cliente cadastrado ainda'
    } else {
        placeholder.textContent = 'Selecione um cliente'
        clientes.forEach(cliente => {
            const option = document.createElement('option')
            option.value       = cliente.idCliente
            option.textContent = `${cliente.nomeCompleto} — ${cliente.telefone} | ${cliente.endereco}`
            if (idSelecionado && String(cliente.idCliente) === String(idSelecionado)) {
                option.selected = true
            }
            selectEl.appendChild(option)
        })
    }

    if (idSelecionado == null) selectEl.value = ''

    selectEl.insertBefore(placeholder, selectEl.firstChild)
}

/**
 * Cria e retorna um card de pedido já com o listener de edição.
 * @param {Object}   pedido    objeto com os dados do pedido
 * @param {Function} onEditar  callback chamado com o objeto ao clicar em "Editar"
 * @param {Function} onConcluir  callback chamado com o objeto ao clicar no botão de entrega
 * @returns {HTMLElement}
 */
export function criarCardPedido(pedido, onEditar, onConcluir) {
    const dataEntrega    = pedido.dataEntrega ? formatarData(pedido.dataEntrega) : '—'
    const temDescricao   = pedido.descricao && pedido.descricao.trim() !== ''
    const valorFormatado = formatarValor(pedido.valor)

    const card = document.createElement('div')
    card.classList.add('card', 'card--dynamic')
    card.dataset.pedidoId = pedido.idPedido

    card.innerHTML = `
        <div class="card__accent-bar"></div>
        <div class="card__body">
            <div class="card__header">
                <div class="card__meta">
                    <span class="card__cliente-nome">${pedido.nomeCompleto || 'Cliente'}</span>
                    <span class="card__cliente-contato">📞 ${pedido.telefone || '—'}</span>
                </div>
                <span class="card__data-badge">
                    <i class="fa-regular fa-calendar"></i>&nbsp;Entrega: ${dataEntrega}
                </span>
                <button class="card__status-btn ${pedido.statusConcluido ? 'card__status-btn--concluido' : ''}" title="Marcar pedido como entregue" aria-label="Status de entrega" aria-pressed="${pedido.statusConcluido ? 'true' : 'false'}">
                    <i class="fa-solid fa-check"></i>
                    <span>${pedido.statusConcluido ? 'Entregue' : 'Pendente'}</span>
                </button>
            </div>
            <div class="card__divider"></div>
            <div class="card__descricao ${temDescricao ? '' : 'card__descricao--vazia'}">
                ${temDescricao ? pedido.descricao : 'Nenhuma descrição para este pedido.'}
            </div>
            <div class="card__footer">
                <span class="card__valor">
                    <i class="fa-solid fa-tag"></i>&nbsp;${valorFormatado}
                </span>
                <button class="card__edit-btn">
                    <i class="fa-regular fa-pen-to-square"></i>&nbsp;Editar
                </button>
            </div>
        </div>
    `

    card.querySelector('.card__edit-btn').addEventListener('click', e => {
        e.stopPropagation()
        onEditar(pedido)
    })

    card.querySelector('.card__status-btn').addEventListener('click', async e => {
        e.stopPropagation()
        const btn = e.currentTarget
        const concluido = btn.classList.toggle('card__status-btn--concluido')
        btn.setAttribute('aria-pressed', String(concluido))
        btn.querySelector('span').textContent = concluido ? 'Entregue' : 'Pendente'

        try {
            await onConcluir(pedido.idPedido, concluido)
        } catch (error) {
            btn.classList.toggle('card__status-btn--concluido', !concluido)
            btn.setAttribute('aria-pressed', String(!concluido))
            btn.querySelector('span').textContent = !concluido ? 'Entregue' : 'Pendente'
            console.error('#ERRO ao atualizar status do pedido:', error)
        }
    })

    return card
}
