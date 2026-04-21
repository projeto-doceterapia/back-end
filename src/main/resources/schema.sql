
CREATE TABLE categoria_produto (
    id_categoria_produto INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(45) NOT NULL,
    descricao VARCHAR(245)
);

CREATE TABLE categoria_insumo (
    id_categoria_insumo INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(45) NOT NULL,
    descricao VARCHAR(245)
);

-- =====================================
-- TABELAS PRINCIPAIS
-- =====================================

CREATE TABLE cliente (
    id_cliente INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(45) NOT NULL,
    telefone CHAR(12) NOT NULL,
    endereco VARCHAR(245) NOT NULL,
    tipo_cliente VARCHAR(45),
    status TINYINT NOT NULL DEFAULT 1,
    observacao VARCHAR(245),

    CONSTRAINT chk_cliente_status
        CHECK (status IN (0, 1))
);

CREATE TABLE produto (
    id_produto INT PRIMARY KEY AUTO_INCREMENT,
    fk_categoria_produto INT NOT NULL,
    nome VARCHAR(45) NOT NULL,
    custo_estimado DECIMAL(10,2),
    preco_atual DECIMAL(10,2),
    preco_sugerido DECIMAL(10,2),
    margem_lucro DECIMAL(10,2),
    unidade_producao VARCHAR(45),
    status TINYINT NOT NULL DEFAULT 1,
    descricao VARCHAR(245),

    CONSTRAINT fk_produto_categoria
        FOREIGN KEY (fk_categoria_produto)
        REFERENCES categoria_produto(id_categoria_produto)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT chk_produto_status
        CHECK (status IN (0, 1)),

    CONSTRAINT chk_produto_custo
        CHECK (custo_estimado IS NULL OR custo_estimado >= 0),

    CONSTRAINT chk_produto_preco_atual
        CHECK (preco_atual IS NULL OR preco_atual >= 0),

    CONSTRAINT chk_produto_preco_sugerido
        CHECK (preco_sugerido IS NULL OR preco_sugerido >= 0)
);

CREATE TABLE insumo (
    id_insumo INT PRIMARY KEY AUTO_INCREMENT,
    fk_categoria_insumo INT NOT NULL,
    nome VARCHAR(45) NOT NULL,
    quantidade_atual DECIMAL(10,3) NOT NULL DEFAULT 0,
    quantidade_minima DECIMAL(10,3) NOT NULL DEFAULT 0,
    unidade VARCHAR(45) NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    marca VARCHAR(45),

    CONSTRAINT fk_insumo_categoria
        FOREIGN KEY (fk_categoria_insumo)
        REFERENCES categoria_insumo(id_categoria_insumo)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT chk_insumo_status
        CHECK (status IN (0, 1)),

    CONSTRAINT chk_insumo_qtd_atual
        CHECK (quantidade_atual >= 0),

    CONSTRAINT chk_insumo_qtd_minima
        CHECK (quantidade_minima >= 0)
);

CREATE TABLE pedido (
    id_pedido INT PRIMARY KEY AUTO_INCREMENT,
    fk_cliente INT NOT NULL,
    tipo_pedido VARCHAR(45) NOT NULL,
    status_pedido VARCHAR(245) NOT NULL,
    forma_entrega VARCHAR(45) NOT NULL,
    endereco_entrega VARCHAR(225),
    data_criacao DATE NOT NULL,
    data_entrega DATE,
    anotacao VARCHAR(245),

    CONSTRAINT fk_pedido_cliente
        FOREIGN KEY (fk_cliente)
        REFERENCES cliente(id_cliente)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

CREATE TABLE pagamento (
    id_pagamento INT PRIMARY KEY AUTO_INCREMENT,
    fk_pedido INT NOT NULL UNIQUE,
    valor_total DECIMAL(10,2) NOT NULL,
    valor_sinal DECIMAL(10,2) NOT NULL,
    valor_restante DECIMAL(10,2) NOT NULL,
    status_pagamento VARCHAR(245) NOT NULL,

    CONSTRAINT fk_pagamento_pedido
        FOREIGN KEY (fk_pedido)
        REFERENCES pedido(id_pedido)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT chk_pagamento_total
        CHECK (valor_total >= 0),

    CONSTRAINT chk_pagamento_sinal
        CHECK (valor_sinal >= 0),

    CONSTRAINT chk_pagamento_restante
        CHECK (valor_restante >= 0)
);

CREATE TABLE cancelamento_pedido (
    id_cancelamento INT PRIMARY KEY AUTO_INCREMENT,
    fk_pedido INT NOT NULL UNIQUE,
    tipo_cancelamento VARCHAR(45) NOT NULL,
    valor_retorno DECIMAL(10,2),
    data_cancelamento DATE NOT NULL,
    observacao VARCHAR(245),

    CONSTRAINT fk_cancelamento_pedido
        FOREIGN KEY (fk_pedido)
        REFERENCES pedido(id_pedido)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT chk_cancelamento_valor
        CHECK (valor_retorno IS NULL OR valor_retorno >= 0)
);

CREATE TABLE item_pedido (
    id_produto_pedido INT PRIMARY KEY AUTO_INCREMENT,
    fk_produto INT NOT NULL,
    fk_pedido INT NOT NULL,
    quantidade INT NOT NULL,
    valor_unitario DECIMAL(10,2) NOT NULL,
    valor_total DECIMAL(10,2) NOT NULL,
    observacao VARCHAR(245),

    CONSTRAINT fk_item_pedido_produto
        FOREIGN KEY (fk_produto)
        REFERENCES produto(id_produto)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_item_pedido_pedido
        FOREIGN KEY (fk_pedido)
        REFERENCES pedido(id_pedido)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT chk_item_pedido_quantidade
        CHECK (quantidade > 0),

    CONSTRAINT chk_item_pedido_valor_unitario
        CHECK (valor_unitario >= 0),

    CONSTRAINT chk_item_pedido_valor_total
        CHECK (valor_total >= 0)
);

CREATE TABLE producao (
    id_producao INT PRIMARY KEY AUTO_INCREMENT,
    fk_pedido INT NOT NULL,
    fk_item_pedido INT NOT NULL,
    data_inicio DATE,
    data_prevista DATE,
    status_producao VARCHAR(45) NOT NULL,
    prioridade VARCHAR(45),
    observacao VARCHAR(245),

    CONSTRAINT fk_producao_pedido
        FOREIGN KEY (fk_pedido)
        REFERENCES pedido(id_pedido)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_producao_item_pedido
        FOREIGN KEY (fk_item_pedido)
        REFERENCES item_pedido(id_produto_pedido)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE produto_insumo (
    id_produto_insumo INT PRIMARY KEY AUTO_INCREMENT,
    fk_produto INT NOT NULL,
    fk_insumo INT NOT NULL,
    quantidade_utilizada DECIMAL(10,3) NOT NULL,
    unidade VARCHAR(45) NOT NULL,

    CONSTRAINT fk_produto_insumo_produto
        FOREIGN KEY (fk_produto)
        REFERENCES produto(id_produto)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_produto_insumo_insumo
        FOREIGN KEY (fk_insumo)
        REFERENCES insumo(id_insumo)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT chk_produto_insumo_qtd
        CHECK (quantidade_utilizada > 0),

    CONSTRAINT uq_produto_insumo
        UNIQUE (fk_produto, fk_insumo)
);
