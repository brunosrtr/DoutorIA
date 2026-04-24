# API Contracts: DoutorIA Backend

**Base URL**: `http://localhost:8080/api`
**Content-Type**: `application/json` (exceto uploads: `multipart/form-data`)
**Autenticação**: Nenhuma (uso local pessoal)

---

## Patients

### `POST /api/patients`

Cria um novo paciente.

**Request**:
```json
{
  "nome": "João Silva",
  "dataNascimento": "1990-05-15",
  "sexo": "masculino",
  "tipoSanguineo": "A+",
  "alergiasConhecidas": ["penicilina", "AAS"]
}
```

**Response 201**:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "nome": "João Silva",
  "dataNascimento": "1990-05-15",
  "sexo": "masculino",
  "tipoSanguineo": "A+",
  "alergiasConhecidas": ["penicilina", "AAS"],
  "createdAt": "2026-04-17T10:30:00Z"
}
```

**Erros**:
- `400` — campos obrigatórios ausentes ou formato inválido
- `409` — paciente com mesmo nome e data de nascimento já existe

---

### `GET /api/patients`

Lista todos os pacientes (para seleção no wizard).

**Response 200**:
```json
[
  {
    "id": "550e8400-...",
    "nome": "João Silva",
    "dataNascimento": "1990-05-15",
    "sexo": "masculino",
    "tipoSanguineo": "A+"
  }
]
```

---

### `GET /api/patients/{id}`

Retorna detalhes de um paciente.

**Response 200**: objeto completo do paciente (igual ao POST 201).

**Erros**:
- `404` — paciente não encontrado

---

## Assessments

### `POST /api/assessments`

Cria uma nova avaliação em status `rascunho`.

**Request**:
```json
{
  "patientId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response 201**:
```json
{
  "id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "patientId": "550e8400-...",
  "status": "rascunho",
  "createdAt": "2026-04-17T10:31:00Z"
}
```

---

### `POST /api/assessments/{id}/symptoms`

Adiciona um ou mais sintomas à avaliação.

**Request**:
```json
{
  "sintomas": [
    {
      "symptomId": "uuid-do-sintoma-do-catalogo",
      "intensidade": 7,
      "duracaoDias": 3,
      "localizacaoCorpo": "cabeça",
      "observacoes": "Piora à tarde e com barulho"
    },
    {
      "symptomId": null,
      "nomeCustom": "Pressão ocular",
      "intensidade": 5,
      "duracaoDias": 1,
      "localizacaoCorpo": "cabeça",
      "observacoes": null
    }
  ]
}
```

> `symptomId` ou `nomeCustom` — um dos dois deve estar presente. `symptomId` referencia o catálogo; `nomeCustom` permite sintomas fora do catálogo.

**Response 200**:
```json
{
  "assessmentId": "f47ac10b-...",
  "sintomasAdicionados": 2
}
```

**Erros**:
- `404` — avaliação não encontrada
- `409` — avaliação não está em status `rascunho`

---

### `POST /api/assessments/{id}/reports`

Faz upload de um arquivo médico.

**Request**: `multipart/form-data`
- `file` — arquivo (JPG, PNG ou PDF, máx. 10 MB)

**Response 201**:
```json
{
  "id": "report-uuid",
  "assessmentId": "f47ac10b-...",
  "fileName": "hemograma.pdf",
  "fileType": "application/pdf",
  "fileSizeBytes": 204800,
  "ocrStatus": "pendente",
  "createdAt": "2026-04-17T10:32:00Z"
}
```

**Erros**:
- `400` — arquivo ausente, formato inválido ou tamanho excedido
- `404` — avaliação não encontrada
- `409` — avaliação não está em status `rascunho`
- `422` — limite de 5 arquivos por avaliação atingido

---

### `POST /api/assessments/{id}/submit`

Submete a avaliação para análise. Muda status para `processando`, envia ao Gemini, aguarda resposta e retorna resultado.

**Request**: sem body.

**Response 200** (análise concluída na mesma request):
```json
{
  "assessmentId": "f47ac10b-...",
  "status": "concluido",
  "urgente": false,
  "resumoGeral": "Os sintomas descritos sugerem episódio de enxaqueca...",
  "diagnosticos": [
    {
      "id": "diag-uuid-1",
      "nomeDiagnostico": "Enxaqueca (cefaleia migranosa)",
      "cidCodigo": "G43",
      "confiancaPercentual": 75.50,
      "gravidade": "media",
      "justificativa": "Cefaleia unilateral pulsátil com fotofobia e duração de 3 dias..."
    }
  ],
  "sugestoes": [
    {
      "id": "sug-uuid-1",
      "diagnosticId": "diag-uuid-1",
      "tipo": "especialista",
      "descricao": "Neurologista — para avaliação e tratamento da enxaqueca",
      "prioridade": 2
    },
    {
      "id": "sug-uuid-2",
      "diagnosticId": "diag-uuid-1",
      "tipo": "habito",
      "descricao": "Repouso em ambiente escuro e silencioso, hidratação adequada",
      "prioridade": 1
    }
  ]
}
```

**Response 202** (se Gemini não retornar em 30s — acionar polling):
```json
{
  "assessmentId": "f47ac10b-...",
  "status": "processando",
  "message": "Análise em andamento. Consulte GET /api/assessments/{id}/result"
}
```

**Erros**:
- `404` — avaliação não encontrada
- `409` — avaliação não está em status `rascunho`
- `422` — avaliação sem sintomas (mínimo 1 sintoma obrigatório)
- `500` — erro na comunicação com Gemini após retry (status muda para `erro`)

---

### `GET /api/assessments/{id}/result`

Busca o resultado de uma avaliação. Usado para polling quando `submit` retornou 202.

**Response 200**: mesmo schema do `POST /submit` 200.

**Response 200** (ainda processando):
```json
{
  "assessmentId": "f47ac10b-...",
  "status": "processando"
}
```

**Erros**:
- `404` — avaliação não encontrada

---

### `GET /api/assessments/history`

Lista o histórico de avaliações de um paciente.

**Query params**:
- `patientId` (obrigatório) — UUID do paciente
- `status` (opcional) — filtrar por status
- `page` (opcional, default: 0) — paginação
- `size` (opcional, default: 10) — itens por página

**Response 200**:
```json
{
  "content": [
    {
      "id": "f47ac10b-...",
      "patientId": "550e8400-...",
      "status": "concluido",
      "urgente": false,
      "gravidadeMaisAlta": "media",
      "resumoSintomas": "Dor de cabeça (intensidade 7), fadiga (intensidade 5)",
      "totalDiagnosticos": 1,
      "createdAt": "2026-04-17T10:30:00Z"
    }
  ],
  "totalElements": 5,
  "totalPages": 1,
  "page": 0,
  "size": 10
}
```

---

### `GET /api/assessments/{id}`

Retorna detalhes completos de uma avaliação (sintomas + relatórios + diagnósticos + sugestões).

**Response 200**:
```json
{
  "id": "f47ac10b-...",
  "patient": { "id": "...", "nome": "João Silva" },
  "status": "concluido",
  "urgente": false,
  "resumoGeral": "...",
  "createdAt": "2026-04-17T10:30:00Z",
  "sintomas": [
    {
      "id": "...",
      "symptomNome": "Dor de cabeça",
      "intensidade": 7,
      "duracaoDias": 3,
      "localizacaoCorpo": "cabeça",
      "observacoes": "Piora à tarde"
    }
  ],
  "relatorios": [
    {
      "id": "...",
      "fileName": "hemograma.pdf",
      "fileType": "application/pdf",
      "ocrStatus": "processado"
    }
  ],
  "diagnosticos": [ /* array de diagnósticos com sugestões aninhadas */ ]
}
```

---

### `DELETE /api/assessments/{id}`

Remove uma avaliação e todos os arquivos vinculados.

**Response 204**: sem body.

**Erros**:
- `404` — avaliação não encontrada

---

## Symptoms Catalog

### `GET /api/symptoms`

Busca sintomas no catálogo (autocomplete).

**Query params**:
- `q` — termo de busca (mínimo 2 caracteres)
- `categoria` (opcional) — filtrar por categoria

**Response 200**:
```json
[
  {
    "id": "uuid",
    "nome": "Dor de cabeça",
    "categoria": "Neurológico",
    "sinonimos": ["cefaleia", "enxaqueca"]
  }
]
```

---

## Reports (arquivos)

### `GET /api/reports/{id}/download`

Faz download do arquivo de um relatório.

**Response 200**: stream do arquivo com `Content-Type` e `Content-Disposition` corretos.

**Erros**:
- `404` — relatório não encontrado
