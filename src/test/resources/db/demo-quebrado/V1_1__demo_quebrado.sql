-- Deliberately out of step with the schema: the startup must fail on it.
INSERT INTO cliente (id_cliente, coluna_que_nao_existe) VALUES (1, 'x');
