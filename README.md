# DoutorIA

Análise de sintomas assistida por IA com diagnósticos, sugestões e upload de exames.

**Stack**: Java 21 + Spring Boot 3.3 + SQLite | Next.js 16 + TypeScript + Tailwind

---

## Rodando localmente

### Pré-requisitos
- Java 21 (Temurin recomendado)
- Node.js 18+
- Chave de API do Google Gemini

### Backend

```bash
cd backend
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home
export GEMINI_API_KEY=sua_chave_aqui
mvn spring-boot:run
# Banco criado automaticamente em ./doutor_ia.db
```

API disponível em `http://localhost:8080`

### Frontend

```bash
cd frontend
npm install
npm run dev
```

App disponível em `http://localhost:3000`

---

## Variáveis de ambiente

Copie `.env.example` para `.env` e preencha:

```
GEMINI_API_KEY=sua_chave_aqui
UPLOAD_DIR=./uploads          # diretório local de uploads
NEXT_PUBLIC_API_URL=http://localhost:8080
```

---

## Docker

```bash
cp .env.example .env
# Edite .env com sua GEMINI_API_KEY
docker compose up --build
```

Backend: `http://localhost:8080` · Frontend (se adicionado): `http://localhost:3000`

---

## Funcionalidades

- Cadastro de paciente com dados clínicos
- Avaliação de sintomas com intensidade, duração e localização
- Upload de exames (JPG, PNG, PDF até 10 MB, máx. 5 arquivos)
- Análise por IA (Gemini) com diagnósticos, CID, confiança e gravidade
- Sugestões de especialistas, exames e hábitos
- Histórico de avaliações com paginação
- Disclaimer médico obrigatório em todos os resultados
- Tema claro/escuro

---

> **Aviso:** Este sistema é informativo e não substitui avaliação médica profissional.
