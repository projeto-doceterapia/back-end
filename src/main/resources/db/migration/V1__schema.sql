-- Baseline schema. Applied to the schema named in DB_URL, so charset and collation
-- come from the server (utf8mb4 / utf8mb4_0900_ai_ci on MySQL 8.4).
-- Enums are stored as VARCHAR rather than native ENUM: a new value needs no ALTER.
-- While no real data exists this file is edited in place and the database is recreated.

-- =====================================
-- Support tables
-- =====================================

CREATE TABLE categoria_produto (
    id_categoria_produto INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(100),
    descricao VARCHAR(255)
) ENGINE=InnoDB;

CREATE TABLE categoria_insumo (
    id_categoria_insumo INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(100),
    descricao VARCHAR(255)
) ENGINE=InnoDB;

-- =====================================
-- Main tables
-- =====================================

CREATE TABLE cliente (
    id_cliente INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(255),
    telefone VARCHAR(20),
    endereco VARCHAR(255),
    cep VARCHAR(8) NULL,
    tipo_pessoa VARCHAR(20),
    classificacao_cliente VARCHAR(20),
    status VARCHAR(20),
    observacao TEXT,

    CONSTRAINT ck_cliente_status
        CHECK (status IN ('ATIVO', 'INATIVO'))
) ENGINE=InnoDB;

CREATE TABLE produto (
    id_produto INT PRIMARY KEY AUTO_INCREMENT,
    fk_categoria_produto INT NOT NULL,
    nome VARCHAR(255),
    custo_estimado DECIMAL(10,2),
    preco_atual DECIMAL(10,2),
    preco_sugerido DECIMAL(10,2),
    margem_lucro DECIMAL(10,2),
    unidade_producao VARCHAR(20),
    status VARCHAR(20),
    quantidade_produzida INT,
    descricao VARCHAR(255),

    CONSTRAINT fk_produto_categoria
        FOREIGN KEY (fk_categoria_produto)
        REFERENCES categoria_produto (id_categoria_produto)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT ck_produto_status
        CHECK (status IN ('ATIVO', 'INATIVO'))
) ENGINE=InnoDB;

CREATE TABLE insumo (
    id_insumo INT PRIMARY KEY AUTO_INCREMENT,
    fk_categoria_insumo INT,
    nome VARCHAR(255),
    nome_normalizado VARCHAR(120),
    quantidade_atual DECIMAL(10,3) DEFAULT 0,
    quantidade_minima DECIMAL(10,3),
    unidade VARCHAR(20),
    custo_unitario DECIMAL(10,4),
    status VARCHAR(20),
    marca VARCHAR(255),

    CONSTRAINT fk_insumo_categoria
        FOREIGN KEY (fk_categoria_insumo)
        REFERENCES categoria_insumo (id_categoria_insumo)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT ck_insumo_status
        CHECK (status IN ('ATIVO', 'INATIVO')),

    KEY idx_insumo_nome_normalizado (nome_normalizado)
) ENGINE=InnoDB;

CREATE TABLE pedido (
    id_pedido INT PRIMARY KEY AUTO_INCREMENT,
    fk_cliente INT NOT NULL,
    tipo_pedido VARCHAR(20) NOT NULL,
    status_pedido VARCHAR(30) NOT NULL,
    forma_entrega VARCHAR(20),
    endereco_entrega VARCHAR(255),
    cep VARCHAR(8) NULL,
    data_criacao DATETIME(6),
    data_confirmacao DATETIME(6),
    data_entrega DATE,
    anotacao TEXT,
    confirmado_com_falta_insumo BIT(1) NOT NULL DEFAULT 0,

    CONSTRAINT fk_pedido_cliente
        FOREIGN KEY (fk_cliente)
        REFERENCES cliente (id_cliente)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE pagamento (
    id_pagamento INT PRIMARY KEY AUTO_INCREMENT,
    fk_pedido INT NOT NULL UNIQUE,
    valor_total DECIMAL(10,2),
    valor_sinal DECIMAL(10,2),
    valor_restante DECIMAL(10,2),
    status_pagamento VARCHAR(20) NOT NULL,
    forma_pagamento VARCHAR(20),
    forma_pagamento_sinal VARCHAR(30) NULL,
    forma_pagamento_restante VARCHAR(30) NULL,
    data_pagamento_sinal DATE,
    data_pagamento_restante DATE,

    CONSTRAINT fk_pagamento_pedido
        FOREIGN KEY (fk_pedido)
        REFERENCES pedido (id_pedido)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE cancelamento_pedido (
    id_cancelamento INT PRIMARY KEY AUTO_INCREMENT,
    fk_pedido INT NOT NULL UNIQUE,
    tipo_cancelamento VARCHAR(30) NOT NULL,
    tipo_retorno VARCHAR(30) NOT NULL,
    valor_retorno DECIMAL(10,2),
    data_cancelamento DATETIME(6),
    observacao TEXT,

    CONSTRAINT fk_cancelamento_pedido
        FOREIGN KEY (fk_pedido)
        REFERENCES pedido (id_pedido)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE item_pedido (
    id_produto_pedido INT PRIMARY KEY AUTO_INCREMENT,
    fk_produto INT NOT NULL,
    fk_pedido INT NOT NULL,
    quantidade DECIMAL(10,3),
    preco_original_produto DECIMAL(10,2),
    valor_unitario DECIMAL(10,2),
    valor_total DECIMAL(10,2),
    custo_estimado_item DECIMAL(10,2),
    margem_lucro_item DECIMAL(10,2),
    observacao TEXT,

    CONSTRAINT fk_item_pedido_produto
        FOREIGN KEY (fk_produto)
        REFERENCES produto (id_produto)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_item_pedido_pedido
        FOREIGN KEY (fk_pedido)
        REFERENCES pedido (id_pedido)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE producao (
    id_producao INT PRIMARY KEY AUTO_INCREMENT,
    fk_pedido INT NOT NULL UNIQUE,
    data_inicio DATE,
    data_prevista DATE,
    data_finalizacao DATE,
    status_producao VARCHAR(45) NOT NULL,
    observacao TEXT,

    CONSTRAINT fk_producao_pedido
        FOREIGN KEY (fk_pedido)
        REFERENCES pedido (id_pedido)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE produto_insumo (
    id_produto_insumo INT PRIMARY KEY AUTO_INCREMENT,
    fk_produto INT NOT NULL,
    fk_insumo INT NOT NULL,
    quantidade_utilizada DECIMAL(10,3) NOT NULL,
    unidade VARCHAR(20) NOT NULL,

    CONSTRAINT fk_produto_insumo_produto
        FOREIGN KEY (fk_produto)
        REFERENCES produto (id_produto)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_produto_insumo_insumo
        FOREIGN KEY (fk_insumo)
        REFERENCES insumo (id_insumo)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT uq_produto_insumo
        UNIQUE (fk_produto, fk_insumo)
) ENGINE=InnoDB;

CREATE TABLE usuario (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(255),
    email VARCHAR(255),
    senha VARCHAR(255),
    perfil VARCHAR(20) NOT NULL DEFAULT 'DONA',

    CONSTRAINT uk_usuario_email
        UNIQUE (email)
) ENGINE=InnoDB;

CREATE TABLE configuracao_farol_agenda (
    id_configuracao_farol INT PRIMARY KEY AUTO_INCREMENT,
    limite_verde INT NOT NULL,
    limite_amarelo INT NOT NULL,
    limite_vermelho INT NOT NULL
) ENGINE=InnoDB;

CREATE TABLE notificacao (
    id_notificacao INT PRIMARY KEY AUTO_INCREMENT,
    tipo VARCHAR(30) NOT NULL,
    titulo VARCHAR(150) NOT NULL,
    mensagem TEXT NOT NULL,
    data_criacao DATETIME(6) NOT NULL,
    lida BIT(1) NOT NULL DEFAULT 0,
    chave_unicidade VARCHAR(150),
    janela_inicio DATETIME(6),
    janela_fim DATETIME(6),
    template_key VARCHAR(80),
    parametros TEXT,
    status_envio VARCHAR(20) NOT NULL DEFAULT 'NAO_ENVIAR',
    enviado_em DATETIME(6),
    canal_message_id VARCHAR(120),
    fk_pedido INT,
    fk_insumo INT,

    CONSTRAINT uq_notificacao_chave_unicidade
        UNIQUE (chave_unicidade),

    CONSTRAINT fk_notificacao_pedido
        FOREIGN KEY (fk_pedido)
        REFERENCES pedido (id_pedido)
        ON DELETE SET NULL
        ON UPDATE CASCADE,

    CONSTRAINT fk_notificacao_insumo
        FOREIGN KEY (fk_insumo)
        REFERENCES insumo (id_insumo)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE configuracao_notificacao (
    id_configuracao_notificacao INT PRIMARY KEY AUTO_INCREMENT,
    inicio_expediente TIME NOT NULL DEFAULT '08:00:00',
    fim_expediente TIME NOT NULL DEFAULT '18:00:00',
    dias_expediente VARCHAR(20) NOT NULL DEFAULT '1,2,3,4,5,6',
    horario_resumo_inicio TIME NOT NULL DEFAULT '08:00:00',
    horario_resumo_fim TIME NOT NULL DEFAULT '17:30:00',
    dias_antecedencia INT NOT NULL DEFAULT 3,
    resumo_inicio_dia_ativo BIT(1) NOT NULL DEFAULT 1,
    resumo_fim_dia_ativo BIT(1) NOT NULL DEFAULT 1,
    alerta_estoque_baixo_ativo BIT(1) NOT NULL DEFAULT 1,
    alerta_dia_lotado_ativo BIT(1) NOT NULL DEFAULT 1,
    avisos_automaticos_ativo BIT(1) NOT NULL DEFAULT 1,
    assistente_ativo BIT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB;

CREATE TABLE configuracao_precificacao (
    id_configuracao_precificacao INT PRIMARY KEY AUTO_INCREMENT,
    margem_padrao DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    percentual_indiretos DECIMAL(5,2) NOT NULL DEFAULT 0.00
) ENGINE=InnoDB;

-- Append-only stock ledger. insumo.quantidade_atual is a cache written only by the
-- stock movement service; the true balance is the sum of the movements.
-- tipo and origem have no CHECK, so new enum values need no schema change.
CREATE TABLE movimentacao_estoque (
    id_movimentacao INT PRIMARY KEY AUTO_INCREMENT,
    fk_insumo INT NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    quantidade DECIMAL(10,3) NOT NULL,
    quantidade_informada DECIMAL(10,3) NOT NULL,
    unidade_informada VARCHAR(20) NOT NULL,
    custo_unitario DECIMAL(10,4),
    origem VARCHAR(20) NOT NULL,
    chave_nota VARCHAR(44),
    fluxo_id VARCHAR(255),
    chave_idempotencia VARCHAR(120),
    hash_conteudo VARCHAR(64) NOT NULL,
    ocorrido_em DATETIME(6) NOT NULL,
    criado_em DATETIME(6) NOT NULL,

    CONSTRAINT fk_movimentacao_insumo
        FOREIGN KEY (fk_insumo)
        REFERENCES insumo (id_insumo)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT uq_movimentacao_chave_idempotencia
        UNIQUE (chave_idempotencia),

    KEY idx_movimentacao_chave_nota (chave_nota)
) ENGINE=InnoDB;

-- =====================================
-- Candidate indexes
-- =====================================

-- Candidate: serves the Agenda, Producao, Historico and indicator queries that filter
-- by status_pedido and a data_entrega range. Kept or dropped by EXPLAIN under volume.
CREATE INDEX idx_pedido_status_entrega ON pedido (status_pedido, data_entrega);

-- Candidate: serves the Estoque screen, which lists the last 30 days by ocorrido_em.
-- Kept or dropped by EXPLAIN under volume.
CREATE INDEX idx_movimentacao_ocorrido_em ON movimentacao_estoque (ocorrido_em);

-- =====================================
-- Single-row settings
-- =====================================

-- Every value comes from the column defaults, which match the entity defaults.
INSERT INTO configuracao_notificacao () VALUES ();

INSERT INTO configuracao_precificacao (margem_padrao, percentual_indiretos) VALUES (0.00, 0.00);
