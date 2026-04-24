CREATE TABLE symptoms_catalog (
    id        TEXT PRIMARY KEY,
    nome      TEXT NOT NULL,
    categoria TEXT NOT NULL,
    descricao TEXT,
    sinonimos TEXT NOT NULL DEFAULT '[]',

    CONSTRAINT uq_symptoms_catalog_nome UNIQUE (nome)
);

CREATE INDEX idx_symptoms_catalog_categoria ON symptoms_catalog(categoria);
