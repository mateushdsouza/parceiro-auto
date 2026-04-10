ALTER TABLE recurrence_rule
    ADD COLUMN ultimaExecucao DATE;

UPDATE recurrence_rule
SET ultimaExecucao = dataInicio
WHERE ultimaExecucao IS NULL;

ALTER TABLE recurrence_rule
    ALTER COLUMN ultimaExecucao SET NOT NULL;
