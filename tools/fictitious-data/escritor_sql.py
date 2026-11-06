"""Streams the dataset as SQL, one multi-row INSERT per batch of 1,000 rows."""

from datetime import date, timedelta
from decimal import Decimal
from typing import Callable, Iterable, TextIO

import catalogo
from massa import Massa

TAMANHO_LOTE = 1000


class Tempo:
    """Writes a day offset as relative SQL (demo) or as a literal date (volume)."""

    def __init__(self, data_referencia: date | None):
        self.data_referencia = data_referencia

    def _relativo(self, base: str, dia: int) -> str:
        if dia == 0:
            return base
        return f"{base} {'+' if dia > 0 else '-'} INTERVAL '{abs(dia)}' DAY"

    def _literal(self, dia: int) -> date:
        return self.data_referencia + timedelta(days=dia)

    def data(self, dia: int | None) -> str:
        if dia is None:
            return "NULL"
        if self.data_referencia is None:
            return self._relativo("CURRENT_DATE", dia)
        return f"'{self._literal(dia).isoformat()}'"

    def instante(self, dia: int | None) -> str:
        if dia is None:
            return "NULL"
        if self.data_referencia is None:
            return self._relativo("CURRENT_TIMESTAMP", dia)
        return f"'{self._literal(dia).isoformat()} 10:00:00'"


def _sql(valor) -> str:
    if valor is None:
        return "NULL"
    if isinstance(valor, bool):
        return "TRUE" if valor else "FALSE"
    if isinstance(valor, (int, Decimal)):
        return format(valor, "f") if isinstance(valor, Decimal) else str(valor)
    return "'" + str(valor).replace("'", "''") + "'"


class _Bruto(str):
    """An expression that goes to the SQL as is, such as a relative date."""


def _campo(valor) -> str:
    return str(valor) if isinstance(valor, _Bruto) else _sql(valor)


def _escrever_tabela(saida: TextIO, tabela: str, colunas: str, linhas: Iterable, para_linha: Callable) -> None:
    lote: list[str] = []

    def descarregar() -> None:
        saida.write(f"INSERT INTO {tabela} ({colunas}) VALUES\n")
        saida.write(",\n".join(lote))
        saida.write(";\n\n")
        lote.clear()

    for linha in linhas:
        lote.append("  (" + ", ".join(_campo(v) for v in para_linha(linha)) + ")")
        if len(lote) == TAMANHO_LOTE:
            descarregar()
    if lote:
        descarregar()


def escrever(massa: Massa, saida: TextIO, cabecalho: str, data_referencia: date | None = None) -> None:
    tempo = Tempo(data_referencia)
    data = lambda dia: _Bruto(tempo.data(dia))  # noqa: E731
    instante = lambda dia: _Bruto(tempo.instante(dia))  # noqa: E731
    carga_em_massa = massa.tamanho == "volume"

    saida.write(f"-- {cabecalho}\n\n")
    if carga_em_massa:
        saida.write("SET NAMES utf8mb4;\nSET FOREIGN_KEY_CHECKS=0;\nSET unique_checks=0;\n\n")

    _escrever_tabela(saida, "categoria_produto", "id_categoria_produto, nome, descricao", massa.categorias_produto,
                     lambda c: (c.id, c.nome, c.descricao))
    _escrever_tabela(saida, "categoria_insumo", "id_categoria_insumo, nome, descricao", massa.categorias_insumo,
                     lambda c: (c.id, c.nome, c.descricao))
    _escrever_tabela(
        saida, "cliente",
        "id_cliente, nome, telefone, endereco, cep, tipo_pessoa, classificacao_cliente, status, observacao",
        massa.clientes,
        lambda c: (c.id, c.nome, c.telefone, c.endereco, c.cep, c.tipo_pessoa, c.classificacao_cliente, "ATIVO", None),
    )
    _escrever_tabela(
        saida, "insumo",
        "id_insumo, fk_categoria_insumo, nome, nome_normalizado, quantidade_atual, quantidade_minima, "
        "unidade, status, marca, custo_unitario",
        massa.insumos,
        lambda i: (i.id, i.fk_categoria_insumo, i.nome, i.nome_normalizado, i.quantidade_atual, i.quantidade_minima,
                   i.unidade, "ATIVO", i.marca, i.custo_unitario),
    )
    _escrever_tabela(
        saida, "produto",
        "id_produto, fk_categoria_produto, nome, preco_atual, margem_lucro, unidade_producao, status, "
        "quantidade_produzida, descricao",
        massa.produtos,
        lambda p: (p.id, p.fk_categoria_produto, p.nome, p.preco_atual, p.margem_lucro, "UNIDADE", "ATIVO",
                   p.quantidade_produzida, p.descricao),
    )
    _escrever_tabela(
        saida, "produto_insumo", "id_produto_insumo, fk_produto, fk_insumo, quantidade_utilizada, unidade",
        massa.fichas,
        lambda f: (f.id, f.fk_produto, f.fk_insumo, f.quantidade_utilizada, f.unidade),
    )
    _escrever_tabela(
        saida, "pedido",
        "id_pedido, fk_cliente, tipo_pedido, status_pedido, forma_entrega, endereco_entrega, cep, data_entrega, "
        "anotacao, data_criacao, data_confirmacao, confirmado_com_falta_insumo",
        massa.pedidos,
        lambda p: (p.id, p.fk_cliente, p.tipo_pedido, p.status_pedido, p.forma_entrega, p.endereco_entrega, p.cep,
                   data(p.dia_entrega), p.anotacao, instante(p.dia_criacao), instante(p.dia_confirmacao), False),
    )
    _escrever_tabela(
        saida, "item_pedido",
        "id_produto_pedido, fk_produto, fk_pedido, quantidade, preco_original_produto, valor_unitario, valor_total, "
        "custo_estimado_item, margem_lucro_item, observacao",
        massa.itens,
        lambda i: (i.id, i.fk_produto, i.fk_pedido, i.quantidade, i.preco_original_produto, i.valor_unitario,
                   i.valor_total, i.custo_estimado_item, i.margem_lucro_item, None),
    )
    _escrever_tabela(
        saida, "pagamento",
        "id_pagamento, fk_pedido, valor_total, valor_sinal, valor_restante, forma_pagamento_sinal, "
        "forma_pagamento_restante, status_pagamento, data_pagamento_sinal, data_pagamento_restante",
        massa.pagamentos,
        lambda p: (p.id, p.fk_pedido, p.valor_total, p.valor_sinal, p.valor_restante, p.forma_pagamento_sinal,
                   p.forma_pagamento_restante, p.status_pagamento, data(p.dia_pagamento_sinal),
                   data(p.dia_pagamento_restante)),
    )
    _escrever_tabela(
        saida, "producao",
        "id_producao, fk_pedido, data_inicio, data_prevista, data_finalizacao, status_producao, observacao",
        massa.producoes,
        lambda p: (p.id, p.fk_pedido, data(p.dia_inicio), data(p.dia_prevista), data(p.dia_finalizacao),
                   p.status_producao, None),
    )
    _escrever_tabela(
        saida, "cancelamento_pedido",
        "id_cancelamento, fk_pedido, tipo_cancelamento, tipo_retorno, valor_retorno, data_cancelamento, observacao",
        massa.cancelamentos,
        lambda c: (c.id, c.fk_pedido, c.tipo_cancelamento, c.tipo_retorno, c.valor_retorno,
                   instante(c.dia_cancelamento), None),
    )
    _escrever_tabela(
        saida, "movimentacao_estoque",
        "id_movimentacao, fk_insumo, tipo, quantidade, quantidade_informada, unidade_informada, custo_unitario, "
        "origem, chave_nota, fluxo_id, chave_idempotencia, hash_conteudo, ocorrido_em, criado_em",
        massa.movimentacoes,
        lambda m: (m.id, m.fk_insumo, m.tipo, m.quantidade, m.quantidade_informada, m.unidade_informada,
                   m.custo_unitario, m.origem, None, m.fluxo_id, m.chave_idempotencia, m.hash_conteudo,
                   instante(m.dia_ocorrido), instante(m.dia_ocorrido)),
    )
    _escrever_tabela(
        saida, "notificacao",
        "id_notificacao, tipo, titulo, mensagem, data_criacao, lida, chave_unicidade, status_envio, fk_pedido, fk_insumo",
        massa.notificacoes,
        lambda n: (n.id, n.tipo, n.titulo, n.mensagem, instante(n.dia_criacao), n.lida, n.chave_unicidade,
                   "NAO_ENVIAR", n.fk_pedido, n.fk_insumo),
    )
    _escrever_tabela(
        saida, "configuracao_farol_agenda", "id_configuracao_farol, limite_verde, limite_amarelo, limite_vermelho",
        [(1, catalogo.FAROL_VERDE, catalogo.FAROL_AMARELO, catalogo.FAROL_VERMELHO)],
        lambda linha: linha,
    )

    # V1 already seeds these single rows, so they are updated rather than inserted.
    saida.write(
        "UPDATE configuracao_notificacao SET avisos_automaticos_ativo = TRUE, assistente_ativo = TRUE "
        "WHERE id_configuracao_notificacao = 1;\n\n"
    )
    saida.write(
        f"UPDATE configuracao_precificacao SET margem_padrao = {catalogo.MARGEM_PADRAO}, "
        f"percentual_indiretos = {catalogo.PERCENTUAL_INDIRETOS} WHERE id_configuracao_precificacao = 1;\n"
    )
    if carga_em_massa:
        saida.write("\nSET unique_checks=1;\nSET FOREIGN_KEY_CHECKS=1;\n")
