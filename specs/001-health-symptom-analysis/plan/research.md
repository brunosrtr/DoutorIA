# Research: Análise de Sintomas — Decisões Técnicas

**Feature**: `specs/001-health-symptom-analysis`
**Data**: 2026-04-17 (atualizado 2026-04-23)
**Status**: Resolvido

---

## 1. OCR vs. Multimodal Nativo do Gemini

**Decisão**: Abordagem 2 — Multimodal nativo (Gemini 1.5 Pro)

**Rationale**:
- Gemini 1.5 Pro suporta envio de PDFs e imagens diretamente via `inlineData` (base64) ou via File API para arquivos > 20 MB.
- Elimina dependência de Tika/Tesseract (menos complexidade, menos dependências no pom.xml).
- Qualidade superior para imagens de baixa resolução ou PDFs com layout complexo de laudos médicos.
- Para uso local com arquivos de até 10 MB, `inlineData` é a escolha mais simples e direta.

**Alternativas consideradas**:
- Apache Tika: boa extração de texto de PDFs, mas não processa imagens de forma semântica.
- Tesseract: OCR clássico, exige instalação de binário, qualidade inferior para layouts médicos.

**Constraint importante**: O Gemini 1.5 Pro aceita `inlineData` para arquivos até 20 MB. Como o limite da aplicação é 10 MB, o fluxo multimodal cobre 100% dos casos sem precisar da File API do Gemini.

---

## 2. SDK do Google para Java

**Decisão**: `com.google.cloud:google-cloud-vertexai` (via Vertex AI) **ou** SDK direto `com.google.ai.client.generativeai:generativeai` (Google AI Studio)

**Rationale**:
- Para projeto pessoal/local, o SDK do **Google AI Studio** (`com.google.ai.client.generativeai`) é mais simples: não exige GCP project, basta uma API key.
- Biblioteca Java: `com.google.ai.client.generativeai:generativeai:0.9.0+`
- Suporta `GenerationConfig` com `responseMimeType = "application/json"` para forçar JSON output.
- Suporta `responseSchema` (via `Schema` class) para validar o JSON gerado.

**Maven coordinates**:
```xml
<dependency>
  <groupId>com.google.ai.client.generativeai</groupId>
  <artifactId>generativeai</artifactId>
  <version>0.9.0</version>
</dependency>
```

**Nota**: A API de response schema no SDK Java pode exigir `GenerationConfig.Builder.setResponseSchema()` — verificar versão do SDK na hora da implementação; alternativa é usar Gson para parsear o JSON livre.

---

## 3. Armazenamento de Arquivos (Local vs. Supabase Storage)

**Decisão**: **Sistema de arquivos local** com volume Docker para ambiente de desenvolvimento

**Rationale**:
- O projeto roda apenas localmente; Supabase Storage requer conexão com internet e configuração de bucket.
- Arquivos salvos em `/data/uploads/{patient_id}/{assessment_id}/` no container.
- O caminho é persistido na coluna `file_path` da tabela `medical_reports`.
- Migração futura para Supabase Storage é trivial: trocar a implementação de `FileStorageService`.

**Alternativa mantida como opção**: Supabase Storage pode ser habilitado adicionando as variáveis `SUPABASE_URL` e `SUPABASE_SERVICE_KEY` — a abstração de `FileStorageService` (interface) permite trocar a implementação sem mudar o restante do código.

---

## 4. Autenticação (Contexto Local)

**Decisão**: **Sem autenticação** na versão local

**Rationale**:
- Projeto pessoal, uso local. O usuário informou explicitamente que não precisa de autenticação complexa.
- Paciente padrão (ou seleção simples de paciente por lista) no frontend.
- O isolamento de dados é por `patient_id` passado nas requests — suficiente para uso pessoal.

**Nota**: A especificação original (`spec.md`) foi criada para uma versão pública com auth. O plano técnico atual é a versão local simplificada. Se o projeto evoluir para multi-usuário, adicionar Spring Security + JWT é a extensão natural.

---

## 5. PostgreSQL: Supabase (local) vs. Docker PostgreSQL direto

**Decisão**: **PostgreSQL via Docker** (imagem `postgres:16-alpine`)

**Rationale**:
- Supabase local (`supabase start`) requer Docker com 7+ GB de RAM e sobe ~10 containers. Para uso local pessoal, é excessivo.
- `postgres:16-alpine` é a imagem mínima, sobe em segundos.
- Supabase pode ser usado como banco remoto no futuro apenas adicionando a connection string.

---

## 6. Frontend: Polling vs. Resposta Síncrona

**Decisão**: **Resposta síncrona** no endpoint `POST /api/assessments/{id}/submit`

**Rationale**:
- A análise do Gemini leva 5–20s. Para uso local pessoal, manter a request aberta (timeout configurado para 60s no Spring e no Next.js) é mais simples que implementar polling.
- O endpoint retorna o resultado completo na mesma resposta HTTP 200.
- Se o Gemini demorar > 30s, o endpoint retorna 202 + `status: processando` e o frontend faz polling a cada 3s no `GET /api/assessments/{id}/result`.

---

## 7. Flyway vs. Liquibase

**Decisão**: **Flyway** com migrações SQL puras

**Rationale**:
- Mais simples para um projeto do zero.
- Convenção de nomenclatura: `V{N}__{descricao}.sql` (dois underscores).
- Spring Boot auto-configura Flyway via `spring-boot-starter-jpa` + `flyway-core`.

---

## 8. Body Map (Frontend)

**Decisão**: **Dropdown de regiões corporais** em vez de SVG interativo

**Rationale**:
- SVG interativo exige biblioteca adicional ou implementação manual complexa.
- Dropdown com regiões pré-definidas (cabeça, tórax, abdômen, costas, membros superiores, membros inferiores, geral) é suficiente para o caso de uso.
- Pode ser evoluído para SVG interativo numa iteração futura.

---

## 9. Banco de Dados: SQLite em vez de PostgreSQL

> Decisão tomada em 2026-04-23 para eliminar a dependência do Docker na execução local.

**Decisão**: **SQLite via arquivo local** (`doutor_ia.db` na raiz do backend)

**Rationale**:
- Zero configuração: arquivo criado automaticamente pelo Flyway na primeira execução.
- Elimina o `docker compose up -d postgres` do fluxo de desenvolvimento.
- Dados persistem entre reinicializações do backend.
- Adequado para uso local pessoal (sem necessidade de concorrência alta).

**Alternativas consideradas**:
- H2 in-memory: descartado por não persistir dados entre reinicializações.
- H2 em modo arquivo + compatibilidade PostgreSQL: descartado por adicionar complexidade desnecessária (H2 ainda exige configuração de dialect e tem comportamentos diferentes do SQLite para arrays).
- Manter PostgreSQL via Docker: descartado pelo usuário por exigir Docker para rodar o projeto.

---

### 9.1 Dialect Hibernate para SQLite

**Decisão**: `org.hibernate.community.dialect.SQLiteDialect` de `org.hibernate.orm:hibernate-community-dialects`

**Rationale**:
- É o dialect oficial da comunidade Hibernate para SQLite, compatível com Hibernate 6.x (usado pelo Spring Boot 3.3).
- Suporta `GenerationType.UUID`, `@Enumerated(EnumType.STRING)`, e tipos básicos nativamente.
- Mapeia `OffsetDateTime` como `TEXT` (ISO 8601), o que é suficiente para o caso de uso.

**Maven coordinates**:
```xml
<dependency>
  <groupId>org.hibernate.orm</groupId>
  <artifactId>hibernate-community-dialects</artifactId>
</dependency>
<dependency>
  <groupId>org.xerial</groupId>
  <artifactId>sqlite-jdbc</artifactId>
  <version>3.47.0.0</version>
</dependency>
```

**Nota**: `hibernate-community-dialects` usa a versão gerenciada pelo Spring Boot parent — não precisa especificar versão explícita.

---

### 9.2 Enums PostgreSQL → TEXT com CHECK

**Decisão**: Substituir `CREATE TYPE xxx AS ENUM` por `TEXT NOT NULL CHECK (col IN (...))`

**Rationale**:
- SQLite não tem tipo `ENUM` nativo.
- CHECK constraints são suportadas e garantem integridade.
- No lado JPA, `@Enumerated(EnumType.STRING)` funciona nativamente com `TEXT` — apenas remover `columnDefinition = "xxx_enum"` e `@JdbcTypeCode(SqlTypes.NAMED_ENUM)`.

---

### 9.3 Arrays PostgreSQL (text[]) → JSON como TEXT

**Decisão**: Serializar `String[]` como JSON string via `@Convert(converter = StringArrayConverter.class)`

**Rationale**:
- SQLite não suporta arrays nativos.
- JSON é a representação mais robusta (lida com valores contendo vírgulas).
- Jackson (já no classpath) faz a serialização/deserialização.
- Afeta dois campos: `patients.alergias_conhecidas` e `symptoms_catalog.sinonimos`.

**Implementação**: `StringArrayConverter` — `@Converter(autoApply = false)` com `Jackson ObjectMapper`:
```java
public String convertToDatabaseColumn(String[] arr) {
    return objectMapper.writeValueAsString(arr == null ? new String[0] : arr);
}
public String[] convertToEntityAttribute(String dbData) {
    return objectMapper.readValue(dbData, String[].class);
}
```

---

### 9.4 UUID Storage

**Decisão**: UUID gerado em Java via `@GeneratedValue(strategy = GenerationType.UUID)`, armazenado como `TEXT` (36 chars)

**Rationale**:
- SQLite não tem `gen_random_uuid()` — as migrations não precisam de `DEFAULT gen_random_uuid()`.
- O Hibernate SQLite dialect armazena UUID como `TEXT` automaticamente.
- Sem alteração necessária nas entidades para UUID.

---

### 9.5 ILIKE → LOWER() + LIKE

**Decisão**: Manter JPQL com `LOWER(s.nome) LIKE LOWER(CONCAT('%', :query, '%'))` — sem mudança necessária

**Rationale**:
- O repositório já usa JPQL com `LOWER()` e `CONCAT()` (não SQL nativo com `ILIKE`).
- O Hibernate traduz JPQL `LOWER()` para SQLite `LOWER()` corretamente.
- JPQL `CONCAT()` é traduzido para `||` pelo SQLite dialect.
- Resultado: zero mudança necessária nos repositórios.

---

### 9.6 Seed V9 — ARRAY[] → JSON strings

**Decisão**: Substituir `ARRAY['a','b','c']` por `'["a","b","c"]'` e `ON CONFLICT (nome) DO NOTHING` por `INSERT OR IGNORE INTO`

**Rationale**:
- `ARRAY[...]` é sintaxe PostgreSQL; SQLite não suporta.
- `INSERT OR IGNORE INTO` é o equivalente SQLite de `ON CONFLICT DO NOTHING`.
- Os dados do seed são os mesmos — apenas a sintaxe muda.

---

### 9.7 docker-compose.yml

**Decisão**: Remover serviço `postgres` e ajustar serviço `backend` para montar volume do arquivo SQLite

**Rationale**:
- Sem PostgreSQL, o compose fica mais simples.
- O arquivo `doutor_ia.db` deve ser persistido via volume Docker para não se perder quando o container reiniciar.
- Para desenvolvimento local (sem Docker), o arquivo é criado no diretório onde o JAR é executado.
