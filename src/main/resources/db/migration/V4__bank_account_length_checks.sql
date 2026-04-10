ALTER TABLE bank_account
    ADD CONSTRAINT ck_bank_account_agencia_digits
        CHECK (agencia ~ '^[0-9]{4}$');

ALTER TABLE bank_account
    ADD CONSTRAINT ck_bank_account_numero_conta_digits
        CHECK (numeroConta ~ '^[0-9]{4,13}$');
