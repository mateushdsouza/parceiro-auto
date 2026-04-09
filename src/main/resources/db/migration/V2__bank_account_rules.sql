-- Flyway V2: regras de integridade para conta bancaria

UPDATE bank_account
SET contaPadrao = FALSE
WHERE contaPadrao IS NULL;

ALTER TABLE bank_account
    ALTER COLUMN contaPadrao SET DEFAULT FALSE;

ALTER TABLE bank_account
    ALTER COLUMN contaPadrao SET NOT NULL;

ALTER TABLE bank_account
    ADD CONSTRAINT ck_bank_account_saldo_non_negative
        CHECK (saldo >= 0);

CREATE OR REPLACE FUNCTION fn_bank_account_single_default()
RETURNS TRIGGER AS
$$
BEGIN
    IF NEW.contaPadrao = TRUE THEN
        UPDATE bank_account
        SET contaPadrao = FALSE
        WHERE fk_id_company = NEW.fk_id_company
          AND id <> COALESCE(NEW.id, -1)
          AND contaPadrao = TRUE;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_bank_account_single_default ON bank_account;

CREATE TRIGGER trg_bank_account_single_default
BEFORE INSERT OR UPDATE OF contaPadrao, fk_id_company
ON bank_account
FOR EACH ROW
EXECUTE FUNCTION fn_bank_account_single_default();
