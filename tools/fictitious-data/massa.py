"""Builds the fictitious dataset in memory.

Every date is an offset in days from the reference day, so the demo can be written with
relative SQL dates and the volume with literal ones from the very same model.
"""

import hashlib
import random
import unicodedata
from dataclasses import dataclass, field
from decimal import ROUND_CEILING, ROUND_HALF_UP, Decimal

import catalogo

CENTAVO = Decimal("0.01")
MILESIMO = Decimal("0.001")
DECIMO_DE_MILESIMO = Decimal("0.0001")
CEM = Decimal("100")
MIL = Decimal("1000")

STATUS_PEDIDO = (
    "ORCAMENTO",
    "AGUARDANDO_SINAL",
    "ESPERANDO_FAZER",
    "EM_PRODUCAO",
    "AGUARDANDO_ENTREGA",
    "ENTREGUE",
    "CANCELADO",
)
TIPOS_RETORNO = ("DEVOLUCAO", "SEM_DEVOLUCAO")
TIPOS_CANCELAMENTO = ("CLIENTE_DESISTIU", "ERRO_PEDIDO", "FALTA_INSUMO", "OUTRO")
FORMAS_PAGAMENTO = ("PIX", "DINHEIRO", "CARTAO_CREDITO", "CARTAO_DEBITO", "TRANSFERENCIA")
ORIGENS_COMPRA = ("MANUAL", "NOTA_IMAGEM", "TEXTO")
FASES_COM_SINAL = ("ESPERANDO_FAZER", "EM_PRODUCAO", "AGUARDANDO_ENTREGA", "ENTREGUE")
FASES_COM_BAIXA = ("EM_PRODUCAO", "AGUARDANDO_ENTREGA", "ENTREGUE")
STATUS_PRODUCAO_POR_FASE = {
    "ESPERANDO_FAZER": "ESPERANDO_FAZER",
    "EM_PRODUCAO": "EM_PRODUCAO",
    "AGUARDANDO_ENTREGA": "PRONTO_PARA_ENTREGA",
    "ENTREGUE": "FINALIZADO",
}

DIAS_VOLUME = 1095
ULTIMO_DIA_ATIVO = 14
PEDIDOS_VOLUME = 80_000
FAIXA_ITENS_DEMO = (2, 4)
FAIXA_ITENS_VOLUME = (1, 5)
CLIENTES_VOLUME = 2_000
INSUMOS_VOLUME = 200
PRODUTOS_VOLUME = 60
LINHAS_POR_FICHA_VOLUME = 5
COMPRAS_POR_INSUMO_VOLUME = 18

DIA_VERMELHO_DEMO = 3
DIA_AMARELO_DEMO = 5


@dataclass(slots=True)
class Categoria:
    id: int
    nome: str
    descricao: str


@dataclass(slots=True)
class Cliente:
    id: int
    nome: str
    telefone: str
    endereco: str
    cep: str
    tipo_pessoa: str
    classificacao_cliente: str


@dataclass(slots=True)
class Insumo:
    id: int
    fk_categoria_insumo: int
    nome: str
    nome_normalizado: str
    quantidade_atual: Decimal
    quantidade_minima: Decimal
    unidade: str
    marca: str
    custo_unitario: Decimal
    abaixo_do_minimo: bool
    custo_base: Decimal


@dataclass(slots=True)
class FichaLinha:
    id: int
    fk_produto: int
    fk_insumo: int
    quantidade_utilizada: Decimal
    unidade: str


@dataclass(slots=True)
class Produto:
    id: int
    fk_categoria_produto: int
    nome: str
    preco_atual: Decimal
    margem_lucro: Decimal
    custo_calculado: Decimal
    quantidade_produzida: int
    descricao: str


@dataclass(slots=True)
class Pedido:
    id: int
    fk_cliente: int
    tipo_pedido: str
    status_pedido: str
    fase: str
    forma_entrega: str
    endereco_entrega: str | None
    cep: str | None
    dia_entrega: int
    anotacao: str | None
    dia_criacao: int
    dia_confirmacao: int | None


@dataclass(slots=True)
class ItemPedido:
    id: int
    fk_produto: int
    fk_pedido: int
    quantidade: Decimal
    preco_original_produto: Decimal
    valor_unitario: Decimal
    valor_total: Decimal
    custo_estimado_item: Decimal
    margem_lucro_item: Decimal


@dataclass(slots=True)
class Pagamento:
    id: int
    fk_pedido: int
    valor_total: Decimal
    valor_sinal: Decimal
    valor_restante: Decimal
    forma_pagamento_sinal: str | None
    forma_pagamento_restante: str | None
    status_pagamento: str
    dia_pagamento_sinal: int | None
    dia_pagamento_restante: int | None


@dataclass(slots=True)
class Producao:
    id: int
    fk_pedido: int
    dia_inicio: int | None
    dia_prevista: int
    dia_finalizacao: int | None
    status_producao: str


@dataclass(slots=True)
class Cancelamento:
    id: int
    fk_pedido: int
    tipo_cancelamento: str
    tipo_retorno: str
    valor_retorno: Decimal
    dia_cancelamento: int


@dataclass(slots=True)
class Movimentacao:
    id: int
    fk_insumo: int
    tipo: str
    quantidade: Decimal
    quantidade_informada: Decimal
    unidade_informada: str
    custo_unitario: Decimal | None
    origem: str
    fluxo_id: str | None
    chave_idempotencia: str | None
    hash_conteudo: str
    dia_ocorrido: int
    valor_liquido: Decimal | None = None
    fk_pedido: int | None = None


@dataclass(slots=True)
class Notificacao:
    id: int
    tipo: str
    titulo: str
    mensagem: str
    dia_criacao: int
    lida: bool
    chave_unicidade: str
    fk_pedido: int | None
    fk_insumo: int | None


@dataclass
class Massa:
    tamanho: str
    semente: int
    com_saida_producao: bool
    categorias_produto: list[Categoria] = field(default_factory=list)
    categorias_insumo: list[Categoria] = field(default_factory=list)
    clientes: list[Cliente] = field(default_factory=list)
    insumos: list[Insumo] = field(default_factory=list)
    produtos: list[Produto] = field(default_factory=list)
    fichas: list[FichaLinha] = field(default_factory=list)
    pedidos: list[Pedido] = field(default_factory=list)
    itens: list[ItemPedido] = field(default_factory=list)
    pagamentos: list[Pagamento] = field(default_factory=list)
    producoes: list[Producao] = field(default_factory=list)
    cancelamentos: list[Cancelamento] = field(default_factory=list)
    movimentacoes: list[Movimentacao] = field(default_factory=list)
    notificacoes: list[Notificacao] = field(default_factory=list)


@dataclass(slots=True)
class _Plano:
    """What one order must become; ids are assigned after sorting by creation day."""

    status: str
    dia_entrega: int
    dia_criacao: int
    fase: str
    tipo_retorno: str | None = None
    devolucao_parcial: bool = False
    restante_pago: bool = False
    total_impar: bool = False


def centavos(valor: Decimal) -> Decimal:
    return valor.quantize(CENTAVO, rounding=ROUND_HALF_UP)


def quantidade_decimal(valor: Decimal) -> Decimal:
    return valor.quantize(MILESIMO, rounding=ROUND_HALF_UP)


def normalizar_nome(nome: str) -> str:
    """Same rule as the back-end supply-name normalizer."""
    sem_acento = "".join(
        c for c in unicodedata.normalize("NFD", nome) if unicodedata.category(c) != "Mn"
    )
    return " ".join(sem_acento.strip().lower().split())


def grandeza(unidade: str) -> str:
    return {"KG": "MASSA", "G": "MASSA", "L": "VOLUME", "ML": "VOLUME", "UNIDADE": "CONTAGEM"}[unidade]


def compativeis(origem: str, destino: str) -> bool:
    return grandeza(origem) == grandeza(destino)


def converter(quantidade: Decimal, origem: str, destino: str) -> Decimal:
    if not compativeis(origem, destino):
        raise ValueError(f"incompatible units: {origem} and {destino}")
    if origem == destino:
        return quantidade
    return quantidade / MIL if origem in ("G", "ML") else quantidade * MIL


def custo_do_produto(linhas: list[FichaLinha], insumos_por_id: dict[int, Insumo], indiretos: Decimal) -> Decimal:
    custo_insumos = sum(
        (
            converter(linha.quantidade_utilizada, linha.unidade, insumos_por_id[linha.fk_insumo].unidade)
            * insumos_por_id[linha.fk_insumo].custo_unitario
            for linha in linhas
        ),
        Decimal("0"),
    )
    return centavos(custo_insumos * (1 + indiretos / CEM))


def preco_sugerido(custo: Decimal, margem: Decimal) -> Decimal:
    """Rounds up to the next multiple of 0.50."""
    base = custo * (1 + margem / CEM)
    return ((base * 2).quantize(Decimal("1"), rounding=ROUND_CEILING) / 2).quantize(CENTAVO)


def sinal_de(total: Decimal) -> Decimal:
    return centavos(total * Decimal("0.50"))


def _texto_decimal(valor: Decimal) -> str:
    """Same text as BigDecimal.stripTrailingZeros().toPlainString()."""
    return "0" if valor.is_zero() else format(valor.normalize(), "f")


def _sha256(conteudo: str) -> str:
    return hashlib.sha256(conteudo.encode("utf-8")).hexdigest()


def hash_entrada(insumo_id: int, quantidade_informada: Decimal, unidade: str, valor_liquido: Decimal,
                 fluxo_id: str | None) -> str:
    return _sha256("|".join([
        str(insumo_id),
        _texto_decimal(quantidade_informada),
        unidade,
        _texto_decimal(valor_liquido),
        "",
        fluxo_id or "",
    ]))


def hash_manual(insumo_id: int, tipo: str, quantidade: Decimal) -> str:
    return _sha256("|".join([str(insumo_id), tipo, _texto_decimal(quantidade)]))


def hash_saida_producao(pedido_id: int, insumo_id: int, quantidade: Decimal) -> str:
    return _sha256("|".join([str(pedido_id), str(insumo_id), "SAIDA_PRODUCAO", _texto_decimal(quantidade)]))


def telefone_ficticio(sequencia: int) -> str:
    return f"+55 11 90000-{sequencia:04d}"


def cep_ficticio(sequencia: int) -> str:
    return f"00000{(sequencia % 999) + 1:03d}"


def _endereco(rng: random.Random) -> str:
    return (
        f"{rng.choice(catalogo.LOGRADOUROS)}, {rng.randint(1, 999)} - "
        f"{rng.choice(catalogo.BAIRROS)}, {catalogo.CIDADE}"
    )


def _gerar_clientes(massa: Massa, rng: random.Random, escala: float) -> None:
    if massa.tamanho == "demo":
        fontes = catalogo.CLIENTES_DEMO
    else:
        total = max(10, round(CLIENTES_VOLUME * escala))
        fontes = [
            (
                f"Cliente Exemplo {i:04d}",
                "JURIDICA" if i % 20 == 0 else "FISICA",
                "ALTO_VOLUME" if i % 20 == 0 else ("FREQUENTE" if i % 5 == 0 else "PADRAO"),
            )
            for i in range(1, total + 1)
        ]
    for i, (nome, tipo_pessoa, classificacao) in enumerate(fontes, start=1):
        massa.clientes.append(Cliente(
            id=i,
            nome=nome,
            telefone=telefone_ficticio(i),
            endereco=_endereco(rng),
            cep=cep_ficticio(i + 100),
            tipo_pessoa=tipo_pessoa,
            classificacao_cliente=classificacao,
        ))


def _novo_insumo(identificador: int, categoria: int, nome: str, unidade: str, minimo: Decimal, custo: Decimal,
                 baixo: bool, rng: random.Random) -> Insumo:
    return Insumo(
        id=identificador,
        fk_categoria_insumo=categoria,
        nome=nome,
        nome_normalizado=normalizar_nome(nome),
        quantidade_atual=Decimal("0.000"),
        quantidade_minima=minimo,
        unidade=unidade,
        marca=rng.choice(catalogo.MARCAS),
        custo_unitario=custo,
        abaixo_do_minimo=baixo,
        custo_base=custo,
    )


def _gerar_insumos_demo(massa: Massa, rng: random.Random) -> None:
    for i, (nome, categoria, unidade, minimo, custo, baixo) in enumerate(catalogo.INSUMOS_DEMO, start=1):
        massa.insumos.append(_novo_insumo(i, categoria, nome, unidade, Decimal(minimo), Decimal(custo), baixo, rng))


def _gerar_insumos_volume(massa: Massa, rng: random.Random, escala: float) -> None:
    total = max(20, round(INSUMOS_VOLUME * escala))
    categoria_embalagem = len(catalogo.CATEGORIAS_INSUMO_DEMO)
    for i in range(1, total + 1):
        if i % 5 == 0:
            nome, categoria, unidade = f"Embalagem Exemplo {i:03d}", categoria_embalagem, "UNIDADE"
            custo = Decimal(rng.randint(20, 400)) / CEM
            minimo = Decimal(rng.randint(20, 200))
        else:
            unidade = "L" if i % 5 == 2 else "KG"
            nome, categoria = f"Insumo Exemplo {i:03d}", (i % 3) + 1
            custo = Decimal(rng.randint(500, 8000)) / CEM
            minimo = Decimal(rng.randint(2, 20))
        massa.insumos.append(_novo_insumo(
            i, categoria, nome, unidade, quantidade_decimal(minimo), custo.quantize(DECIMO_DE_MILESIMO),
            i % 6 == 1 and unidade != "UNIDADE", rng,
        ))


def _gerar_estoque_base(massa: Massa, rng: random.Random, primeiro_dia: int, compras_por_insumo) -> list[Movimentacao]:
    """Purchases and adjustments. The opening balance comes later, once the production
    withdrawals are known, so the closing balance does not depend on the withdrawal switch."""
    movimentos = []
    for insumo in massa.insumos:
        compras = compras_por_insumo(insumo)
        dias = sorted(rng.randint(primeiro_dia + 1, -1) for _ in range(compras))
        for ordem, dia in enumerate(dias):
            ultima = ordem == compras - 1
            variacao = Decimal("1") if ultima else Decimal(rng.randint(90, 99)) / CEM
            custo = (insumo.custo_base * variacao).quantize(DECIMO_DE_MILESIMO, rounding=ROUND_HALF_UP)
            # Supplies meant to end below the minimum buy half of it in total, whatever the count.
            fator = Decimal("0.50") / compras if insumo.abaixo_do_minimo else Decimal(rng.randint(100, 200)) / CEM
            quantidade = quantidade_decimal(insumo.quantidade_minima * fator)
            if insumo.unidade == "UNIDADE":
                quantidade = quantidade.quantize(Decimal("1"), rounding=ROUND_CEILING).quantize(MILESIMO)
            quantidade_informada, unidade_informada = quantidade, insumo.unidade
            if insumo.unidade in ("KG", "L") and rng.random() < 0.5:
                quantidade_informada = quantidade * MIL
                unidade_informada = "G" if insumo.unidade == "KG" else "ML"
            origem = rng.choice(ORIGENS_COMPRA)
            fluxo = None if origem == "MANUAL" else f"{massa.tamanho}-fluxo-{insumo.id}-{ordem + 1}"
            valor_liquido = quantidade * custo
            movimentos.append(Movimentacao(
                id=0,
                fk_insumo=insumo.id,
                tipo="ENTRADA_COMPRA",
                quantidade=quantidade,
                quantidade_informada=quantidade_informada,
                unidade_informada=unidade_informada,
                custo_unitario=custo,
                origem=origem,
                fluxo_id=fluxo,
                chave_idempotencia=None,
                hash_conteudo=hash_entrada(insumo.id, quantidade_informada, unidade_informada, valor_liquido, fluxo),
                dia_ocorrido=dia,
                valor_liquido=valor_liquido,
            ))
            if ultima:
                insumo.custo_unitario = custo
    return movimentos


def _gerar_ajustes(massa: Massa, rng: random.Random, primeiro_dia: int, quantos: int) -> list[Movimentacao]:
    candidatos = [i for i in massa.insumos if not i.abaixo_do_minimo and i.unidade != "UNIDADE"]
    movimentos = []
    for insumo in rng.sample(candidatos, min(quantos, len(candidatos))):
        sinal = 1 if rng.random() < 0.5 else -1
        diferenca = quantidade_decimal(insumo.quantidade_minima * Decimal("0.05") * sinal)
        movimentos.append(Movimentacao(
            id=0,
            fk_insumo=insumo.id,
            tipo="AJUSTE",
            quantidade=diferenca,
            quantidade_informada=diferenca,
            unidade_informada=insumo.unidade,
            custo_unitario=None,
            origem="MANUAL",
            fluxo_id=None,
            chave_idempotencia=None,
            hash_conteudo=hash_manual(insumo.id, "AJUSTE", diferenca),
            dia_ocorrido=rng.randint(primeiro_dia + 1, -1),
        ))
    return movimentos


def _gerar_saidas(massa: Massa) -> list[Movimentacao]:
    """One withdrawal per order and supply for every order that reached production."""
    insumos_por_id = {i.id: i for i in massa.insumos}
    linhas_por_produto: dict[int, list[FichaLinha]] = {}
    for linha in massa.fichas:
        linhas_por_produto.setdefault(linha.fk_produto, []).append(linha)
    itens_por_pedido: dict[int, list[ItemPedido]] = {}
    for item in massa.itens:
        itens_por_pedido.setdefault(item.fk_pedido, []).append(item)
    inicio_por_pedido = {p.fk_pedido: p.dia_inicio for p in massa.producoes}

    movimentos = []
    for pedido in massa.pedidos:
        if pedido.fase not in FASES_COM_BAIXA:
            continue
        por_insumo: dict[int, Decimal] = {}
        for item in itens_por_pedido.get(pedido.id, []):
            for linha in linhas_por_produto[item.fk_produto]:
                unidade = insumos_por_id[linha.fk_insumo].unidade
                consumo = converter(linha.quantidade_utilizada, linha.unidade, unidade) * item.quantidade
                por_insumo[linha.fk_insumo] = por_insumo.get(linha.fk_insumo, Decimal("0")) + consumo
        for insumo_id in sorted(por_insumo):
            quantidade = quantidade_decimal(-por_insumo[insumo_id])
            movimentos.append(Movimentacao(
                id=0,
                fk_insumo=insumo_id,
                tipo="SAIDA_PRODUCAO",
                quantidade=quantidade,
                quantidade_informada=quantidade,
                unidade_informada=insumos_por_id[insumo_id].unidade,
                custo_unitario=None,
                origem="PRODUCAO",
                fluxo_id=f"pedido:{pedido.id}",
                chave_idempotencia=f"SAIDA_PRODUCAO:pedido:{pedido.id}:insumo:{insumo_id}",
                hash_conteudo=hash_saida_producao(pedido.id, insumo_id, quantidade),
                dia_ocorrido=inicio_por_pedido[pedido.id],
                fk_pedido=pedido.id,
            ))
    return movimentos


def _fechar_estoque(massa: Massa, base: list[Movimentacao], saidas: list[Movimentacao], primeiro_dia: int) -> None:
    prefixo = "demo" if massa.tamanho == "demo" else "vol"
    soma_base: dict[int, Decimal] = {}
    soma_saidas: dict[int, Decimal] = {}
    for movimento in base:
        soma_base[movimento.fk_insumo] = soma_base.get(movimento.fk_insumo, Decimal("0")) + movimento.quantidade
    for movimento in saidas:
        soma_saidas[movimento.fk_insumo] = soma_saidas.get(movimento.fk_insumo, Decimal("0")) + movimento.quantidade

    iniciais = []
    for insumo in massa.insumos:
        fator = Decimal("0.10") if insumo.abaixo_do_minimo else Decimal("2")
        # The opening balance absorbs the withdrawals so the balance never goes negative.
        quantidade = quantidade_decimal(insumo.quantidade_minima * fator - soma_saidas.get(insumo.id, Decimal("0")))
        iniciais.append(Movimentacao(
            id=0,
            fk_insumo=insumo.id,
            tipo="SALDO_INICIAL",
            quantidade=quantidade,
            quantidade_informada=quantidade,
            unidade_informada=insumo.unidade,
            custo_unitario=None,
            origem="MANUAL",
            fluxo_id=None,
            chave_idempotencia=None,
            hash_conteudo=hash_manual(insumo.id, "SALDO_INICIAL", quantidade),
            dia_ocorrido=primeiro_dia,
        ))
        insumo.quantidade_atual = quantidade_decimal(
            quantidade + soma_base.get(insumo.id, Decimal("0")) + soma_saidas.get(insumo.id, Decimal("0"))
        )

    todos = iniciais + base + saidas
    todos.sort(key=lambda m: (m.dia_ocorrido, m.fk_insumo, m.tipo))
    for identificador, movimento in enumerate(todos, start=1):
        movimento.id = identificador
        if movimento.chave_idempotencia is None:
            movimento.chave_idempotencia = f"{prefixo}-{identificador}"
    massa.movimentacoes = todos


def _gerar_produtos_demo(massa: Massa) -> None:
    por_nome = {i.nome: i for i in massa.insumos}
    identificador_ficha = 0
    for i, (nome, categoria, margem, receita) in enumerate(catalogo.PRODUTOS_DEMO, start=1):
        linhas = []
        for nome_insumo, quantidade, unidade in receita:
            identificador_ficha += 1
            linhas.append(FichaLinha(
                identificador_ficha, i, por_nome[nome_insumo].id, quantidade_decimal(Decimal(quantidade)), unidade,
            ))
        massa.fichas.extend(linhas)
        massa.produtos.append(_novo_produto(i, categoria, nome, Decimal(margem), linhas, massa))


def _gerar_produtos_volume(massa: Massa, rng: random.Random, escala: float) -> None:
    embalagens = [i for i in massa.insumos if i.unidade == "UNIDADE"]
    comuns = [i for i in massa.insumos if i.unidade != "UNIDADE"]
    identificador_ficha = 0
    for i in range(1, max(10, round(PRODUTOS_VOLUME * escala)) + 1):
        escolhidos = rng.sample(comuns, LINHAS_POR_FICHA_VOLUME - 1)
        linhas = []
        for insumo in escolhidos:
            identificador_ficha += 1
            unidade = "G" if insumo.unidade == "KG" else "ML"
            linhas.append(FichaLinha(
                identificador_ficha, i, insumo.id, quantidade_decimal(Decimal(rng.randint(10, 300))), unidade,
            ))
        identificador_ficha += 1
        linhas.append(FichaLinha(identificador_ficha, i, rng.choice(embalagens).id, Decimal("1.000"), "UNIDADE"))
        massa.fichas.extend(linhas)
        categoria = (i % len(massa.categorias_produto)) + 1
        massa.produtos.append(_novo_produto(
            i, categoria, f"Produto Exemplo {i:03d}", catalogo.MARGEM_PADRAO, linhas, massa,
        ))


def _novo_produto(identificador: int, categoria: int, nome: str, margem: Decimal, linhas: list[FichaLinha],
                  massa: Massa) -> Produto:
    insumos_por_id = {i.id: i for i in massa.insumos}
    custo = custo_do_produto(linhas, insumos_por_id, catalogo.PERCENTUAL_INDIRETOS)
    return Produto(
        id=identificador,
        fk_categoria_produto=categoria,
        nome=nome,
        preco_atual=preco_sugerido(custo, margem),
        margem_lucro=margem,
        custo_calculado=custo,
        quantidade_produzida=1,
        descricao=f"{nome} (produto fictício)",
    )


def _dia_criacao(rng: random.Random, dia_entrega: int) -> int:
    return min(dia_entrega, 0) - rng.randint(3, 12)


def _plano(rng: random.Random, status: str, dia_entrega: int, **extras) -> _Plano:
    fase = extras.pop("fase", status)
    return _Plano(status, dia_entrega, _dia_criacao(rng, dia_entrega), fase, **extras)


def _planos_demo(rng: random.Random) -> list[_Plano]:
    planos = []

    # A full day and an almost full day inside the 7-day agenda window. The cancelled order
    # on the full day must not be counted, which the checks on both sides verify.
    for status in ("ESPERANDO_FAZER", "ESPERANDO_FAZER", "AGUARDANDO_SINAL", "EM_PRODUCAO", "EM_PRODUCAO",
                   "AGUARDANDO_ENTREGA", "ORCAMENTO"):
        planos.append(_plano(rng, status, DIA_VERMELHO_DEMO))
    planos.append(_plano(rng, "CANCELADO", DIA_VERMELHO_DEMO, fase="AGUARDANDO_SINAL", tipo_retorno="SEM_DEVOLUCAO"))
    for status in ("ESPERANDO_FAZER", "ESPERANDO_FAZER", "AGUARDANDO_SINAL", "EM_PRODUCAO", "ORCAMENTO"):
        planos.append(_plano(rng, status, DIA_AMARELO_DEMO))

    dias_livres = [d for d in range(-2, 15) if d not in (DIA_VERMELHO_DEMO, DIA_AMARELO_DEMO)] * 2
    rng.shuffle(dias_livres)
    ativos = (
        ["ORCAMENTO"] * 4
        + ["AGUARDANDO_SINAL"] * 6
        + ["ESPERANDO_FAZER"] * 6
        + ["EM_PRODUCAO"] * 3
        + ["AGUARDANDO_ENTREGA"] * 5
    )
    for indice, status in enumerate(ativos):
        planos.append(_plano(
            rng, status, dias_livres[indice],
            restante_pago=status == "AGUARDANDO_ENTREGA" and indice % 2 == 0,
            total_impar=indice == len(ativos) - 7,
        ))

    # Half of the delivered orders stay inside the last 30 days so the current period has data.
    for indice in range(28):
        dia = rng.randint(-30, -1) if indice % 2 == 0 else rng.randint(-90, -31)
        planos.append(_plano(rng, "ENTREGUE", dia, restante_pago=indice >= 4))

    cancelados = [
        ("ORCAMENTO", "SEM_DEVOLUCAO", False),
        ("ESPERANDO_FAZER", "DEVOLUCAO", False),
        ("EM_PRODUCAO", "DEVOLUCAO", True),
        ("ESPERANDO_FAZER", "SEM_DEVOLUCAO", False),
        ("AGUARDANDO_ENTREGA", "SEM_DEVOLUCAO", False),
        ("AGUARDANDO_SINAL", "SEM_DEVOLUCAO", False),
    ]
    for fase, retorno, parcial in cancelados:
        planos.append(_plano(
            rng, "CANCELADO", rng.randint(-60, 10), fase=fase, tipo_retorno=retorno, devolucao_parcial=parcial,
        ))
    return planos


def _planos_volume(rng: random.Random, escala: float) -> list[_Plano]:
    fases_ativas = ("ORCAMENTO", "AGUARDANDO_SINAL", "ESPERANDO_FAZER", "EM_PRODUCAO", "AGUARDANDO_ENTREGA")
    planos = []
    # The first orders pin one of each status, so a reduced scale still covers all of them.
    for i in range(round(PEDIDOS_VOLUME * escala)):
        dia = rng.randint(-DIAS_VOLUME, ULTIMO_DIA_ATIVO)
        sorteio = rng.random()
        if i < 5:
            status = fases_ativas[i]
            dia = max(dia, 1)
        elif i == 5:
            status = "ENTREGUE"
            dia = min(dia, -1)
        elif i == 6 or sorteio < 0.03:
            fase = rng.choice(fases_ativas)
            retorno = "SEM_DEVOLUCAO"
            parcial = False
            if fase in FASES_COM_SINAL and rng.random() < 0.5:
                retorno, parcial = "DEVOLUCAO", rng.random() < 0.5
            planos.append(_plano(
                rng, "CANCELADO", dia, fase=fase, tipo_retorno=retorno, devolucao_parcial=parcial,
            ))
            continue
        elif dia >= -2:
            status = rng.choice(fases_ativas)
        elif sorteio < 0.05:
            status = "ORCAMENTO"
        else:
            status = "ENTREGUE"
        planos.append(_plano(rng, status, dia, restante_pago=rng.random() < (0.9 if status == "ENTREGUE" else 0.3)))
    return planos


def _gerar_itens(massa: Massa, rng: random.Random, pedido: Pedido, plano: _Plano, ids: dict[str, int],
                 faixa_itens: tuple[int, int]) -> Decimal:
    produtos = massa.produtos
    if plano.total_impar:
        produto = produtos[0]
        escolhas = [(produto, Decimal("1.000"), Decimal("133.15"))]
    else:
        quantos = rng.randint(*faixa_itens)
        escolhas = []
        for produto in rng.sample(produtos, min(quantos, len(produtos))):
            quantidade = Decimal(rng.randint(1, 6)).quantize(MILESIMO)
            # Some sales are negotiated below the list price, which yields odd cents.
            valor = centavos(produto.preco_atual * Decimal("0.95")) if rng.random() < 0.15 else produto.preco_atual
            escolhas.append((produto, quantidade, valor))

    total = Decimal("0.00")
    for produto, quantidade, valor_unitario in escolhas:
        ids["item"] += 1
        valor_total = centavos(quantidade * valor_unitario)
        massa.itens.append(ItemPedido(
            id=ids["item"],
            fk_produto=produto.id,
            fk_pedido=pedido.id,
            quantidade=quantidade,
            preco_original_produto=produto.preco_atual,
            valor_unitario=valor_unitario,
            valor_total=valor_total,
            custo_estimado_item=produto.custo_calculado,
            margem_lucro_item=produto.margem_lucro,
        ))
        total += valor_total
    return total


def _gerar_pagamento(massa: Massa, rng: random.Random, pedido: Pedido, plano: _Plano, total: Decimal,
                     ids: dict[str, int]) -> tuple[int | None, Decimal]:
    """Returns the day the deposit was paid, if it was, and the amount paid so far."""
    if plano.fase == "ORCAMENTO":
        return None, Decimal("0.00")
    sinal = sinal_de(total)
    sinal_pago = plano.fase in FASES_COM_SINAL
    restante_pago = plano.restante_pago and plano.status in ("AGUARDANDO_ENTREGA", "ENTREGUE")
    dia_sinal = min(pedido.dia_confirmacao + rng.randint(0, 1), 0) if sinal_pago else None
    dia_restante = None
    if restante_pago:
        dia_restante = min(pedido.dia_entrega, 0) if plano.status == "ENTREGUE" else dia_sinal
    if restante_pago:
        status_pagamento = "PAGO_INTEGRAL"
    elif sinal_pago:
        status_pagamento = "AGUARDANDO_PAGAMENTO"
    else:
        status_pagamento = "AGUARDANDO_SINAL"
    ids["pagamento"] += 1
    massa.pagamentos.append(Pagamento(
        id=ids["pagamento"],
        fk_pedido=pedido.id,
        valor_total=total,
        valor_sinal=sinal,
        valor_restante=total - sinal,
        forma_pagamento_sinal=rng.choice(FORMAS_PAGAMENTO) if sinal_pago else None,
        forma_pagamento_restante=rng.choice(FORMAS_PAGAMENTO) if restante_pago else None,
        status_pagamento=status_pagamento,
        dia_pagamento_sinal=dia_sinal,
        dia_pagamento_restante=dia_restante,
    ))
    if restante_pago:
        return dia_sinal, total
    return dia_sinal, sinal if sinal_pago else Decimal("0.00")


def _gerar_producao(massa: Massa, rng: random.Random, pedido: Pedido, plano: _Plano, dia_sinal: int | None,
                    ids: dict[str, int]) -> None:
    status_producao = STATUS_PRODUCAO_POR_FASE.get(plano.fase)
    if status_producao is None:
        return
    dia_inicio = None
    if status_producao != "ESPERANDO_FAZER":
        limite = min(pedido.dia_entrega, 0)
        dia_inicio = rng.randint(min(dia_sinal, limite), limite)
    ids["producao"] += 1
    massa.producoes.append(Producao(
        id=ids["producao"],
        fk_pedido=pedido.id,
        dia_inicio=dia_inicio,
        dia_prevista=pedido.dia_entrega - 1,
        dia_finalizacao=pedido.dia_entrega if status_producao == "FINALIZADO" else None,
        status_producao=status_producao,
    ))


def _gerar_cancelamento(massa: Massa, rng: random.Random, pedido: Pedido, plano: _Plano, pago: Decimal,
                        ids: dict[str, int]) -> None:
    if plano.status != "CANCELADO":
        return
    valor = Decimal("0.00")
    if plano.tipo_retorno == "DEVOLUCAO":
        valor = centavos(pago / 2) if plano.devolucao_parcial else pago
    inicio = pedido.dia_confirmacao if pedido.dia_confirmacao is not None else pedido.dia_criacao
    ids["cancelamento"] += 1
    massa.cancelamentos.append(Cancelamento(
        id=ids["cancelamento"],
        fk_pedido=pedido.id,
        tipo_cancelamento=rng.choice(TIPOS_CANCELAMENTO),
        tipo_retorno=plano.tipo_retorno,
        valor_retorno=valor,
        dia_cancelamento=rng.randint(inicio, min(inicio + 2, 0)),
    ))


def _gerar_pedidos(massa: Massa, rng: random.Random, planos: list[_Plano], faixa_itens: tuple[int, int]) -> None:
    planos.sort(key=lambda p: (p.dia_criacao, p.dia_entrega))
    ids = {"item": 0, "pagamento": 0, "producao": 0, "cancelamento": 0}
    for identificador, plano in enumerate(planos, start=1):
        cliente = rng.choice(massa.clientes)
        forma_entrega = "ENTREGA" if rng.random() < 0.6 else "RETIRADA"
        confirmado = plano.fase != "ORCAMENTO"
        pedido = Pedido(
            id=identificador,
            fk_cliente=cliente.id,
            tipo_pedido="PEDIDO_CONFIRMADO" if confirmado else "ORCAMENTO",
            status_pedido=plano.status,
            fase=plano.fase,
            forma_entrega=forma_entrega,
            endereco_entrega=cliente.endereco if forma_entrega == "ENTREGA" else None,
            cep=cliente.cep if forma_entrega == "ENTREGA" else None,
            dia_entrega=plano.dia_entrega,
            anotacao=rng.choice(catalogo.ANOTACOES) if rng.random() < 0.2 else None,
            dia_criacao=plano.dia_criacao,
            dia_confirmacao=plano.dia_criacao + rng.randint(0, 1) if confirmado else None,
        )
        massa.pedidos.append(pedido)
        total = _gerar_itens(massa, rng, pedido, plano, ids, faixa_itens)
        dia_sinal, pago = _gerar_pagamento(massa, rng, pedido, plano, total, ids)
        _gerar_producao(massa, rng, pedido, plano, dia_sinal, ids)
        _gerar_cancelamento(massa, rng, pedido, plano, pago, ids)


def _gerar_notificacoes_demo(massa: Massa) -> None:
    baixos = [i for i in massa.insumos if i.quantidade_atual < i.quantidade_minima]
    lotados = [p for p in massa.pedidos if p.dia_entrega == DIA_VERMELHO_DEMO and p.status_pedido != "CANCELADO"]
    amarelos = [p for p in massa.pedidos if p.dia_entrega == DIA_AMARELO_DEMO and p.status_pedido != "CANCELADO"]
    compras = [i for i in massa.insumos if not i.abaixo_do_minimo]
    definicoes = [
        ("ESTOQUE_BAIXO", "Estoque baixo", f"{baixos[0].nome} está abaixo do mínimo.", -1, False, None, baixos[0].id),
        ("ESTOQUE_BAIXO", "Estoque baixo", f"{baixos[1].nome} está abaixo do mínimo.", -2, True, None, baixos[1].id),
        ("DIA_LOTADO", "Dia lotado", f"Daqui a {DIA_VERMELHO_DEMO} dias há {len(lotados)} pedidos para entregar.",
         0, False, lotados[0].id, None),
        ("DIA_LOTADO", "Agenda quase cheia", f"Daqui a {DIA_AMARELO_DEMO} dias há {len(amarelos)} pedidos para entregar.",
         -1, True, amarelos[0].id, None),
        ("COMPRA_REGISTRADA", "Compra registrada", f"Entrada de {compras[0].nome} registrada no estoque.",
         -3, True, None, compras[0].id),
        ("COMPRA_REGISTRADA", "Compra registrada", f"Entrada de {compras[1].nome} registrada no estoque.",
         -1, False, None, compras[1].id),
    ]
    for i, (tipo, titulo, mensagem, dia, lida, pedido, insumo) in enumerate(definicoes, start=1):
        massa.notificacoes.append(Notificacao(i, tipo, titulo, mensagem, dia, lida, f"demo-{i}", pedido, insumo))


def _categorias(fontes: list[tuple[str, str]]) -> list[Categoria]:
    return [Categoria(i, nome, descricao) for i, (nome, descricao) in enumerate(fontes, start=1)]


def gerar_demo(semente: int, com_saida_producao: bool = False) -> Massa:
    rng = random.Random(semente)
    massa = Massa("demo", semente, com_saida_producao)
    massa.categorias_produto = _categorias(catalogo.CATEGORIAS_PRODUTO_DEMO)
    massa.categorias_insumo = _categorias(catalogo.CATEGORIAS_INSUMO_DEMO)
    _gerar_clientes(massa, rng, 1.0)
    _gerar_insumos_demo(massa, rng)

    primeiro_dia = -120
    base = _gerar_estoque_base(massa, rng, primeiro_dia, lambda i: 1 + (i.id % 2))
    base += _gerar_ajustes(massa, rng, primeiro_dia, 3)
    _gerar_produtos_demo(massa)
    _gerar_pedidos(massa, rng, _planos_demo(rng), FAIXA_ITENS_DEMO)
    saidas = _gerar_saidas(massa) if com_saida_producao else []
    _fechar_estoque(massa, base, saidas, primeiro_dia)
    _gerar_notificacoes_demo(massa)
    return massa


def gerar_volume(semente: int, escala: float = 1.0, com_saida_producao: bool = False) -> Massa:
    rng = random.Random(semente)
    massa = Massa("volume", semente, com_saida_producao)
    massa.categorias_produto = _categorias(catalogo.CATEGORIAS_PRODUTO_DEMO + catalogo.CATEGORIAS_EXTRAS_VOLUME)
    massa.categorias_insumo = _categorias(catalogo.CATEGORIAS_INSUMO_DEMO + catalogo.CATEGORIAS_EXTRAS_VOLUME)
    _gerar_clientes(massa, rng, escala)
    _gerar_insumos_volume(massa, rng, escala)

    primeiro_dia = -DIAS_VOLUME - 30
    base = _gerar_estoque_base(massa, rng, primeiro_dia, lambda _: COMPRAS_POR_INSUMO_VOLUME)
    base += _gerar_ajustes(massa, rng, primeiro_dia, 20)
    _gerar_produtos_volume(massa, rng, escala)
    _gerar_pedidos(massa, rng, _planos_volume(rng, escala), FAIXA_ITENS_VOLUME)
    saidas = _gerar_saidas(massa) if com_saida_producao else []
    _fechar_estoque(massa, base, saidas, primeiro_dia)
    return massa
