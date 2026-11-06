"""Checks shared by the --verificar option and the unit tests. Each returns its violations."""

import re
from collections import Counter
from decimal import Decimal

import catalogo
import massa as modelo
from massa import Massa

REGEX_TELEFONE = re.compile(r"^\+55 11 90000-\d{4}$")
REGEX_CEP = re.compile(r"^00000\d{3}$")

PAGAMENTO_POR_FASE = {
    "AGUARDANDO_SINAL": {"AGUARDANDO_SINAL"},
    "ESPERANDO_FAZER": {"AGUARDANDO_PAGAMENTO"},
    "EM_PRODUCAO": {"AGUARDANDO_PAGAMENTO"},
    "AGUARDANDO_ENTREGA": {"AGUARDANDO_PAGAMENTO", "PAGO_INTEGRAL"},
    "ENTREGUE": {"AGUARDANDO_PAGAMENTO", "PAGO_INTEGRAL"},
}


def total_sinal_restante(m: Massa) -> list[str]:
    erros = []
    soma: dict[int, Decimal] = {}
    for item in m.itens:
        if item.valor_total != modelo.centavos(item.quantidade * item.valor_unitario):
            erros.append(f"item {item.id}: valor_total differs from quantity times unit value")
        soma[item.fk_pedido] = soma.get(item.fk_pedido, Decimal("0")) + item.valor_total
    for pagamento in m.pagamentos:
        pedido = pagamento.fk_pedido
        if soma.get(pedido) != pagamento.valor_total:
            erros.append(f"pedido {pedido}: total differs from the sum of its items")
        if pagamento.valor_sinal != modelo.sinal_de(pagamento.valor_total):
            erros.append(f"pedido {pedido}: deposit is not 50% rounded half up")
        if pagamento.valor_sinal + pagamento.valor_restante != pagamento.valor_total:
            erros.append(f"pedido {pedido}: deposit plus remainder differs from the total")
    return erros


def todos_os_status(m: Massa) -> list[str]:
    presentes = {p.status_pedido for p in m.pedidos}
    return [f"no order with status {s}" for s in modelo.STATUS_PEDIDO if s not in presentes]


def pagamento_e_producao_por_status(m: Massa) -> list[str]:
    erros = []
    pagamentos = {p.fk_pedido: p for p in m.pagamentos}
    producoes = {p.fk_pedido: p for p in m.producoes}
    for pedido in m.pedidos:
        pagamento, producao = pagamentos.get(pedido.id), producoes.get(pedido.id)
        esperado_tipo = "ORCAMENTO" if pedido.fase == "ORCAMENTO" else "PEDIDO_CONFIRMADO"
        if pedido.tipo_pedido != esperado_tipo:
            erros.append(f"pedido {pedido.id}: tipo {pedido.tipo_pedido} in phase {pedido.fase}")
        if pedido.fase == "ORCAMENTO":
            if pagamento or producao:
                erros.append(f"pedido {pedido.id}: quote with payment or production")
            continue
        if pagamento is None:
            erros.append(f"pedido {pedido.id}: confirmed without payment")
            continue
        if pagamento.status_pagamento not in PAGAMENTO_POR_FASE[pedido.fase]:
            erros.append(f"pedido {pedido.id}: payment {pagamento.status_pagamento} in phase {pedido.fase}")
        sinal_pago = pedido.fase in modelo.FASES_COM_SINAL
        if sinal_pago != (pagamento.forma_pagamento_sinal is not None) or \
                sinal_pago != (pagamento.dia_pagamento_sinal is not None):
            erros.append(f"pedido {pedido.id}: deposit date and method disagree with the phase")
        restante_pago = pagamento.status_pagamento == "PAGO_INTEGRAL"
        if restante_pago != (pagamento.forma_pagamento_restante is not None) or \
                restante_pago != (pagamento.dia_pagamento_restante is not None):
            erros.append(f"pedido {pedido.id}: remainder date and method disagree with the status")
        esperado_producao = modelo.STATUS_PRODUCAO_POR_FASE.get(pedido.fase)
        obtido_producao = producao.status_producao if producao else None
        if esperado_producao != obtido_producao:
            erros.append(f"pedido {pedido.id}: production {obtido_producao} in phase {pedido.fase}")
    return erros


def ficha_unidades_compativeis(m: Massa) -> list[str]:
    insumos = {i.id: i for i in m.insumos}
    return [
        f"ficha {f.id}: {f.unidade} is incompatible with {insumos[f.fk_insumo].unidade}"
        for f in m.fichas if not modelo.compativeis(f.unidade, insumos[f.fk_insumo].unidade)
    ]


def embalagem_unidade(m: Massa) -> list[str]:
    insumos = {i.id: i for i in m.insumos}
    com_embalagem = {
        f.fk_produto for f in m.fichas
        if f.unidade == "UNIDADE" and insumos[f.fk_insumo].fk_categoria_insumo == len(catalogo.CATEGORIAS_INSUMO_DEMO)
    }
    return [f"produto {p.id}: recipe without packaging" for p in m.produtos if p.id not in com_embalagem]


def preco_segue_formula(m: Massa) -> list[str]:
    insumos = {i.id: i for i in m.insumos}
    linhas: dict[int, list] = {}
    for linha in m.fichas:
        linhas.setdefault(linha.fk_produto, []).append(linha)
    erros = []
    for produto in m.produtos:
        custo = modelo.custo_do_produto(linhas[produto.id], insumos, catalogo.PERCENTUAL_INDIRETOS)
        if custo != produto.custo_calculado or produto.preco_atual != modelo.preco_sugerido(custo, produto.margem_lucro):
            erros.append(f"produto {produto.id}: price does not follow the markup formula")
    return erros


def saldo_igual_movimentacoes(m: Massa) -> list[str]:
    soma: dict[int, Decimal] = {}
    for movimento in m.movimentacoes:
        soma[movimento.fk_insumo] = soma.get(movimento.fk_insumo, Decimal("0")) + movimento.quantidade
    erros = [
        f"insumo {i.id}: balance {i.quantidade_atual} differs from movements {soma.get(i.id)}"
        for i in m.insumos if soma.get(i.id) != i.quantidade_atual
    ]
    erros += [f"insumo {i.id}: negative balance" for i in m.insumos if i.quantidade_atual < 0]
    return erros


def estoque_coerente(m: Massa) -> list[str]:
    erros = []
    abaixo = [i for i in m.insumos if i.quantidade_atual < i.quantidade_minima]
    if len(abaixo) < 3:
        erros.append(f"only {len(abaixo)} supplies below the minimum")
    ultima_compra: dict[int, tuple[int, int, Decimal]] = {}
    for movimento in m.movimentacoes:
        if movimento.tipo == "ENTRADA_COMPRA":
            if movimento.custo_unitario is None:
                erros.append(f"movimento {movimento.id}: purchase without unit cost")
                continue
            chave = (movimento.dia_ocorrido, movimento.id, movimento.custo_unitario)
            if movimento.fk_insumo not in ultima_compra or chave[:2] > ultima_compra[movimento.fk_insumo][:2]:
                ultima_compra[movimento.fk_insumo] = chave
    for insumo in m.insumos:
        compra = ultima_compra.get(insumo.id)
        if compra is None:
            erros.append(f"insumo {insumo.id}: no purchase")
        elif insumo.custo_unitario != compra[2]:
            erros.append(f"insumo {insumo.id}: cost differs from the last purchase")
    return erros


def movimentacoes_coerentes(m: Massa) -> list[str]:
    erros = []
    chaves = Counter(mov.chave_idempotencia for mov in m.movimentacoes)
    erros += [f"idempotency key {c} repeated" for c, n in chaves.items() if n > 1 or c is None]
    tem_saida = False
    for mov in m.movimentacoes:
        if mov.tipo == "ENTRADA_COMPRA":
            esperado = modelo.hash_entrada(
                mov.fk_insumo, mov.quantidade_informada, mov.unidade_informada, mov.valor_liquido, mov.fluxo_id)
        elif mov.tipo == "SAIDA_PRODUCAO":
            tem_saida = True
            esperado = modelo.hash_saida_producao(mov.fk_pedido, mov.fk_insumo, mov.quantidade)
        else:
            esperado = modelo.hash_manual(mov.fk_insumo, mov.tipo, mov.quantidade)
        if mov.hash_conteudo != esperado:
            erros.append(f"movimento {mov.id}: content hash differs from the service canonical form")
    if tem_saida and not m.com_saida_producao:
        erros.append("production withdrawal emitted without the switch")
    return erros


def cancelamentos_coerentes(m: Massa) -> list[str]:
    erros = []
    pedidos_cancelados = {p.id for p in m.pedidos if p.status_pedido == "CANCELADO"}
    pagamentos = {p.fk_pedido: p for p in m.pagamentos}
    com_cancelamento = {c.fk_pedido for c in m.cancelamentos}
    if pedidos_cancelados != com_cancelamento:
        erros.append("cancelled orders and cancellation rows differ")
    if {c.tipo_retorno for c in m.cancelamentos} != set(modelo.TIPOS_RETORNO):
        erros.append("not every return type is present")
    for cancelamento in m.cancelamentos:
        pagamento = pagamentos.get(cancelamento.fk_pedido)
        pago = Decimal("0.00")
        if pagamento is not None and pagamento.dia_pagamento_sinal is not None:
            pago = pagamento.valor_sinal
        if not Decimal("0") <= cancelamento.valor_retorno <= pago:
            erros.append(f"cancelamento {cancelamento.id}: return {cancelamento.valor_retorno} outside 0 to {pago}")
        if cancelamento.tipo_retorno == "SEM_DEVOLUCAO" and cancelamento.valor_retorno != 0:
            erros.append(f"cancelamento {cancelamento.id}: no refund with a value")
    return erros


def faixas_ficticias(m: Massa) -> list[str]:
    erros = []
    telefones = Counter(c.telefone for c in m.clientes)
    erros += [f"phone {t} repeated" for t, n in telefones.items() if n > 1]
    erros += [f"cliente {c.id}: phone outside the fictitious range" for c in m.clientes
              if not REGEX_TELEFONE.match(c.telefone)]
    erros += [f"cliente {c.id}: zip code outside the fictitious range" for c in m.clientes
              if not REGEX_CEP.match(c.cep)]
    erros += [f"pedido {p.id}: zip code outside the fictitious range" for p in m.pedidos
              if p.cep is not None and not REGEX_CEP.match(p.cep)]
    erros += [f"insumo {i.id}: brand outside the fictitious list" for i in m.insumos
              if i.marca not in catalogo.MARCAS]
    if m.tamanho == "demo":
        nomes = {nome for nome, _, _ in catalogo.CLIENTES_DEMO}
        erros += [f"cliente {c.id}: name outside the fictitious list" for c in m.clientes if c.nome not in nomes]
    return erros


def datas_coerentes(m: Massa) -> list[str]:
    """Nothing that already happened may be dated in the future."""
    erros = []
    for pedido in m.pedidos:
        if pedido.dia_criacao > 0 or (pedido.dia_confirmacao or 0) > 0:
            erros.append(f"pedido {pedido.id}: created or confirmed in the future")
    for pagamento in m.pagamentos:
        if (pagamento.dia_pagamento_sinal or 0) > 0 or (pagamento.dia_pagamento_restante or 0) > 0:
            erros.append(f"pagamento {pagamento.id}: paid in the future")
    for producao in m.producoes:
        if (producao.dia_inicio or 0) > 0 or (producao.dia_finalizacao or 0) > 0:
            erros.append(f"producao {producao.id}: started or finished in the future")
    erros += [f"cancelamento {c.id}: in the future" for c in m.cancelamentos if c.dia_cancelamento > 0]
    erros += [f"movimento {mov.id}: in the future" for mov in m.movimentacoes if mov.dia_ocorrido > 0]
    return erros


def agenda_do_demo(m: Massa) -> list[str]:
    por_dia = Counter(p.dia_entrega for p in m.pedidos if p.status_pedido != "CANCELADO")
    erros = []
    if not any(por_dia[d] >= catalogo.FAROL_VERMELHO for d in range(0, 8)):
        erros.append("no full day in the next 7 days")
    if not any(por_dia[d] == catalogo.FAROL_AMARELO for d in range(0, 15)):
        erros.append("no amber day in the agenda")
    if not any(p.valor_sinal != p.valor_restante for p in m.pagamentos):
        erros.append("no total with odd cents")
    return erros


CHECAGENS = (
    total_sinal_restante,
    todos_os_status,
    pagamento_e_producao_por_status,
    ficha_unidades_compativeis,
    embalagem_unidade,
    preco_segue_formula,
    saldo_igual_movimentacoes,
    estoque_coerente,
    movimentacoes_coerentes,
    cancelamentos_coerentes,
    faixas_ficticias,
    datas_coerentes,
)


def verificar(m: Massa) -> list[str]:
    erros = []
    for checagem in CHECAGENS:
        erros += checagem(m)
    if m.tamanho == "demo":
        erros += agenda_do_demo(m)
    return erros


def contagens(m: Massa) -> dict[str, int]:
    return {
        "categoria_produto": len(m.categorias_produto),
        "categoria_insumo": len(m.categorias_insumo),
        "cliente": len(m.clientes),
        "insumo": len(m.insumos),
        "produto": len(m.produtos),
        "produto_insumo": len(m.fichas),
        "pedido": len(m.pedidos),
        "item_pedido": len(m.itens),
        "pagamento": len(m.pagamentos),
        "producao": len(m.producoes),
        "cancelamento_pedido": len(m.cancelamentos),
        "movimentacao_estoque": len(m.movimentacoes),
        "notificacao": len(m.notificacoes),
    }


def contagem_por_status(m: Massa) -> dict[str, int]:
    return dict(sorted(Counter(p.status_pedido for p in m.pedidos).items()))
