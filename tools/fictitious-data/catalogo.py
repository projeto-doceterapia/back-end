"""Fixed fictitious lists. Nothing here may resemble a real person, brand or address."""

from decimal import Decimal

CATEGORIAS_PRODUTO_DEMO = [
    ("Bolos", "Bolos inteiros e de pote"),
    ("Doces finos", "Docinhos e bombons"),
    ("Tortas", "Tortas doces"),
    ("Kits", "Combinações para festas"),
]

CATEGORIAS_INSUMO_DEMO = [
    ("Ingredientes secos", "Farinhas, açúcares e afins"),
    ("Laticínios", "Leite, creme e manteiga"),
    ("Chocolates", "Chocolates e coberturas"),
    ("Embalagens", "Caixas, potes e fitas"),
]

CATEGORIAS_EXTRAS_VOLUME = [
    ("Categoria Exemplo 5", "Categoria fictícia"),
    ("Categoria Exemplo 6", "Categoria fictícia"),
]

# (name, person type, classification)
CLIENTES_DEMO = [
    ("Ana Exemplo", "FISICA", "FREQUENTE"),
    ("Doces Modelo Ltda", "JURIDICA", "ALTO_VOLUME"),
    ("Bruna Exemplo", "FISICA", "PADRAO"),
    ("Carlos Modelo", "FISICA", "PADRAO"),
    ("Daniela Teste", "FISICA", "FREQUENTE"),
    ("Eduardo Exemplo", "FISICA", "PADRAO"),
    ("Festas Exemplo Ltda", "JURIDICA", "ALTO_VOLUME"),
    ("Fernanda Modelo", "FISICA", "PADRAO"),
    ("Gabriel Teste", "FISICA", "PADRAO"),
    ("Helena Exemplo", "FISICA", "FREQUENTE"),
    ("Igor Modelo", "FISICA", "PADRAO"),
    ("Juliana Teste", "FISICA", "PADRAO"),
    ("Buffet Teste Ltda", "JURIDICA", "ALTO_VOLUME"),
    ("Karina Exemplo", "FISICA", "PADRAO"),
    ("Lucas Modelo", "FISICA", "FREQUENTE"),
    ("Marina Teste", "FISICA", "PADRAO"),
    ("Nina Exemplo", "FISICA", "PADRAO"),
    ("Otávio Modelo", "FISICA", "PADRAO"),
    ("Paula Teste", "FISICA", "FREQUENTE"),
    ("Rafael Exemplo", "FISICA", "PADRAO"),
    ("Cafeteria Modelo Ltda", "JURIDICA", "FREQUENTE"),
    ("Sofia Modelo", "FISICA", "PADRAO"),
    ("Tiago Teste", "FISICA", "PADRAO"),
    ("Vera Exemplo", "FISICA", "PADRAO"),
    ("Yara Modelo", "FISICA", "FREQUENTE"),
]

LOGRADOUROS = ["Rua Exemplo", "Avenida Teste", "Travessa Modelo", "Alameda Fictícia"]
BAIRROS = ["Bairro Modelo", "Centro", "Vila Exemplo", "Jardim Teste"]
CIDADE = "São Paulo"

ANOTACOES = [
    "Sem lactose em um dos itens.",
    "Retirar na parte da manhã.",
    "Entregar na portaria.",
    "Escrever mensagem de parabéns no cartão.",
]

MARCAS = ["Marca Exemplo A", "Marca Exemplo B", "Marca Exemplo C", "Marca Exemplo D"]

# (name, category index starting at 1, unit, minimum, cost per unit, below the minimum)
INSUMOS_DEMO = [
    ("Farinha de trigo", 1, "KG", "10.000", "5.2000", False),
    ("Açúcar refinado", 1, "KG", "10.000", "4.6000", False),
    ("Açúcar de confeiteiro", 1, "KG", "3.000", "9.8000", False),
    ("Cacau em pó", 1, "KG", "2.000", "38.5000", False),
    ("Fermento químico", 1, "KG", "0.500", "28.0000", False),
    ("Coco ralado", 1, "KG", "2.000", "26.0000", True),
    ("Leite integral", 2, "L", "12.000", "4.9000", False),
    ("Creme de leite", 2, "L", "6.000", "17.5000", False),
    ("Leite condensado", 2, "KG", "8.000", "19.0000", False),
    ("Manteiga sem sal", 2, "KG", "4.000", "46.0000", True),
    ("Ovo", 1, "UNIDADE", "60.000", "0.8000", False),
    ("Chocolate meio amargo", 3, "KG", "4.000", "42.9000", True),
    ("Chocolate branco", 3, "KG", "3.000", "47.5000", False),
    ("Granulado de chocolate", 3, "KG", "2.000", "24.0000", False),
    ("Essência de baunilha", 1, "L", "0.500", "62.0000", False),
    ("Caixa para bolo", 4, "UNIDADE", "30.000", "3.4000", False),
    ("Caixa para bolo de pote", 4, "UNIDADE", "20.000", "1.2000", False),
    ("Forminha para doce", 4, "UNIDADE", "300.000", "0.0900", False),
    ("Pote com tampa", 4, "UNIDADE", "40.000", "1.7000", True),
    ("Caixa para torta", 4, "UNIDADE", "15.000", "4.2000", False),
]

# (name, category index starting at 1, markup percent, recipe for one sold unit)
PRODUTOS_DEMO = [
    ("Bolo de chocolate", 1, "60.00", [
        ("Farinha de trigo", "400", "G"), ("Açúcar refinado", "300", "G"), ("Cacau em pó", "80", "G"),
        ("Ovo", "4", "UNIDADE"), ("Caixa para bolo", "1", "UNIDADE"),
    ]),
    ("Bolo de pote de coco", 1, "60.00", [
        ("Coco ralado", "30", "G"), ("Leite condensado", "60", "G"), ("Farinha de trigo", "40", "G"),
        ("Pote com tampa", "1", "UNIDADE"),
    ]),
    ("Bolo de baunilha", 1, "60.00", [
        ("Farinha de trigo", "400", "G"), ("Manteiga sem sal", "150", "G"), ("Essência de baunilha", "10", "ML"),
        ("Ovo", "4", "UNIDADE"), ("Caixa para bolo", "1", "UNIDADE"),
    ]),
    ("Bolo de pote de chocolate", 1, "60.00", [
        ("Chocolate meio amargo", "50", "G"), ("Creme de leite", "40", "ML"), ("Farinha de trigo", "40", "G"),
        ("Caixa para bolo de pote", "1", "UNIDADE"), ("Pote com tampa", "1", "UNIDADE"),
    ]),
    ("Brigadeiro gourmet", 2, "70.00", [
        ("Leite condensado", "25", "G"), ("Chocolate meio amargo", "8", "G"), ("Granulado de chocolate", "4", "G"),
        ("Forminha para doce", "1", "UNIDADE"),
    ]),
    ("Beijinho", 2, "70.00", [
        ("Leite condensado", "25", "G"), ("Coco ralado", "6", "G"), ("Forminha para doce", "1", "UNIDADE"),
    ]),
    ("Bombom de chocolate branco", 2, "70.00", [
        ("Chocolate branco", "15", "G"), ("Creme de leite", "5", "ML"), ("Forminha para doce", "1", "UNIDADE"),
    ]),
    ("Torta de leite condensado", 3, "60.00", [
        ("Farinha de trigo", "250", "G"), ("Manteiga sem sal", "120", "G"), ("Leite condensado", "395", "G"),
        ("Açúcar de confeiteiro", "60", "G"), ("Caixa para torta", "1", "UNIDADE"),
    ]),
    ("Torta de chocolate", 3, "60.00", [
        ("Farinha de trigo", "250", "G"), ("Chocolate meio amargo", "300", "G"), ("Creme de leite", "200", "ML"),
        ("Fermento químico", "8", "G"), ("Caixa para torta", "1", "UNIDADE"),
    ]),
    ("Kit festa pequena", 4, "50.00", [
        ("Leite condensado", "500", "G"), ("Chocolate meio amargo", "150", "G"), ("Leite integral", "200", "ML"),
        ("Forminha para doce", "20", "UNIDADE"), ("Caixa para bolo", "1", "UNIDADE"),
    ]),
]

# Fixed anchors of the price arithmetic.
MARGEM_PADRAO = Decimal("60.00")
PERCENTUAL_INDIRETOS = Decimal("10.00")

# Traffic-light thresholds of the agenda.
FAROL_VERDE, FAROL_AMARELO, FAROL_VERMELHO = 3, 5, 7
