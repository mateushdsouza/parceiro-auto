-- Flyway V1: cria as tabelas do projeto (PostgreSQL)

CREATE TABLE users
(
    id       BIGSERIAL PRIMARY KEY,
    login    VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(20) NOT NULL
);

CREATE TABLE company
(
    id           BIGSERIAL PRIMARY KEY,
    cnpj         VARCHAR(14) NOT NULL UNIQUE,
    razaoSocial  VARCHAR(50) NOT NULL,
    nomeFantasia VARCHAR(50) NOT NULL
);

CREATE TABLE transaction_category
(
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(50) NOT NULL,
    tipo          VARCHAR(50) NOT NULL,
    active        BOOLEAN     NOT NULL,
    fk_id_company BIGINT      NOT NULL,
    CONSTRAINT fk_transaction_category_company FOREIGN KEY (fk_id_company) REFERENCES company (id),
    CONSTRAINT ux_transaction_category_company_name UNIQUE (fk_id_company, name)
);

CREATE TABLE user_company
(
    id            BIGSERIAL PRIMARY KEY,
    fk_id_company BIGINT       NOT NULL,
    fk_id_user    BIGINT       NOT NULL,
    role          VARCHAR(255) NOT NULL,
    CONSTRAINT fk_user_company_company FOREIGN KEY (fk_id_company) REFERENCES company (id),
    CONSTRAINT fk_user_company_user FOREIGN KEY (fk_id_user) REFERENCES users (id),
    CONSTRAINT ux_user_company UNIQUE (fk_id_company, fk_id_user)
);

CREATE TABLE bank_account
(
    id            BIGSERIAL PRIMARY KEY,
    banco         VARCHAR(50)    NOT NULL,
    agencia       VARCHAR(10)    NOT NULL,
    numeroConta   VARCHAR(20)    NOT NULL,
    tipoConta     VARCHAR(20)    NOT NULL,
    saldo         NUMERIC(19, 2) NOT NULL DEFAULT 0,
    contaPadrao   BOOLEAN,
    fk_id_company BIGINT         NOT NULL,
    CONSTRAINT fk_bank_account_company FOREIGN KEY (fk_id_company) REFERENCES company (id),
    CONSTRAINT ux_bank_account_company UNIQUE (fk_id_company, banco, agencia, numeroConta)
);

CREATE TABLE transactions
(
    id                         BIGSERIAL PRIMARY KEY,
    fk_id_company              BIGINT         NOT NULL,
    fk_id_bank_account         BIGINT,
    fk_id_transaction_category BIGINT,
    tipo                       VARCHAR(255)   NOT NULL,
    descricao                  VARCHAR(255)   NOT NULL,
    valor                      NUMERIC(19, 2) NOT NULL,
    data                       DATE           NOT NULL,
    forma                      VARCHAR(255)   NOT NULL,
    CONSTRAINT fk_transactions_company FOREIGN KEY (fk_id_company) REFERENCES company (id),
    CONSTRAINT fk_transactions_bank_account FOREIGN KEY (fk_id_bank_account) REFERENCES bank_account (id),
    CONSTRAINT fk_transactions_transaction_category FOREIGN KEY (fk_id_transaction_category) REFERENCES transaction_category (id)
);

CREATE TABLE recurrence_rule
(
    id                BIGSERIAL PRIMARY KEY,
    fk_id_transaction BIGINT       NOT NULL UNIQUE,
    frequencia        VARCHAR(255) NOT NULL,
    diaExecucao       INT          NOT NULL CHECK (diaExecucao BETWEEN 1 AND 31),
    dataInicio        DATE         NOT NULL,
    dataFim           DATE,
    CONSTRAINT fk_recurrence_rule_transaction FOREIGN KEY (fk_id_transaction) REFERENCES transactions (id)
);
