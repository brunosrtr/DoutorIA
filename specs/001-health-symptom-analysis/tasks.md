# Tasks: DoutorIA — Análise de Sintomas e Diagnóstico Assistido por IA

**Feature**: `specs/001-health-symptom-analysis`
**Gerado em**: 2026-04-17
**Stack**: Next.js 14 · Java 21/Spring Boot 3 · PostgreSQL 16 (Docker) · Google Gemini API
**Total de tarefas**: 65

---

## User Stories

| ID | Descrição | Prioridade |
|---|---|---|
| US1 | Descrever sintomas e receber análise com diagnósticos, gravidade e sugestões | P1 |
| US2 | Enviar arquivos de exames (PDFs/imagens) junto à avaliação | P2 |
| US3 | Consultar histórico de todas as avaliações anteriores | P3 |
| US4 | Dashboard e modo claro/escuro | P4 |

---

## Dependências entre User Stories

```
Setup → Foundational → US1 → US2 → US3 → US4
                   ↑
              (US2 pode ser parcialmente em paralelo com US1 no frontend)
```

- **US1** depende de: Setup + Foundational
- **US2** depende de: US1 (precisa do AssessmentWizard criado na US1)
- **US3** depende de: US1 (precisa do fluxo de avaliação funcionando)
- **US4** depende de: US1 (precisa da estrutura de layout e páginas)

---

## Phase 1: Setup — Inicialização do Projeto

**Objetivo**: repositório configurado com ambas as aplicações prontas para desenvolver.

**Critério de conclusão**: `docker compose up -d postgres` sobe o banco; `mvn spring-boot:run` inicia sem erros; `npm run dev` exibe a página inicial do Next.js.

- [x] T001 Criar projeto Spring Boot via Spring Initializr com dependências: spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-validation, flyway-core, postgresql, lombok — em `backend/`
- [x] T002 Criar projeto Next.js 14 com TypeScript, Tailwind CSS e App Router via `npx create-next-app@latest frontend --typescript --tailwind --app` — em `frontend/`
- [x] T003 Criar `docker-compose.yml` na raiz do projeto com serviços: postgres (postgres:16-alpine, porta 5432, volume postgres_data), backend (porta 8080, volume uploads_data), healthcheck no postgres — conforme spec em `specs/001-health-symptom-analysis/plan/quickstart.md`
- [x] T004 Criar `.env.example` na raiz com variáveis: GEMINI_API_KEY, POSTGRES_DB, POSTGRES_USER, POSTGRES_PASSWORD, DB_URL, DB_USER, DB_PASSWORD, UPLOAD_DIR, NEXT_PUBLIC_API_URL — e criar `.env` local a partir dele
- [x] T005 Criar `backend/Dockerfile` multi-stage (stage 1: `maven:3.9-eclipse-temurin-21` para build do JAR; stage 2: `eclipse-temurin:21-jre-alpine` para runtime) com WORKDIR `/app`, COPY target/*.jar e ENTRYPOINT

---

## Phase 2: Foundational — Backend Base

**Objetivo**: schema do banco criado, entities JPA mapeadas, repositórios prontos, configuração do Spring funcionando.

**Critério de conclusão**: aplicação Spring Boot inicia, Flyway roda todas as 9 migrations sem erro, tabelas existem no banco.

- [x] T006 Criar as 9 migrations Flyway em `backend/src/main/resources/db/migration/`: V1 (enums), V2 (patients), V3 (symptoms_catalog), V4 (symptom_assessments), V5 (assessment_symptoms), V6 (medical_reports), V7 (diagnoses), V8 (suggestions), V9 (seed do catálogo) — DDL completo em `specs/001-health-symptom-analysis/plan/data-model.md`
- [x] T007 [P] Criar entity JPA `Patient` com todos os campos e `@Enumerated` para `SexoEnum` em `backend/src/main/java/com/doutor/domain/entity/Patient.java` + `PatientRepository` em `backend/src/main/java/com/doutor/repository/PatientRepository.java`
- [x] T008 [P] Criar entity JPA `SymptomAssessment` com `@Enumerated(EnumType.STRING)` para `AssessmentStatusEnum`, relacionamento `@ManyToOne Patient` e lista `@OneToMany` para symptoms/reports/diagnoses em `backend/src/main/java/com/doutor/domain/entity/SymptomAssessment.java` + `SymptomAssessmentRepository` em `backend/src/main/java/com/doutor/repository/SymptomAssessmentRepository.java`
- [x] T009 [P] Criar entity JPA `SymptomsCatalog` com campo `sinonimos` mapeado como `@Type(PostgreSQLArrayType.class)` (hypersistence-utils) ou como `@Column(columnDefinition = "text[]")` em `backend/src/main/java/com/doutor/domain/entity/SymptomsCatalog.java` + `SymptomsCatalogRepository` com método `findByNomeContainingIgnoreCase` em `backend/src/main/java/com/doutor/repository/SymptomsCatalogRepository.java`
- [x] T010 [P] Criar entity JPA `AssessmentSymptom` com `@ManyToOne SymptomAssessment` e `@ManyToOne(optional=true) SymptomsCatalog` (nullable para sintomas custom), campo `nomeCustom` adicional, constraint `intensidade` 1-10 em `backend/src/main/java/com/doutor/domain/entity/AssessmentSymptom.java` + repository em `backend/src/main/java/com/doutor/repository/AssessmentSymptomRepository.java`
- [x] T011 [P] Criar entity JPA `MedicalReport` com `@Enumerated` para `OcrStatusEnum`, campo `fileSizeBytes` como `Long` em `backend/src/main/java/com/doutor/domain/entity/MedicalReport.java` + `MedicalReportRepository` em `backend/src/main/java/com/doutor/repository/MedicalReportRepository.java`
- [x] T012 [P] Criar entities JPA `Diagnosis` (com `@Enumerated` para `GravidadeEnum`) e `Suggestion` (com `@Enumerated` para `SugestaoTipoEnum`, `@ManyToOne Diagnosis`) em `backend/src/main/java/com/doutor/domain/entity/Diagnosis.java` e `Suggestion.java` + repositories em `backend/src/main/java/com/doutor/repository/`
- [x] T013 Criar todos os enums Java espelhando os PostgreSQL enums: `SexoEnum`, `AssessmentStatusEnum`, `OcrStatusEnum`, `GravidadeEnum`, `SugestaoTipoEnum` em `backend/src/main/java/com/doutor/domain/enums/`
- [x] T014 Configurar `backend/src/main/resources/application.yml` com: datasource (lendo do .env), JPA (ddl-auto=validate, show-sql=false), Flyway (enabled=true, locations=classpath:db/migration), multipart (max-file-size=10MB, max-request-size=50MB), server.port=8080, gemini.api.key=${GEMINI_API_KEY}, upload.dir=${UPLOAD_DIR:/data/uploads}
- [x] T015 Criar `CorsConfig` em `backend/src/main/java/com/doutor/config/CorsConfig.java` permitindo origem `http://localhost:3000` para todos os métodos e headers
- [x] T016 Criar `GlobalExceptionHandler` em `backend/src/main/java/com/doutor/config/GlobalExceptionHandler.java` com `@RestControllerAdvice` tratando: `EntityNotFoundException` (404), `IllegalStateException` (409), `MethodArgumentNotValidException` (400), `MaxUploadSizeExceededException` (413) — retornando `ErrorResponse { timestamp, status, message, path }`
- [x] T017 Criar interface `FileStorageService` em `backend/src/main/java/com/doutor/service/FileStorageService.java` com métodos `store(UUID assessmentId, MultipartFile file): String`, `load(String filePath): byte[]`, `delete(String filePath): void` — e implementação `LocalFileStorageService` em `backend/src/main/java/com/doutor/service/impl/LocalFileStorageService.java` que persiste em `${upload.dir}/{assessmentId}/{uuid}-{originalFileName}`

---

## Phase 3: US1 — Criar Avaliação com Sintomas e Receber Análise

**Story**: Como usuário, quero descrever meus sintomas (o que sinto, há quanto tempo, intensidade 1-10) e receber uma análise com diagnósticos sugeridos, nível de gravidade e sugestões do que fazer.

**Critério de conclusão independente**: `POST /api/assessments/{id}/submit` com sintomas retorna JSON com `status: concluido`, diagnósticos com confiança e gravidade, sugestões e resumo geral. Frontend exibe resultado completo com aviso médico visível.

### Backend — US1

- [x] T018 [P] [US1] Criar DTOs de request: `CreatePatientRequest`, `AddSymptomsRequest` (lista de `SymptomItemRequest` com campos symptomId, nomeCustom, intensidade, duracaoDias, localizacaoCorpo, observacoes) em `backend/src/main/java/com/doutor/dto/request/`
- [x] T019 [P] [US1] Criar DTOs de response: `PatientResponse`, `AssessmentResponse`, `AssessmentResultResponse` (com listas de `DiagnosisResponse` contendo lista `SuggestionResponse` aninhada) em `backend/src/main/java/com/doutor/dto/response/`
- [x] T020 [P] [US1] Criar MapStruct mappers: `PatientMapper`, `AssessmentMapper`, `DiagnosisMapper` em `backend/src/main/java/com/doutor/mapper/` com `@Mapper(componentModel = "spring")`
- [x] T021 [US1] Implementar `PatientService` interface + `PatientServiceImpl` em `backend/src/main/java/com/doutor/service/` com métodos `create`, `findById`, `findAll` — lançando `EntityNotFoundException` se paciente não encontrado
- [x] T022 [US1] Implementar `PatientController` em `backend/src/main/java/com/doutor/controller/PatientController.java` com endpoints `POST /api/patients` (201), `GET /api/patients` (200), `GET /api/patients/{id}` (200)
- [x] T023 [US1] Implementar `SymptomsService` interface + `SymptomsServiceImpl` com método `search(String query, String categoria)` usando `ILIKE` no repository e `SymptomsController` em `backend/src/main/java/com/doutor/controller/SymptomsController.java` com endpoint `GET /api/symptoms?q={query}&categoria={categoria}` (mínimo 2 chars, máx 20 resultados)
- [x] T024 [US1] Criar `GeminiConfig` em `backend/src/main/java/com/doutor/config/GeminiConfig.java` como `@Configuration` que instancia `GenerativeModel("gemini-1.5-pro", apiKey)` como bean Spring, lendo `gemini.api.key` do application.yml
- [x] T025 [P] [US1] Criar records `AssessmentContext`, `PatientDTO`, `SymptomDTO`, `ReportFileDTO` (com `byte[] content` e `String mimeType`) e `GeminiAnalysisResult` (com listas de `GeminiDiagnostico` e `GeminiSugestao`, `resumoGeral`, `urgente`) em `backend/src/main/java/com/doutor/integration/gemini/`
- [x] T026 [US1] Implementar `GeminiService` em `backend/src/main/java/com/doutor/integration/gemini/GeminiService.java`: método `analyze(AssessmentContext)` que (1) monta SystemInstruction, (2) monta UserPrompt dinâmico com dados do paciente + sintomas, (3) adiciona `Part.inlineData` para cada relatório, (4) configura `GenerationConfig` com temperature=0.2 e responseMimeType="application/json", (5) chama Gemini com retry 1x em caso de erro, (6) parseia JSON com Jackson retornando `GeminiAnalysisResult`
- [x] T027 [US1] Implementar `AssessmentService` interface + `AssessmentServiceImpl` em `backend/src/main/java/com/doutor/service/` com métodos: `create(UUID patientId)`, `addSymptoms(UUID assessmentId, AddSymptomsRequest)`, `submit(UUID assessmentId)` — `submit` deve: (1) validar status=rascunho, (2) mudar para processando, (3) montar AssessmentContext, (4) chamar GeminiService, (5) persistir Diagnosis + Suggestion, (6) mudar para concluido; em erro muda para `erro`
- [x] T028 [US1] Implementar `AssessmentController` em `backend/src/main/java/com/doutor/controller/AssessmentController.java` com endpoints: `POST /api/assessments` (201), `POST /api/assessments/{id}/symptoms` (200), `POST /api/assessments/{id}/submit` (200 ou 202), `GET /api/assessments/{id}/result` (200)

### Frontend — US1

- [x] T029 [P] [US1] Instalar dependências do frontend: `@tanstack/react-query`, `axios`, `react-hook-form`, `@hookform/resolvers`, `zod`, `next-themes`, `lucide-react` via `npm install` em `frontend/`
- [x] T030 [P] [US1] Inicializar shadcn/ui em `frontend/` com `npx shadcn@latest init` e adicionar componentes: button, card, badge, input, select, slider, skeleton, separator, alert, form, label, dialog, toast
- [x] T031 [P] [US1] Criar `frontend/src/types/api.ts` com todas as interfaces TypeScript espelhando os contratos de `specs/001-health-symptom-analysis/plan/contracts/api.md`: `PatientResponse`, `AssessmentResponse`, `AssessmentResultResponse`, `DiagnosisResponse`, `SuggestionResponse`, `SymptomCatalogItem`, `AssessmentSummaryResponse`
- [x] T032 [US1] Criar `frontend/src/lib/api.ts` com cliente axios (`baseURL = process.env.NEXT_PUBLIC_API_URL`) e hooks TanStack Query: `usePatients`, `useCreatePatient`, `useCreateAssessment`, `useAddSymptoms`, `useSubmitAssessment`, `useAssessmentResult`, `useSymptomSearch`
- [x] T033 [US1] Configurar `frontend/src/app/layout.tsx` com `QueryClientProvider`, `ThemeProvider` (next-themes, attribute="class"), fonte Inter, metadata da aplicação e import do globals.css
- [x] T034 [P] [US1] Criar `frontend/src/components/assessment/SymptomSearch.tsx`: input com debounce 300ms chamando `useSymptomSearch`, lista de sugestões com destaque do termo buscado, callback `onSelect(SymptomCatalogItem | null)`, suporte a sintoma customizado (texto livre se não encontrar no catálogo)
- [x] T035 [P] [US1] Criar `frontend/src/components/assessment/IntensitySlider.tsx`: shadcn Slider de 1 a 10 com marcadores numéricos, label colorido (1-3 verde, 4-6 amarelo, 7-10 vermelho), prop `value` + `onChange`
- [x] T036 [P] [US1] Criar `frontend/src/components/assessment/BodyLocationSelect.tsx`: shadcn Select com regiões pré-definidas: cabeça, pescoço, tórax, abdômen, costas, membros superiores, membros inferiores, geral/sistêmico
- [x] T037 [P] [US1] Criar `frontend/src/components/results/MedicalDisclaimer.tsx`: banner fixo com ícone de informação, texto "Esta análise é informativa e NÃO substitui avaliação médica profissional", estilo alert de aviso (amarelo), não pode ser fechado/ocultado
- [x] T038 [P] [US1] Criar `frontend/src/components/results/DiagnosisCard.tsx`: card shadcn com nome do diagnóstico, CID, badge de gravidade com cores (baixa=verde, media=amarelo, alta=laranja, critica=vermelho), barra de progresso para confiança percentual, justificativa em texto colapsável
- [x] T039 [P] [US1] Criar `frontend/src/components/results/SuggestionList.tsx`: lista de sugestões agrupadas por tipo com ícones Lucide (especialista=Stethoscope, exame=FileText, habito=Heart, urgencia=AlertTriangle), ordenadas por prioridade dentro de cada grupo
- [x] T040 [P] [US1] Criar `frontend/src/components/results/UrgencyBanner.tsx`: banner vermelho proeminente exibido apenas quando `urgente=true`, com texto "ATENÇÃO: Esta avaliação indica possível urgência médica. Busque atendimento imediatamente." e ícone de alerta
- [x] T041 [US1] Criar `frontend/src/app/(dashboard)/new-assessment/page.tsx` como wizard multi-step com `react-hook-form` + Zod: Step 1 (selecionar ou criar paciente), Step 2 (adicionar sintomas com SymptomSearch + IntensitySlider + BodyLocationSelect, múltiplos sintomas), Step 4 (revisão + botão "Analisar" que chama `submit`, loading state durante análise, polling a cada 3s se retornar 202)
- [x] T042 [US1] Criar `frontend/src/app/(dashboard)/assessments/[id]/page.tsx`: exibe MedicalDisclaimer no topo, UrgencyBanner se urgente, resumo geral, lista de DiagnosisCard ordenada por confiança, SuggestionList, dados do paciente e sintomas reportados no collapse

---

## Phase 4: US2 — Upload de Arquivos de Exames

**Story**: Como usuário, quero anexar fotos de exames e PDFs de relatórios médicos à minha avaliação para que o sistema analise o conteúdo junto com meus sintomas.

**Critério de conclusão independente**: É possível fazer upload de um PDF de hemograma e de uma imagem JPG em uma avaliação; ao submeter, o resultado inclui referências ao conteúdo dos documentos na justificativa dos diagnósticos.

### Backend — US2

- [x] T043 [US2] Criar `ReportResponse` DTO em `backend/src/main/java/com/doutor/dto/response/ReportResponse.java` e `ReportMapper` em `backend/src/main/java/com/doutor/mapper/ReportMapper.java`
- [x] T044 [US2] Implementar `ReportService` interface + `ReportServiceImpl` em `backend/src/main/java/com/doutor/service/`: método `addReport(UUID assessmentId, MultipartFile file)` que (1) valida tipo e tamanho, (2) chama `FileStorageService.store()`, (3) persiste `MedicalReport` com status=pendente, (4) lança exceção se limite de 5 arquivos atingido
- [x] T045 [US2] Adicionar endpoint `POST /api/assessments/{id}/reports` em `AssessmentController` aceitando `multipart/form-data` com campo `file`, retornando `ReportResponse` (201)
- [x] T046 [US2] Atualizar `AssessmentServiceImpl.submit()` para carregar bytes de cada `MedicalReport` da avaliação via `FileStorageService.load()`, popular `ReportFileDTO` com conteúdo base64 + mimeType e incluir no `AssessmentContext` antes de chamar o Gemini; atualizar `ocr_status` do report para `processado` ou `erro` após o submit
- [x] T047 [US2] Implementar `GET /api/reports/{id}/download` em novo `ReportController` em `backend/src/main/java/com/doutor/controller/ReportController.java`: carrega bytes via `FileStorageService.load()`, retorna `ResponseEntity<byte[]>` com `Content-Type` e `Content-Disposition: attachment`

### Frontend — US2

- [x] T048 [P] [US2] Instalar `react-dropzone` via `npm install react-dropzone` em `frontend/` e criar `frontend/src/components/assessment/FileUploader.tsx`: zona de drag-and-drop com preview de arquivos (nome, tamanho, ícone PDF/imagem), validação client-side de tipo e tamanho, badge de contagem (x/5), botão de remover por arquivo
- [x] T049 [US2] Adicionar Step 3 ao wizard em `frontend/src/app/(dashboard)/new-assessment/page.tsx`: integrar `FileUploader`, chamar `POST /api/assessments/{id}/reports` para cada arquivo via `useMutation`, exibir progresso de upload e status de cada arquivo, continuar para Step 4 mesmo com erros de upload (exibir aviso)
- [x] T050 [US2] Atualizar `frontend/src/app/(dashboard)/assessments/[id]/page.tsx` para exibir seção "Documentos Analisados" listando os `relatorios` da avaliação com nome, tipo e badge `ocrStatus`

---

## Phase 5: US3 — Histórico de Avaliações

**Story**: Como usuário, quero consultar todas as minhas avaliações anteriores com data, resumo de sintomas e gravidade mais alta identificada, e poder ver os detalhes completos de qualquer avaliação passada.

**Critério de conclusão independente**: A página de histórico lista avaliações em ordem decrescente de data; clicar em uma avaliação abre o resultado completo com diagnósticos e sugestões.

### Backend — US3

- [x] T051 [US3] Criar `AssessmentSummaryResponse` DTO com campos: id, patientId, status, urgente, gravidadeMaisAlta (calculada como MAX da lista de gravidades), resumoSintomas (concatenação dos primeiros 2 nomes de sintomas), totalDiagnosticos, createdAt — em `backend/src/main/java/com/doutor/dto/response/AssessmentSummaryResponse.java`
- [x] T052 [US3] Adicionar método `getHistory(UUID patientId, Pageable pageable)` ao `AssessmentService` e implementação em `AssessmentServiceImpl` com query JPQL que retorna avaliações por `patient_id` ordenadas por `created_at DESC`, populando `gravidadeMaisAlta` via subquery ou computação em Java
- [x] T053 [US3] Adicionar endpoints ao `AssessmentController`: `GET /api/assessments/history?patientId={id}&status={status}&page={n}&size={n}` (200, paginado) e `GET /api/assessments/{id}` (200, detalhe completo com sintomas + relatórios + diagnósticos aninhados)
- [x] T054 [US3] Adicionar endpoint `DELETE /api/assessments/{id}` ao `AssessmentController` que deleta avaliação e arquivos físicos via `FileStorageService.delete()` para cada relatório, retornando 204

### Frontend — US3

- [x] T055 [P] [US3] Criar `frontend/src/components/assessment/AssessmentCard.tsx`: card de item do histórico com data formatada (pt-BR), resumo dos sintomas, badge de status (rascunho=cinza, processando=azul, concluido=verde, erro=vermelho), badge de gravidade mais alta, indicador de urgência, link para detalhe
- [x] T056 [US3] Criar hook `useAssessmentHistory(patientId, filters)` em `frontend/src/lib/api.ts` com suporte a paginação e filtro por status; criar hook `useDeleteAssessment()` com invalidação do cache após deleção
- [x] T057 [US3] Criar `frontend/src/app/(dashboard)/assessments/page.tsx`: seletor de paciente no topo, lista de `AssessmentCard` com filtro por status (tabs), paginação (shadcn Pagination), estado vazio ("Nenhuma avaliação ainda") e botão "Nova Avaliação"

---

## Phase 6: Polish — Dashboard, Tema e Finalização

**Objetivo**: experiência completa e coesa, Docker funcionando, README pronto.

**Critério de conclusão**: `docker compose up --build` sobe tudo sem erros; fluxo completo (criar paciente → nova avaliação → upload → submeter → ver resultado → histórico) funciona do início ao fim no browser; modo escuro sem elementos com contraste inadequado.

- [x] T058 Criar `frontend/src/app/(dashboard)/layout.tsx` com sidebar lateral contendo: logo "DoutorIA" + ícone, links de navegação (Dashboard, Nova Avaliação, Histórico), seletor de paciente ativo persistido em localStorage, ThemeToggle no rodapé da sidebar; responsivo (hamburger menu em mobile)
- [x] T059 Criar `frontend/src/app/(dashboard)/page.tsx` (Dashboard): card de boas-vindas com nome do paciente selecionado, botão proeminente "Nova Avaliação", seção "Últimas Avaliações" com os 3 `AssessmentCard` mais recentes via `useAssessmentHistory` com `size=3`
- [x] T060 Criar `frontend/src/components/ui/ThemeToggle.tsx`: botão com ícones Sun/Moon (Lucide) alternando entre light/dark via `useTheme` do next-themes; preferência persistida automaticamente pelo next-themes em localStorage
- [x] T061 Verificar e corrigir contraste de todos os componentes em modo escuro: DiagnosisCard, SuggestionList, AssessmentCard, MedicalDisclaimer, UrgencyBanner — ajustar classes Tailwind dark: onde necessário em `frontend/src/components/`
- [x] T062 Adicionar `Skeleton` loading states nas páginas: histórico (`frontend/src/app/(dashboard)/assessments/page.tsx`) e resultado (`frontend/src/app/(dashboard)/assessments/[id]/page.tsx`) usando shadcn Skeleton durante fetch
- [x] T063 Adicionar `frontend/src/components/results/MedicalDisclaimer.tsx` à página de resultado como primeiro elemento visível, verificar que está acima da dobra em viewport 375px (mobile) e 1280px (desktop) — ajustar layout se necessário
- [ ] T064 Testar `docker compose up --build` end-to-end: verificar que Flyway roda migrations, seed do catálogo é aplicado, backend responde em :8080, variável `GEMINI_API_KEY` é lida corretamente — documentar qualquer ajuste necessário
- [x] T065 Criar `README.md` na raiz do projeto a partir de `specs/001-health-symptom-analysis/plan/quickstart.md` com seções: Visão Geral, Pré-requisitos, Setup (passo a passo), Como usar, Onde colocar a API Key do Gemini, Troubleshooting

---

## Grafo de Dependências Detalhado

```
T001 T002 T003 T004 T005   ← Setup (paralelos entre si)
        ↓
T006 T007 T008 T009 T010 T011 T012 T013 T014 T015 T016 T017   ← Foundational (T007-T012 paralelos)
        ↓
T018 T019 T020 T025 T029 T030 T031  ← US1 prep (paralelos)
        ↓
T021 T022 T023 T024 T026  ← US1 serviços (paralelos entre si após prep)
        ↓
T027 T028  ← US1 submit + controller (sequencial: T027 antes de T028)
T032 T033 T034 T035 T036 T037 T038 T039 T040  ← US1 frontend (paralelos após T031+T032)
        ↓
T041 T042  ← US1 páginas (sequencial: dependem dos componentes)
        ↓
T043 T044 T048  ← US2 prep (paralelos)
        ↓
T045 T046 T047 T049 T050  ← US2 endpoints + frontend
        ↓
T051 T052 T055 T056  ← US3 prep (paralelos)
        ↓
T053 T054 T057  ← US3 endpoints + página
        ↓
T058 T059 T060 T061 T062 T063  ← Polish (paralelos)
        ↓
T064 T065  ← Docker test + README (sequencial)
```

---

## Exemplos de Execução em Paralelo

### Sessão 1 — Após Setup (T001-T005 completos)

Agente A: T006 (migrations) + T013 (application.yml) + T014 (CORS) + T015 (GlobalExceptionHandler)
Agente B: T007 + T008 + T009 + T010 (entities Patient, Assessment, Catalog, Symptom)
Agente C: T002 concluído? → T029 + T030 + T031 (tipos TypeScript, shadcn init, layout Next.js)

### Sessão 2 — Após Foundational (T006-T017 completos)

Agente A: T018 + T019 + T020 + T021 + T022 (DTOs + serviços de paciente + catálogo)
Agente B: T024 + T025 + T026 (Gemini config + integration)
Agente C: T033 + T034 + T035 + T036 (componentes frontend sem dependência de API)

### Sessão 3 — US1 paralelo com US2 (T041-T042 completos)

Agente A: T043 + T044 + T045 + T046 (backend US2)
Agente B: T048 (FileUploader component, independente do backend)

---

## Estratégia de Implementação

### MVP (US1 completo = T001–T042)

Após T042, o sistema é completamente funcional para o fluxo principal:
1. Criar paciente
2. Iniciar avaliação
3. Adicionar sintomas com catálogo + intensidade + localização
4. Submeter para análise Gemini
5. Ver resultado com diagnósticos, gravidade, sugestões e aviso médico

### Incremento 1 (+ US2 = T043–T050)

Adiciona upload de PDFs e imagens — análise passa a usar conteúdo dos documentos.

### Incremento 2 (+ US3 = T051–T057)

Adiciona histórico completo — usuário pode consultar avaliações anteriores.

### Incremento 3 (+ Polish = T058–T065)

Dashboard, modo escuro, Docker completo, README.

---

## Validação de Formato

- [x] Todos os 65 tasks têm checkbox `- [ ]`
- [x] Todos os tasks têm ID sequencial (T001–T065)
- [x] Tasks paralelas marcadas com `[P]`
- [x] Tasks de user story marcadas com `[US1]`, `[US2]`, `[US3]`
- [x] Todos os tasks de implementação têm caminho de arquivo
- [x] Phases de Setup e Foundational sem label de story
- [x] Phase de Polish sem label de story
