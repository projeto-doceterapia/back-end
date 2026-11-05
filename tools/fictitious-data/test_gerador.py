import dataclasses
import io
import re
import unittest
from datetime import date
from decimal import Decimal
from pathlib import Path

import catalogo
import escritor_sql
import gerador
import invariantes
import massa

RAIZ = Path(__file__).resolve().parents[2]
DEMO_COMMITADO = RAIZ / "src" / "main" / "resources" / "db" / "demo" / "V1_1__demo_data.sql"
SEMENTE = 2026
ESCALA_REDUZIDA = 0.01


def sql_demo(semente: int, com_saida_producao: bool = False) -> str:
    saida = io.StringIO(newline="\n")
    texto = gerador.cabecalho("demo", semente, com_saida_producao, None)
    escritor_sql.escrever(massa.gerar_demo(semente, com_saida_producao), saida, texto)
    return saida.getvalue()


def sql_volume(semente: int, referencia: date) -> str:
    saida = io.StringIO(newline="\n")
    texto = gerador.cabecalho("volume", semente, False, referencia)
    escritor_sql.escrever(massa.gerar_volume(semente, ESCALA_REDUZIDA), saida, texto, referencia)
    return saida.getvalue()


class TesteDeterminismo(unittest.TestCase):

    def test_mesma_semente_mesma_saida(self):
        self.assertEqual(sql_demo(SEMENTE), sql_demo(SEMENTE))
        referencia = date(2026, 11, 16)
        self.assertEqual(sql_volume(SEMENTE, referencia), sql_volume(SEMENTE, referencia))

    def test_semente_diferente_muda_saida(self):
        self.assertNotEqual(sql_demo(SEMENTE), sql_demo(SEMENTE + 1))

    def test_demo_commitado_atualizado(self):
        commitado = DEMO_COMMITADO.read_bytes().decode("utf-8")
        semente = int(re.search(r"--semente (\d+)", commitado.splitlines()[0]).group(1))
        com_saida = "--com-saida-producao" in commitado.splitlines()[0]
        self.assertEqual(sql_demo(semente, com_saida), commitado)


class TesteInvariantes(unittest.TestCase):

    @classmethod
    def setUpClass(cls):
        cls.demo = massa.gerar_demo(SEMENTE)

    def test_verificacao_completa_sem_violacoes(self):
        self.assertEqual([], invariantes.verificar(self.demo))

    def test_total_sinal_restante(self):
        self.assertEqual([], invariantes.total_sinal_restante(self.demo))

    def test_total_impar_arredonda_o_sinal_para_cima(self):
        impar = [p for p in self.demo.pagamentos if p.valor_total == Decimal("133.15")]
        self.assertEqual(1, len(impar))
        self.assertEqual(Decimal("66.58"), impar[0].valor_sinal)
        self.assertEqual(Decimal("66.57"), impar[0].valor_restante)

    def test_todos_os_status(self):
        self.assertEqual([], invariantes.todos_os_status(self.demo))

    def test_pagamento_e_producao_por_status(self):
        self.assertEqual([], invariantes.pagamento_e_producao_por_status(self.demo))

    def test_entregue_a_receber_e_cancelado_antes_do_sinal_existem(self):
        pagamentos = {p.fk_pedido: p for p in self.demo.pagamentos}
        a_receber = [p for p in self.demo.pedidos if p.status_pedido == "ENTREGUE"
                     and pagamentos[p.id].status_pagamento == "AGUARDANDO_PAGAMENTO"]
        self.assertGreater(len(a_receber), 0)
        self.assertTrue(any(p.fase == "AGUARDANDO_SINAL" for p in self.demo.pedidos if p.status_pedido == "CANCELADO"))

    def test_sem_usuarios(self):
        self.assertNotIn("usuario", sql_demo(SEMENTE))

    def test_ficha_unidades_compativeis(self):
        self.assertEqual([], invariantes.ficha_unidades_compativeis(self.demo))

    def test_embalagem_unidade(self):
        self.assertEqual([], invariantes.embalagem_unidade(self.demo))

    def test_preco_segue_markup_sobre_o_custo(self):
        self.assertEqual([], invariantes.preco_segue_formula(self.demo))
        self.assertEqual(Decimal("10.50"), massa.preco_sugerido(Decimal("6.50"), Decimal("60.00")))
        self.assertEqual(Decimal("16.00"), massa.preco_sugerido(Decimal("10.00"), Decimal("60.00")))

    def test_saldo_igual_movimentacoes(self):
        self.assertEqual([], invariantes.saldo_igual_movimentacoes(self.demo))
        self.assertEqual([], invariantes.estoque_coerente(self.demo))

    def test_hash_e_chave_seguem_o_service(self):
        self.assertEqual([], invariantes.movimentacoes_coerentes(self.demo))
        self.assertEqual(massa._sha256("7|SALDO_INICIAL|2.5"), massa.hash_manual(7, "SALDO_INICIAL", Decimal("2.500")))
        self.assertEqual(massa._sha256("7|AJUSTE|100"), massa.hash_manual(7, "AJUSTE", Decimal("100.000")))

    def test_cancelamentos_coerentes(self):
        self.assertEqual([], invariantes.cancelamentos_coerentes(self.demo))
        self.assertNotIn("CREDITO_CLIENTE", sql_demo(SEMENTE))

    def test_agenda_tem_dia_lotado_e_dia_amarelo(self):
        self.assertEqual([], invariantes.agenda_do_demo(self.demo))

    def test_datas_nao_ficam_no_futuro(self):
        self.assertEqual([], invariantes.datas_coerentes(self.demo))

    def test_tamanhos_do_demo(self):
        contagens = invariantes.contagens(self.demo)
        self.assertEqual(25, contagens["cliente"])
        self.assertEqual(20, contagens["insumo"])
        self.assertEqual(10, contagens["produto"])
        self.assertAlmostEqual(45, contagens["produto_insumo"], delta=5)
        self.assertAlmostEqual(70, contagens["pedido"], delta=5)
        self.assertAlmostEqual(200, contagens["item_pedido"], delta=30)
        self.assertAlmostEqual(60, contagens["pagamento"], delta=6)
        self.assertGreaterEqual(contagens["cancelamento_pedido"], 4)
        self.assertEqual(6, contagens["notificacao"])
        compras = [m for m in self.demo.movimentacoes if m.tipo == "ENTRADA_COMPRA"]
        self.assertAlmostEqual(30, len(compras), delta=3)

    def test_checagens_detectam_quebra(self):
        pagamento = self.demo.pagamentos[0]
        quebrado = dataclasses.replace(self.demo, pagamentos=[
            dataclasses.replace(pagamento, valor_sinal=pagamento.valor_sinal + Decimal("0.01"))
        ])
        self.assertNotEqual([], invariantes.total_sinal_restante(quebrado))

        insumo = self.demo.insumos[0]
        quebrado = dataclasses.replace(self.demo, insumos=[
            dataclasses.replace(insumo, quantidade_atual=insumo.quantidade_atual + 1)
        ])
        self.assertNotEqual([], invariantes.saldo_igual_movimentacoes(quebrado))

        cliente = self.demo.clientes[0]
        quebrado = dataclasses.replace(self.demo, clientes=[
            dataclasses.replace(cliente, telefone="+55 11 91234-5678")
        ])
        self.assertNotEqual([], invariantes.faixas_ficticias(quebrado))

        ficha = self.demo.fichas[0]
        quebrado = dataclasses.replace(self.demo, fichas=[dataclasses.replace(ficha, unidade="UNIDADE")])
        self.assertNotEqual([], invariantes.ficha_unidades_compativeis(quebrado))


class TesteSaidaProducao(unittest.TestCase):

    def test_sem_a_opcao_nao_grava_saida(self):
        sql = sql_demo(SEMENTE)
        self.assertNotIn("SAIDA_PRODUCAO", sql)
        self.assertNotIn("'PRODUCAO'", sql)

    def test_com_a_opcao_grava_uma_saida_por_pedido_e_insumo(self):
        demo = massa.gerar_demo(SEMENTE, com_saida_producao=True)
        self.assertEqual([], invariantes.verificar(demo))
        saidas = [m for m in demo.movimentacoes if m.tipo == "SAIDA_PRODUCAO"]
        self.assertGreater(len(saidas), 0)
        self.assertTrue(all(m.quantidade < 0 and m.origem == "PRODUCAO" for m in saidas))
        chaves = {m.chave_idempotencia for m in saidas}
        self.assertEqual(len(saidas), len(chaves))
        self.assertTrue(all(c.startswith("SAIDA_PRODUCAO:pedido:") for c in chaves))

    def test_opcao_nao_muda_o_saldo_final(self):
        sem = massa.gerar_demo(SEMENTE)
        com = massa.gerar_demo(SEMENTE, com_saida_producao=True)
        self.assertEqual([i.quantidade_atual for i in sem.insumos], [i.quantidade_atual for i in com.insumos])


class TesteFaixasFicticias(unittest.TestCase):

    @classmethod
    def setUpClass(cls):
        cls.demo = massa.gerar_demo(SEMENTE)

    def test_telefones_na_faixa(self):
        self.assertEqual([], invariantes.faixas_ficticias(self.demo))
        for cliente in self.demo.clientes:
            self.assertRegex(cliente.telefone, r"^\+55 11 90000-\d{4}$")
            self.assertLessEqual(len(cliente.telefone), 20)

    def test_cep_sem_mascara_na_faixa(self):
        for cliente in self.demo.clientes:
            self.assertRegex(cliente.cep, r"^00000\d{3}$")

    def test_marcas_ficticias(self):
        self.assertTrue(all(i.marca in catalogo.MARCAS for i in self.demo.insumos))


class TesteEscritaSql(unittest.TestCase):

    def test_data_relativa_no_demo(self):
        tempo = escritor_sql.Tempo(None)
        self.assertEqual("CURRENT_DATE + INTERVAL '3' DAY", tempo.data(3))
        self.assertEqual("CURRENT_TIMESTAMP - INTERVAL '20' DAY", tempo.instante(-20))
        self.assertEqual("CURRENT_DATE", tempo.data(0))
        self.assertEqual("NULL", tempo.data(None))

    def test_data_literal_no_volume(self):
        tempo = escritor_sql.Tempo(date(2026, 11, 16))
        self.assertEqual("'2026-11-13'", tempo.data(-3))

    def test_demo_nao_preenche_colunas_calculadas_nem_forma_antiga(self):
        sql = sql_demo(SEMENTE)
        self.assertNotIn("custo_estimado,", sql)
        self.assertNotIn("preco_sugerido", sql)
        self.assertNotRegex(sql, r"[ (]forma_pagamento,")

    def test_demo_atualiza_as_linhas_semeadas_no_schema(self):
        sql = sql_demo(SEMENTE)
        self.assertIn("UPDATE configuracao_notificacao", sql)
        self.assertIn("UPDATE configuracao_precificacao SET margem_padrao = 60.00, percentual_indiretos = 10.00", sql)
        self.assertNotIn("INSERT INTO configuracao_notificacao", sql)

    def test_volume_liga_e_desliga_as_checagens_de_carga(self):
        sql = sql_volume(SEMENTE, date(2026, 11, 16))
        self.assertIn("SET FOREIGN_KEY_CHECKS=0;", sql)
        self.assertTrue(sql.endswith("SET FOREIGN_KEY_CHECKS=1;\n"))
        self.assertNotIn("CURRENT_DATE", sql)

    def test_lote_tem_no_maximo_mil_linhas(self):
        lote = re.split(r"\nINSERT INTO", sql_volume(SEMENTE, date(2026, 11, 16)))
        self.assertTrue(all(trecho.count("\n  (") <= escritor_sql.TAMANHO_LOTE for trecho in lote))


class TesteVolumeReduzido(unittest.TestCase):

    @classmethod
    def setUpClass(cls):
        cls.volume = massa.gerar_volume(SEMENTE, ESCALA_REDUZIDA)

    def test_invariantes(self):
        self.assertEqual([], invariantes.verificar(self.volume))

    def test_proporcoes(self):
        contagens = invariantes.contagens(self.volume)
        self.assertAlmostEqual(massa.PEDIDOS_VOLUME * ESCALA_REDUZIDA, contagens["pedido"], delta=40)
        self.assertAlmostEqual(3.0, contagens["item_pedido"] / contagens["pedido"], delta=0.15)
        self.assertAlmostEqual(massa.CLIENTES_VOLUME * ESCALA_REDUZIDA, contagens["cliente"], delta=1)
        self.assertAlmostEqual(0.03, contagens["cancelamento_pedido"] / contagens["pedido"], delta=0.015)
        self.assertLessEqual(contagens["pagamento"], contagens["pedido"])

    def test_sem_notificacao(self):
        self.assertEqual([], self.volume.notificacoes)


if __name__ == "__main__":
    unittest.main()
