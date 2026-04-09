-- Flyway V3: categorias padrao por empresa

INSERT INTO transaction_category (name, tipo, active, fk_id_company)
SELECT 'Vendas', 'ENTRADA', TRUE, c.id
FROM company c
WHERE NOT EXISTS (
    SELECT 1
    FROM transaction_category tc
    WHERE tc.fk_id_company = c.id
      AND tc.name = 'Vendas'
);

INSERT INTO transaction_category (name, tipo, active, fk_id_company)
SELECT 'Fornecedores', 'SAIDA', TRUE, c.id
FROM company c
WHERE NOT EXISTS (
    SELECT 1
    FROM transaction_category tc
    WHERE tc.fk_id_company = c.id
      AND tc.name = 'Fornecedores'
);

INSERT INTO transaction_category (name, tipo, active, fk_id_company)
SELECT 'Impostos', 'SAIDA', TRUE, c.id
FROM company c
WHERE NOT EXISTS (
    SELECT 1
    FROM transaction_category tc
    WHERE tc.fk_id_company = c.id
      AND tc.name = 'Impostos'
);

CREATE OR REPLACE FUNCTION fn_create_default_transaction_categories()
RETURNS TRIGGER AS
$$
BEGIN
    INSERT INTO transaction_category (name, tipo, active, fk_id_company)
    VALUES ('Vendas', 'ENTRADA', TRUE, NEW.id);

    INSERT INTO transaction_category (name, tipo, active, fk_id_company)
    VALUES ('Fornecedores', 'SAIDA', TRUE, NEW.id);

    INSERT INTO transaction_category (name, tipo, active, fk_id_company)
    VALUES ('Impostos', 'SAIDA', TRUE, NEW.id);

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_create_default_transaction_categories ON company;

CREATE TRIGGER trg_create_default_transaction_categories
AFTER INSERT
ON company
FOR EACH ROW
EXECUTE FUNCTION fn_create_default_transaction_categories();
