# Especificação: Análise de Sintomas e Diagnóstico Assistido por IA

**Versão**: 1.0
**Data**: 2026-04-17
**Status**: Rascunho
**Diretório**: specs/001-health-symptom-analysis

---

## Visão Geral

### Problema

Pessoas com sintomas de saúde frequentemente não sabem se precisam de atendimento urgente, qual especialista procurar ou quais exames solicitar. O acesso a uma triagem inicial informada é limitado por disponibilidade de consultas, custos e distância.

### Solução

Um sistema web onde o usuário descreve seus sintomas — incluindo o que está sentindo, há quanto tempo e a intensidade —, anexa fotos e documentos de exames médicos, e recebe uma análise assistida por IA com possíveis diagnósticos, nível de gravidade dos problemas identificados, sugestões de encaminhamento e cuidados imediatos. Todas as avaliações ficam armazenadas em um histórico pessoal acessível apenas pelo próprio usuário.

O sistema deixa explícito em toda a experiência que **não substitui consulta médica presencial**.

### Usuários-Alvo

- **Usuário leigo em saúde**: pessoa adulta sem formação médica que deseja entender melhor seus sintomas antes ou entre consultas.
- **Usuário recorrente**: pessoa com condições crônicas que acompanha sua saúde ao longo do tempo e mantém histórico de avaliações.

---

## Cenários de Uso e Testes

### Cenário 1 — Registro de sintomas e análise

**Dado que** o usuário está autenticado
**Quando** ele preenche a descrição dos sintomas (o que sente, há quanto tempo, intensidade de 1–10) e envia
**Então** o sistema processa os dados e exibe a análise com possíveis diagnósticos (lista ordenada por probabilidade), problemas destacados por gravidade e sugestões de ação

**Critérios de aceitação**:
- Todos os três campos de sintoma (descrição, duração, intensidade) são exibidos no formulário
- A análise é retornada em menos de 30 segundos após o envio
- Cada diagnóstico sugerido exibe sua probabilidade (ex.: "Alta", "Moderada", "Baixa")
- Os problemas são visualmente diferenciados por gravidade (ex.: crítico, atenção, leve)
- A sugestão de ação inclui tipo de especialista e/ou exame recomendado
- O aviso de que o sistema não substitui consulta médica aparece de forma destacada na tela de resultados

---

### Cenário 2 — Envio de arquivos de exames

**Dado que** o usuário está na tela de nova avaliação
**Quando** ele adiciona arquivos (imagens ou PDF) de exames ou relatórios médicos
**Então** o sistema aceita os arquivos, processa seu conteúdo junto aos sintomas e incorpora as informações à análise

**Critérios de aceitação**:
- Formatos aceitos: imagens (JPG, PNG) e documentos PDF
- Tamanho máximo por arquivo: 10 MB
- Número máximo de arquivos por avaliação: 5
- Se o arquivo não puder ser processado, o sistema informa o motivo sem cancelar o restante da avaliação
- Os arquivos enviados ficam vinculados à avaliação e acessíveis no histórico

---

### Cenário 3 — Consulta ao histórico de avaliações

**Dado que** o usuário tem avaliações anteriores
**Quando** ele acessa a seção de histórico
**Então** ele vê uma lista cronológica das avaliações com data, resumo dos sintomas e status de gravidade; ao clicar em uma avaliação, visualiza os detalhes completos

**Critérios de aceitação**:
- Histórico exibe avaliações em ordem decrescente de data
- Cada item da lista mostra: data, resumo de sintomas, indicador de gravidade mais alta identificada
- O detalhe da avaliação mostra todos os dados originais (sintomas, arquivos, análise completa)
- Arquivos enviados permanecem acessíveis por no mínimo 12 meses

---

### Cenário 4 — Autenticação

**Dado que** o usuário não está autenticado
**Quando** acessa a aplicação
**Então** é direcionado para a tela de login com opções de e-mail/senha e Google

**Critérios de aceitação**:
- Cadastro via e-mail exige confirmação por link enviado ao endereço informado
- Login com Google segue o fluxo OAuth padrão sem exigir cadastro manual
- Sessão expira após período de inatividade (padrão: 7 dias)
- Nenhum dado de saúde é acessível sem autenticação bem-sucedida

---

### Cenário 5 — Privacidade dos dados

**Dado que** dois usuários distintos estão autenticados
**Quando** cada um acessa o sistema
**Então** cada um visualiza somente suas próprias avaliações e arquivos, sem acesso aos dados do outro

**Critérios de aceitação**:
- Tentativa de acessar avaliação de outro usuário retorna erro de autorização (não expõe conteúdo)
- Arquivos enviados são vinculados ao usuário e não são acessíveis por URL pública sem autenticação

---

### Cenário 6 — Modo claro e escuro

**Dado que** o usuário está na aplicação
**Quando** ele altera a preferência de tema (claro/escuro)
**Então** a interface muda para o tema selecionado e mantém a preferência em sessões futuras

**Critérios de aceitação**:
- Alternância de tema disponível de forma acessível (botão visível ou configuração de perfil)
- Preferência persiste entre sessões do mesmo usuário
- Todos os componentes da interface respeitam o tema ativo (sem elementos com contraste inadequado)

---

## Requisitos Funcionais

### RF-01 — Formulário de sintomas
O sistema deve permitir que o usuário descreva: (a) o que está sentindo em texto livre, (b) há quanto tempo os sintomas estão presentes e (c) a intensidade percebida em escala de 1 a 10. Todos os campos são obrigatórios para envio.

### RF-02 — Upload de arquivos médicos
O sistema deve aceitar upload de até 5 arquivos por avaliação, nos formatos JPG, PNG e PDF, com tamanho máximo de 10 MB por arquivo. Os arquivos são armazenados com segurança e vinculados exclusivamente à avaliação e ao usuário que os enviou.

### RF-03 — Análise assistida por IA
Após o envio, o sistema deve processar os sintomas e arquivos anexados e retornar: lista de possíveis diagnósticos com nível de probabilidade (alto / moderado / baixo), lista de problemas identificados classificados por gravidade (crítico / atenção / leve) e sugestões de ação (tipo de especialista a procurar, exames recomendados, cuidados imediatos).

### RF-04 — Aviso médico obrigatório
Em toda tela de resultados de análise, o sistema deve exibir um aviso claro e não ocultável de que a análise é informativa e não substitui avaliação médica profissional.

### RF-05 — Histórico de avaliações
O sistema deve armazenar todas as avaliações realizadas pelo usuário e disponibilizá-las em uma seção de histórico, com listagem e visualização de detalhes.

### RF-06 — Autenticação com e-mail e Google
O sistema deve suportar cadastro e login via e-mail/senha (com confirmação de e-mail) e via conta Google (OAuth 2.0). Nenhuma funcionalidade de análise ou histórico é acessível sem autenticação.

### RF-07 — Isolamento de dados por usuário
Cada usuário deve ter acesso exclusivo aos seus próprios dados. Tentativas de acesso a dados de outro usuário devem ser bloqueadas e registradas.

### RF-08 — Preferência de tema
O sistema deve oferecer modo claro e modo escuro, com a preferência do usuário persistindo entre sessões.

---

## Critérios de Sucesso

1. **Tempo de resposta da análise**: 90% das análises são entregues em menos de 30 segundos após o envio.
2. **Conclusão do fluxo principal**: 80% dos usuários que iniciam o formulário de sintomas conseguem visualizar os resultados sem abandono.
3. **Satisfação com clareza dos resultados**: em pesquisa pós-uso, pelo menos 70% dos usuários classificam as informações recebidas como "claras" ou "muito claras".
4. **Privacidade**: zero incidentes de vazamento de dados entre usuários em testes de penetração e auditoria.
5. **Acessibilidade do aviso médico**: 100% das telas de resultado exibem o aviso de não substituição de consulta médica de forma visível (acima da dobra em resoluções de desktop e mobile).
6. **Adoção de histórico**: 50% dos usuários retornam para visualizar ao menos uma avaliação anterior no primeiro mês.

---

## Entidades Principais

| Entidade | Atributos principais | Notas |
|---|---|---|
| **Usuário** | ID, e-mail, nome, provedor de autenticação, preferência de tema, data de cadastro | Isolado por autenticação |
| **Avaliação** | ID, ID do usuário, data/hora, descrição dos sintomas, duração, intensidade, resultado da análise, status de gravidade | Imutável após criação |
| **Arquivo Médico** | ID, ID da avaliação, nome original, tipo (imagem/PDF), tamanho, referência de armazenamento | Vinculado à avaliação |
| **Resultado de Análise** | ID, ID da avaliação, lista de diagnósticos com probabilidade, lista de problemas com gravidade, lista de sugestões | Gerado pela IA |

---

## Premissas e Decisões

- **Idioma**: interface e análise em português brasileiro.
- **Plataforma**: aplicação web responsiva; mobile-first não é requisito, mas a interface deve funcionar em smartphones.
- **Retenção de dados**: arquivos e avaliações mantidos por no mínimo 12 meses; exclusão de conta remove todos os dados do usuário.
- **Análise de arquivos**: o sistema extrai texto de PDFs e descrições de imagens para incorporar à análise; a qualidade da análise depende da legibilidade dos documentos.
- **Conta única por e-mail**: se um usuário se cadastrou com e-mail e depois tenta entrar com Google usando o mesmo endereço, as contas são vinculadas automaticamente.
- **Exclusão de avaliação**: usuários podem excluir avaliações individualmente do histórico.
- **Sem prescrição**: o sistema não sugere medicamentos específicos, apenas orienta sobre tipos de cuidado e especialistas.

---

## Fora de Escopo

- Teleconsulta ou chat com médicos reais
- Integração com planos de saúde ou sistemas hospitalares
- Prescrição de medicamentos
- Monitoramento contínuo de sinais vitais (ex.: via wearables)
- Versão nativa para iOS/Android
- Suporte a múltiplos idiomas na versão inicial

---

## Dependências

- Serviço de IA capaz de analisar texto e conteúdo de imagens/PDFs e retornar diagnósticos estruturados
- Serviço de autenticação OAuth 2.0 para login com Google
- Serviço de armazenamento seguro de arquivos com controle de acesso por usuário
- Serviço de envio de e-mail para confirmação de cadastro

---

## Riscos

| Risco | Impacto | Mitigação |
|---|---|---|
| Usuário interpretar análise como diagnóstico definitivo | Alto — pode atrasar busca por cuidado adequado | Avisos proeminentes em toda análise; linguagem de probabilidade, não certeza |
| Baixa qualidade de análise para arquivos ilegíveis | Médio — resultado menos útil | Informar ao usuário sobre limitações antes do upload; não bloquear envio |
| Vazamento de dados de saúde | Alto — risco legal e de privacidade | Isolamento por usuário; arquivos não acessíveis por URL pública |
