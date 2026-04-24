CREATE TABLE patients (
    id                  TEXT PRIMARY KEY,
    nome                TEXT NOT NULL,
    data_nascimento     TEXT,
    sexo                TEXT NOT NULL DEFAULT 'nao_informado'
                            CHECK (sexo IN ('masculino','feminino','outro','nao_informado')),
    tipo_sanguineo      TEXT CHECK (tipo_sanguineo IS NULL OR tipo_sanguineo IN ('A+','A-','B+','B-','AB+','AB-','O+','O-')),
    alergias_conhecidas TEXT NOT NULL DEFAULT '[]',
    created_at          TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%SZ','now'))
);

CREATE INDEX idx_patients_nome ON patients(nome);
