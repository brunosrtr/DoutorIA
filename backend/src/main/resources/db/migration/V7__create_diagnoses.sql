CREATE TABLE diagnoses (
    id                   TEXT PRIMARY KEY,
    assessment_id        TEXT NOT NULL REFERENCES symptom_assessments(id) ON DELETE CASCADE,
    nome_diagnostico     TEXT NOT NULL,
    cid_codigo           TEXT,
    confianca_percentual REAL,
    gravidade            TEXT NOT NULL
                             CHECK (gravidade IN ('baixa','media','alta','critica')),
    justificativa        TEXT,
    created_at           TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%SZ','now')),

    CONSTRAINT chk_confianca CHECK (
        confianca_percentual IS NULL OR
        confianca_percentual BETWEEN 0.0 AND 100.0
    )
);

CREATE INDEX idx_diagnoses_assessment ON diagnoses(assessment_id);
CREATE INDEX idx_diagnoses_gravidade  ON diagnoses(gravidade);
