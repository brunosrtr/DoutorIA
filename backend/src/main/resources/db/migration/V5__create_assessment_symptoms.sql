CREATE TABLE assessment_symptoms (
    id                TEXT PRIMARY KEY,
    assessment_id     TEXT NOT NULL REFERENCES symptom_assessments(id) ON DELETE CASCADE,
    symptom_id        TEXT REFERENCES symptoms_catalog(id) ON DELETE SET NULL,
    nome_custom       TEXT,
    intensidade       INTEGER NOT NULL,
    duracao_dias      INTEGER,
    localizacao_corpo TEXT,
    observacoes       TEXT,
    created_at        TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%SZ','now')),

    CONSTRAINT chk_intensidade CHECK (intensidade BETWEEN 1 AND 10),
    CONSTRAINT chk_duracao     CHECK (duracao_dias IS NULL OR duracao_dias >= 0)
);

CREATE INDEX idx_assessment_symptoms_assessment ON assessment_symptoms(assessment_id);
