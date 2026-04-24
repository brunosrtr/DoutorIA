CREATE TABLE symptom_assessments (
    id              TEXT PRIMARY KEY,
    patient_id      TEXT NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    status          TEXT NOT NULL DEFAULT 'rascunho'
                        CHECK (status IN ('rascunho','processando','concluido','erro')),
    resumo_paciente TEXT,
    urgente         INTEGER NOT NULL DEFAULT 0,
    created_at      TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%SZ','now')),
    updated_at      TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%SZ','now'))
);

CREATE INDEX idx_assessments_patient_id ON symptom_assessments(patient_id);
CREATE INDEX idx_assessments_status      ON symptom_assessments(status);
CREATE INDEX idx_assessments_created_at  ON symptom_assessments(created_at);
