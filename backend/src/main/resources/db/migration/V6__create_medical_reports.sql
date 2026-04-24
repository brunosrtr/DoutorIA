CREATE TABLE medical_reports (
    id              TEXT PRIMARY KEY,
    assessment_id   TEXT NOT NULL REFERENCES symptom_assessments(id) ON DELETE CASCADE,
    file_path       TEXT NOT NULL,
    file_name       TEXT NOT NULL,
    file_type       TEXT NOT NULL,
    file_size_bytes INTEGER NOT NULL,
    texto_extraido  TEXT,
    ocr_status      TEXT NOT NULL DEFAULT 'pendente'
                        CHECK (ocr_status IN ('pendente','processado','erro')),
    created_at      TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%SZ','now')),

    CONSTRAINT chk_file_size CHECK (file_size_bytes > 0 AND file_size_bytes <= 10485760),
    CONSTRAINT chk_file_type CHECK (file_type IN ('image/jpeg','image/png','application/pdf'))
);

CREATE INDEX idx_medical_reports_assessment ON medical_reports(assessment_id);
