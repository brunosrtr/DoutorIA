# Quickstart: DoutorIA — Setup Local

## Pré-requisitos

| Ferramenta | Versão mínima | Verificar |
|---|---|---|
| Docker + Docker Compose | Docker 24+ | `docker --version` |
| Java JDK | 21+ | `java -version` |
| Maven | 3.9+ | `mvn -version` |
| Node.js | 20+ | `node -v` |
| Git | qualquer | `git --version` |

**API Key do Google Gemini**: obtenha gratuitamente em [aistudio.google.com/apikey](https://aistudio.google.com/apikey).

---

## Estrutura do Projeto

```
DoutorIA/
├── backend/                  # Spring Boot (Java 21)
│   ├── src/
│   │   └── main/
│   │       ├── java/com/doutor/
│   │       │   ├── config/
│   │       │   ├── controller/
│   │       │   ├── domain/
│   │       │   │   ├── entity/
│   │       │   │   └── enums/
│   │       │   ├── dto/
│   │       │   │   ├── request/
│   │       │   │   └── response/
│   │       │   ├── integration/
│   │       │   │   └── gemini/
│   │       │   ├── mapper/
│   │       │   ├── repository/
│   │       │   └── service/
│   │       └── resources/
│   │           ├── application.yml
│   │           └── db/migration/  # Flyway migrations
│   ├── pom.xml
│   └── Dockerfile
├── frontend/                 # Next.js 14 (App Router)
│   ├── src/
│   │   ├── app/
│   │   │   ├── (dashboard)/
│   │   │   │   ├── page.tsx          # Dashboard
│   │   │   │   ├── new-assessment/
│   │   │   │   │   └── page.tsx      # Wizard multi-step
│   │   │   │   ├── assessments/
│   │   │   │   │   ├── page.tsx      # Histórico
│   │   │   │   │   └── [id]/
│   │   │   │   │       └── page.tsx  # Resultado
│   │   │   │   └── layout.tsx
│   │   │   └── layout.tsx
│   │   ├── components/
│   │   │   ├── ui/               # shadcn/ui
│   │   │   ├── assessment/
│   │   │   │   ├── SymptomSearch.tsx
│   │   │   │   ├── IntensitySlider.tsx
│   │   │   │   ├── BodyLocationSelect.tsx
│   │   │   │   ├── FileUploader.tsx
│   │   │   │   └── AssessmentWizard.tsx
│   │   │   └── results/
│   │   │       ├── DiagnosisCard.tsx
│   │   │       ├── SuggestionList.tsx
│   │   │       ├── MedicalDisclaimer.tsx
│   │   │       └── UrgencyBanner.tsx
│   │   ├── lib/
│   │   │   ├── api.ts            # TanStack Query hooks
│   │   │   └── utils.ts
│   │   └── types/
│   │       └── api.ts            # Tipos TypeScript dos contratos
│   ├── package.json
│   └── next.config.ts
├── docker-compose.yml
├── .env.example
└── specs/                    # Especificações (este diretório)
```

---

## 1. Configurar variáveis de ambiente

```bash
cp .env.example .env
```

Edite `.env`:

```env
# Gemini API
GEMINI_API_KEY=sua_api_key_aqui

# PostgreSQL (usado pelo Docker Compose)
POSTGRES_DB=doutor_ia
POSTGRES_USER=doutor
POSTGRES_PASSWORD=doutor123

# Backend
DB_URL=jdbc:postgresql://localhost:5432/doutor_ia
DB_USER=doutor
DB_PASSWORD=doutor123
UPLOAD_DIR=/data/uploads

# Frontend
NEXT_PUBLIC_API_URL=http://localhost:8080
```

---

## 2. Subir o banco de dados

```bash
docker compose up -d postgres
```

O banco sobe em `localhost:5432`. O Flyway (configurado no Spring Boot) rodará as migrations automaticamente na primeira inicialização do backend.

---

## 3. Rodar o backend

```bash
cd backend
mvn spring-boot:run
```

O Spring Boot inicia em `http://localhost:8080`. Na primeira execução as migrations Flyway criam o schema e o seed do catálogo de sintomas.

Verificar se está rodando:
```bash
curl http://localhost:8080/api/symptoms?q=dor
```

---

## 4. Rodar o frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend disponível em `http://localhost:3000`.

---

## 5. Subir tudo com Docker Compose (alternativa)

```bash
docker compose up --build
```

Isso sobe:
- PostgreSQL na porta 5432
- Backend Spring Boot na porta 8080 (após build do JAR)
- Volume `uploads_data` para arquivos médicos

O frontend fica fora do Compose e roda via `npm run dev` na porta 3000.

---

## docker-compose.yml

```yaml
version: '3.9'

services:
  postgres:
    image: postgres:16-alpine
    container_name: doutor_ia_db
    environment:
      POSTGRES_DB: ${POSTGRES_DB:-doutor_ia}
      POSTGRES_USER: ${POSTGRES_USER:-doutor}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD:-doutor123}
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${POSTGRES_USER:-doutor}"]
      interval: 10s
      timeout: 5s
      retries: 5

  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: doutor_ia_backend
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/${POSTGRES_DB:-doutor_ia}
      SPRING_DATASOURCE_USERNAME: ${POSTGRES_USER:-doutor}
      SPRING_DATASOURCE_PASSWORD: ${POSTGRES_PASSWORD:-doutor123}
      GEMINI_API_KEY: ${GEMINI_API_KEY}
      UPLOAD_DIR: /data/uploads
    ports:
      - "8080:8080"
    volumes:
      - uploads_data:/data/uploads

volumes:
  postgres_data:
  uploads_data:
```

---

## Principais dependências

### Backend (`pom.xml`)

```xml
<!-- Spring Boot -->
<parent>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-parent</artifactId>
  <version>3.3.0</version>
</parent>

<dependencies>
  <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
  <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-data-jpa</artifactId></dependency>
  <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-validation</artifactId></dependency>
  <dependency><groupId>org.flywaydb</groupId><artifactId>flyway-core</artifactId></dependency>
  <dependency><groupId>org.postgresql</groupId><artifactId>postgresql</artifactId></dependency>
  <dependency><groupId>org.mapstruct</groupId><artifactId>mapstruct</artifactId><version>1.5.5.Final</version></dependency>
  <dependency><groupId>org.mapstruct</groupId><artifactId>mapstruct-processor</artifactId><version>1.5.5.Final</version></dependency>
  <dependency><groupId>org.projectlombok</groupId><artifactId>lombok</artifactId></dependency>

  <!-- Google Gemini -->
  <dependency>
    <groupId>com.google.ai.client.generativeai</groupId>
    <artifactId>generativeai</artifactId>
    <version>0.9.0</version>
  </dependency>

  <!-- JSON parsing (resposta do Gemini) -->
  <dependency><groupId>com.fasterxml.jackson.core</groupId><artifactId>jackson-databind</artifactId></dependency>
</dependencies>
```

### Frontend (`package.json` — dependências principais)

```json
{
  "dependencies": {
    "next": "^14.2.0",
    "react": "^18.3.0",
    "react-dom": "^18.3.0",
    "@tanstack/react-query": "^5.40.0",
    "react-hook-form": "^7.52.0",
    "zod": "^3.23.0",
    "@hookform/resolvers": "^3.6.0",
    "axios": "^1.7.0",
    "next-themes": "^0.3.0",
    "react-dropzone": "^14.2.0",
    "lucide-react": "^0.394.0",
    "tailwindcss": "^3.4.0",
    "class-variance-authority": "^0.7.0",
    "clsx": "^2.1.0"
  }
}
```

---

## Troubleshooting

| Problema | Solução |
|---|---|
| `Connection refused` no backend | Verifique se o container `postgres` está rodando: `docker ps` |
| Migrations falham | Verifique a connection string em `application.yml` e se o banco foi criado |
| Erro `GEMINI_API_KEY not set` | Certifique-se de que o `.env` está configurado e o Spring Boot está lendo as variáveis |
| Upload falha com 413 | Ajuste `spring.servlet.multipart.max-file-size=10MB` em `application.yml` |
| Frontend não conecta ao backend | Confirme `NEXT_PUBLIC_API_URL=http://localhost:8080` no `.env` do frontend |
