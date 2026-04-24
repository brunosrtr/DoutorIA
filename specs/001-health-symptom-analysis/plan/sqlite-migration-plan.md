# Plano: Migração PostgreSQL → SQLite

**Feature**: `specs/001-health-symptom-analysis`
**Data**: 2026-04-23
**Objetivo**: Eliminar a dependência do Docker/PostgreSQL para execução local.
**Research**: `plan/research.md` §9
**DDL de referência**: `plan/data-model.md` — seção "Migrations SQLite"

---

## Resumo do Impacto

| Camada | Mudanças |
|---|---|
| `pom.xml` | Remove 2 deps PostgreSQL/Flyway-PG, adiciona SQLite JDBC + Hibernate community dialects |
| `application.yml` | Troca datasource URL, driver, dialect |
| Flyway migrations (V1–V9) | Reescreve todos os 9 arquivos SQL |
| JPA entities (6 arquivos) | Remove anotações PostgreSQL-específicas; adiciona `@Convert` para arrays |
| Novo `StringArrayConverter.java` | Serializa `String[]` ↔ JSON string |
| `docker-compose.yml` | Remove serviço `postgres`; adiciona volume para `doutor_ia.db` |

**Nenhuma mudança necessária** em: controllers, services, mappers, DTOs, frontend, repositories.

---

## Tarefas — Sequência de Implementação

### S1 — pom.xml

**Arquivo**: `backend/pom.xml`

Remover:
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

Adicionar (antes de `<!-- JSON -->`):
```xml
<!-- SQLite -->
<dependency>
    <groupId>org.xerial</groupId>
    <artifactId>sqlite-jdbc</artifactId>
    <version>3.47.0.0</version>
</dependency>
<dependency>
    <groupId>org.hibernate.orm</groupId>
    <artifactId>hibernate-community-dialects</artifactId>
</dependency>
```

---

### S2 — application.yml

**Arquivo**: `backend/src/main/resources/application.yml`

Substituir bloco `datasource` e adicionar `database-platform`:

```yaml
spring:
  datasource:
    url: jdbc:sqlite:./doutor_ia.db
    driver-class-name: org.sqlite.JDBC

  jpa:
    database-platform: org.hibernate.community.dialect.SQLiteDialect
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        format_sql: false

  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: false

  servlet:
    multipart:
      enabled: true
      max-file-size: 10MB
      max-request-size: 50MB

server:
  port: 8080

gemini:
  api:
    key: ${GEMINI_API_KEY:}

upload:
  dir: ${UPLOAD_DIR:./uploads}
```

**Notas**:
- Remover `DB_URL`, `DB_USER`, `DB_PASSWORD` — SQLite não usa usuário/senha.
- `UPLOAD_DIR` default muda de `/data/uploads` para `./uploads` (caminho relativo local).

---

### S3 — Migrations Flyway (V1–V9)

**Diretório**: `backend/src/main/resources/db/migration/`

Substituir o conteúdo de **cada arquivo** pelo DDL SQLite da seção "Migrations SQLite" do `data-model.md`.

| Arquivo | Ação | Resumo da mudança |
|---|---|---|
| `V1__create_enums.sql` | Substituir | `SELECT 1;` (no-op — SQLite sem enums nativos) |
| `V2__create_patients.sql` | Substituir | UUID→TEXT, timestamp→TEXT, array→TEXT JSON, `sexo` como CHECK |
| `V3__create_symptoms_catalog.sql` | Substituir | UUID→TEXT, array→TEXT JSON, remove índice `gin_trgm` |
| `V4__create_assessments.sql` | Substituir | UUID→TEXT, enum→TEXT+CHECK, BOOLEAN→INTEGER |
| `V5__create_assessment_symptoms.sql` | Substituir | UUID→TEXT, adiciona `created_at` |
| `V6__create_medical_reports.sql` | Substituir | UUID→TEXT, enum→TEXT+CHECK, BIGINT→INTEGER |
| `V7__create_diagnoses.sql` | Substituir | UUID→TEXT, enum→TEXT+CHECK, DECIMAL→REAL, adiciona `created_at` |
| `V8__create_suggestions.sql` | Substituir | UUID→TEXT, enum→TEXT+CHECK, adiciona `created_at` |
| `V9__seed_symptoms_catalog.sql` | Substituir | `ARRAY[...]` → `'[...]'` JSON, `ON CONFLICT DO NOTHING` → `INSERT OR IGNORE INTO` |

> **Atenção checksum Flyway**: Se o banco já existir com migrations anteriores, deletar o arquivo `doutor_ia.db` antes de subir o backend com as novas migrations.

---

### S4 — StringArrayConverter

**Novo arquivo**: `backend/src/main/java/com/doutor/converter/StringArrayConverter.java`

```java
package com.doutor.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class StringArrayConverter implements AttributeConverter<String[], String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(String[] attribute) {
        try {
            return MAPPER.writeValueAsString(attribute == null ? new String[0] : attribute);
        } catch (Exception e) {
            return "[]";
        }
    }

    @Override
    public String[] convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return new String[0];
        try {
            return MAPPER.readValue(dbData, new TypeReference<String[]>() {});
        } catch (Exception e) {
            return new String[0];
        }
    }
}
```

---

### S5 — Entity: Patient.java

**Arquivo**: `backend/src/main/java/com/doutor/domain/entity/Patient.java`

Remover imports:
```java
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
```

Remover do campo `sexo`:
```java
@JdbcTypeCode(SqlTypes.NAMED_ENUM)           // remover
@Column(nullable = false, columnDefinition = "sexo_enum")  // mudar para:
@Column(nullable = false)                    // resultado
```

Modificar campo `alergiasConhecidas`:
```java
// Antes:
@Column(name = "alergias_conhecidas", columnDefinition = "text[]")
@JdbcTypeCode(SqlTypes.ARRAY)
private String[] alergiasConhecidas = new String[0];

// Depois:
@Convert(converter = com.doutor.converter.StringArrayConverter.class)
@Column(name = "alergias_conhecidas", nullable = false)
private String[] alergiasConhecidas = new String[0];
```

---

### S6 — Entity: SymptomAssessment.java

**Arquivo**: `backend/src/main/java/com/doutor/domain/entity/SymptomAssessment.java`

Modificar campo `status`:
```java
// Antes:
@Column(nullable = false, columnDefinition = "assessment_status_enum")

// Depois:
@Column(nullable = false)
```

Modificar campo `resumoPaciente`:
```java
// Antes:
@Column(name = "resumo_paciente", columnDefinition = "text")

// Depois:
@Column(name = "resumo_paciente")
```

---

### S7 — Entity: MedicalReport.java

**Arquivo**: `backend/src/main/java/com/doutor/domain/entity/MedicalReport.java`

Modificar campo `ocrStatus`:
```java
// Antes:
@Column(name = "ocr_status", nullable = false, columnDefinition = "ocr_status_enum")

// Depois:
@Column(name = "ocr_status", nullable = false)
```

---

### S8 — Entity: Diagnosis.java

**Arquivo**: `backend/src/main/java/com/doutor/domain/entity/Diagnosis.java`

Modificar campo `gravidade`:
```java
// Antes:
@Column(nullable = false, columnDefinition = "gravidade_enum")

// Depois:
@Column(nullable = false)
```

Modificar campo `justificativa`:
```java
// Antes:
@Column(columnDefinition = "text")

// Depois:
@Column
```

---

### S9 — Entity: Suggestion.java

**Arquivo**: `backend/src/main/java/com/doutor/domain/entity/Suggestion.java`

Modificar campo `tipo`:
```java
// Antes:
@Column(nullable = false, columnDefinition = "sugestao_tipo_enum")

// Depois:
@Column(nullable = false)
```

Modificar campo `descricao`:
```java
// Antes:
@Column(nullable = false, columnDefinition = "text")

// Depois:
@Column(nullable = false)
```

---

### S10 — Entity: SymptomsCatalog.java

**Arquivo**: `backend/src/main/java/com/doutor/domain/entity/SymptomsCatalog.java`

Remover imports:
```java
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
```

Modificar campo `descricao`:
```java
// Antes:
@Column(columnDefinition = "text")

// Depois:
@Column
```

Modificar campo `sinonimos`:
```java
// Antes:
@Column(columnDefinition = "text[]")
@JdbcTypeCode(SqlTypes.ARRAY)
private String[] sinonimos = new String[0];

// Depois:
@Convert(converter = com.doutor.converter.StringArrayConverter.class)
@Column(nullable = false)
private String[] sinonimos = new String[0];
```

---

### S11 — Entity: AssessmentSymptom.java

**Arquivo**: `backend/src/main/java/com/doutor/domain/entity/AssessmentSymptom.java`

Modificar campo `observacoes`:
```java
// Antes:
@Column(columnDefinition = "text")

// Depois:
@Column
```

---

### S12 — docker-compose.yml

**Arquivo**: `docker-compose.yml` na raiz

Remover serviço `postgres` e volume `postgres_data`. Ajustar serviço `backend`:

```yaml
version: '3.9'

services:
  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: doutor_ia_backend
    environment:
      GEMINI_API_KEY: ${GEMINI_API_KEY}
      UPLOAD_DIR: /data/uploads
    ports:
      - "8080:8080"
    volumes:
      - uploads_data:/data/uploads
      - sqlite_data:/app/data

volumes:
  uploads_data:
  sqlite_data:
```

**Nota sobre JDBC URL com Docker**: Quando rodando via container, alterar URL para apontar ao volume:
```yaml
environment:
  SPRING_DATASOURCE_URL: jdbc:sqlite:/app/data/doutor_ia.db
```

Para desenvolvimento local (sem Docker): `./doutor_ia.db` no diretório do backend.

---

### S13 — .env e .env.example

**Arquivo**: `.env` e `.env.example` na raiz

Remover variáveis PostgreSQL:
```
# Remover:
POSTGRES_DB=doutor_ia
POSTGRES_USER=doutor
POSTGRES_PASSWORD=doutor123
DB_URL=jdbc:postgresql://localhost:5432/doutor_ia
DB_USER=doutor
DB_PASSWORD=doutor123
```

Manter:
```
GEMINI_API_KEY=your_gemini_api_key_here
UPLOAD_DIR=./uploads
NEXT_PUBLIC_API_URL=http://localhost:8080
```

---

## Verificação pós-migração

```bash
# 1. Compilar com Java 21
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home \
  mvn compile -q

# 2. Subir o backend (cria doutor_ia.db automaticamente)
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home \
  mvn spring-boot:run

# Esperado no log:
# "Successfully applied 9 migrations to schema"
# "Started DoutorIaApplication in X.X seconds"

# 3. Verificar seed
curl http://localhost:8080/api/symptoms?q=dor
# Esperado: array com sintomas do catálogo

# 4. Subir frontend
cd frontend && npm run dev
# Acessar http://localhost:3000
```

---

## Riscos e Mitigações

| Risco | Probabilidade | Mitigação |
|---|---|---|
| Flyway checksum mismatch (banco anterior) | Alta se já rodou migrations PG | Deletar `doutor_ia.db` antes de subir |
| Hibernate não mapear UUID corretamente como TEXT | Média | SQLite dialect mapeia UUID para TEXT; testar com `POST /api/patients` |
| `LOWER(CONCAT(...))` no JPQL não traduzido corretamente | Baixa | SQLite suporta `LOWER()` e `||`; Hibernate dialect traduz CONCAT para `||` |
| `OffsetDateTime` serializado de forma inesperada | Baixa | Dialect armazena como ISO 8601 TEXT; formato é suficiente para ordenação e display |
