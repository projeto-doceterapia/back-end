CREATE DATABASE IF NOT EXISTS agenda;
USE agenda;

CREATE TABLE cliente (
	idCliente       INT           PRIMARY KEY AUTO_INCREMENT,
    nome_completo   VARCHAR(45)   NOT NULL,
    telefone        VARCHAR(45)   NOT NULL,
    endereco        VARCHAR(255)  NOT NULL
);

CREATE TABLE pedido (
	idPedido          INT             PRIMARY KEY AUTO_INCREMENT,
    fkCliente         INT,
	descricao         TEXT            NOT NULL,
	data_entrega      DATE            NOT NULL,
    valor             DECIMAL(10,2)   NOT NULL,
    status_concluido  BOOLEAN         DEFAULT FALSE,

	CONSTRAINT fk_idCliente
		FOREIGN KEY (fkCliente)
			REFERENCES cliente(idCliente)
);