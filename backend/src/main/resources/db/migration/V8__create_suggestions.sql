CREATE TABLE suggestions (
    id           TEXT PRIMARY KEY,
    diagnosis_id TEXT NOT NULL REFERENCES diagnoses(id) ON DELETE CASCADE,
    tipo         TEXT NOT NULL
                     CHECK (tipo IN ('especialista','exame','habito','urgencia')),
    descricao    TEXT NOT NULL,
    prioridade   INTEGER NOT NULL DEFAULT 3,
    created_at   TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%SZ','now')),

    CONSTRAINT chk_prioridade CHECK (prioridade BETWEEN 1 AND 5)
);

CREATE INDEX idx_suggestions_diagnosis ON suggestions(diagnosis_id);
CREATE INDEX idx_suggestions_tipo      ON suggestions(tipo);
