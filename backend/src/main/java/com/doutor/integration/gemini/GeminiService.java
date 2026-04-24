package com.doutor.integration.gemini;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeminiService {

    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

    private static final String SYSTEM_INSTRUCTION = """
            Você é um assistente médico analítico de triagem iterativo, com tom acolhedor, calmo e tranquilizador. Seu objetivo é ajudar o paciente a entender o que pode estar acontecendo, sem assustá-lo, antes de emitir uma análise preliminar.

            FLUXO DE DECISÃO:
            1. Analise os dados do paciente, sintomas e exames anexados.
            2. Decida: tenho informação suficiente para uma análise confiável?
               - NÃO → retorne "precisa_mais_info": true e uma lista de "perguntas" objetivas (3 a 6) que reduzam ambiguidade. Diagnósticos e sugestões devem vir como listas vazias neste caso.
               - SIM → retorne "precisa_mais_info": false com diagnósticos, sugestões, resumo e urgência.
            3. Se já houver respostas do paciente no histórico (seção "RESPOSTAS A PERGUNTAS ANTERIORES"), considere-as antes de decidir se ainda faltam informações. Após 2 rodadas de perguntas, prefira concluir com análise mesmo que parcial.

            COMO FORMULAR PERGUNTAS:
            - Sejam específicas e responsáveis por 1 fato clínico cada (ex: "A dor irradia para o braço esquerdo?").
            - Agrupe por categoria: "caracterizacao_sintoma", "historico", "gatilho", "exame_fisico", "medicamentos".
            - Inclua um breve "contexto" explicando por que aquela informação é relevante.
            - Evite perguntas já respondidas ou óbvias pelos dados fornecidos.

            TOM E LINGUAGEM (IMPORTANTE):
            - Fale como um profissional calmo e amigo, não como um alerta médico assustador.
            - Priorize hipóteses benignas e comuns antes das raras/graves. Só mencione possibilidades graves se houver sinais reais no quadro.
            - Na "justificativa" e no "resumo_geral", evite termos alarmistas ("grave", "perigoso", "risco iminente", "emergência") a menos que haja sinais claros de urgência.
            - Use frases suaves de probabilidade: "pode estar relacionado a", "é comum em quadros assim", "vale a pena conversar com um médico para confirmar".
            - Quando o quadro for leve/comum, diga isso explicitamente e tranquilize o paciente.

            REGRAS DE SEGURANÇA:
            1. Responda SEMPRE em JSON válido conforme o schema.
            2. Nunca prescreva medicamentos controlados ou indique dosagens.
            3. "gravidade" segue este critério conservador e calmo:
               - "baixa": sintomas comuns, autolimitados, sem sinais de alarme (padrão na maioria dos casos).
               - "media": sintomas persistentes ou que merecem avaliação não urgente.
               - "alta": sinais claros que pedem avaliação médica em breve.
               - "critica": apenas com sinais objetivos de risco imediato à vida.
               Quando em dúvida, prefira a gravidade mais baixa.
            4. "resumo_geral" deve ter tom acolhedor, começar tranquilizando quando apropriado, e terminar com: "Esta análise é informativa e não substitui uma consulta médica."
            5. "urgente": true apenas para risco real e imediato à vida (dor torácica com irradiação, AVC suspeito, dificuldade respiratória grave, sangramento ativo não controlado). Na dúvida, "urgente": false.
            6. Responda em português brasileiro.
            """;

    private static final String JSON_SCHEMA = """
            {
              "type": "object",
              "properties": {
                "precisa_mais_info": {"type": "boolean"},
                "perguntas": {
                  "type": "array",
                  "items": {
                    "type": "object",
                    "properties": {
                      "pergunta": {"type": "string"},
                      "categoria": {"type": "string"},
                      "contexto": {"type": "string"}
                    },
                    "required": ["pergunta", "categoria"]
                  }
                },
                "diagnosticos": {
                  "type": "array",
                  "items": {
                    "type": "object",
                    "properties": {
                      "nome": {"type": "string"},
                      "cid": {"type": "string"},
                      "confianca": {"type": "number", "minimum": 0, "maximum": 1},
                      "gravidade": {"type": "string", "enum": ["baixa", "media", "alta", "critica"]},
                      "justificativa": {"type": "string"}
                    },
                    "required": ["nome", "confianca", "gravidade", "justificativa"]
                  }
                },
                "sugestoes": {
                  "type": "array",
                  "items": {
                    "type": "object",
                    "properties": {
                      "tipo": {"type": "string", "enum": ["especialista", "exame", "habito", "urgencia"]},
                      "descricao": {"type": "string"},
                      "prioridade": {"type": "integer", "minimum": 1, "maximum": 5}
                    },
                    "required": ["tipo", "descricao", "prioridade"]
                  }
                },
                "resumo_geral": {"type": "string"},
                "urgente": {"type": "boolean"}
              },
              "required": ["precisa_mais_info", "perguntas", "diagnosticos", "sugestoes", "resumo_geral", "urgente"]
            }
            """;

    @Value("${gemini.api.key:}")
    private String apiKey;

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    private static final String HEALTH_SUMMARY_INSTRUCTION = """
            Você é um assistente de saúde que analisa o histórico completo de avaliações de um paciente e produz um plano objetivo de melhoria da saúde.

            OBJETIVO:
            Olhar para todas as avaliações passadas (sintomas, diagnósticos, sugestões, urgências) e destilar entre 3 e 6 PONTOS A MELHORAR — itens acionáveis, específicos e orientados ao comportamento, sem repetir literalmente as sugestões pontuais.

            COMO FORMULAR PONTOS:
            - Cada "titulo" deve ser curto (máx. 60 caracteres) e imperativo (ex: "Reduzir sedentarismo", "Monitorar pressão arterial").
            - "descricao" explica em 1–2 frases o porquê e o como, baseado no histórico.
            - "categoria" uma de: "habito", "acompanhamento_medico", "alimentacao", "atividade_fisica", "saude_mental", "medicacao", "sono", "prevencao".
            - "prioridade" uma de: "alta", "media", "baixa" — "alta" para riscos recorrentes ou urgências passadas.
            - Priorize padrões recorrentes ou sinais de risco elevados no histórico.

            "resumo" deve ser 1 parágrafo (máx. 500 caracteres) com visão geral amigável da saúde do paciente e tom encorajador, sem jargão excessivo. Termine com: "Esta análise é informativa e não substitui consulta médica."

            REGRAS:
            1. Responda SEMPRE em JSON válido conforme o schema.
            2. Nunca prescreva medicamentos nem indique dosagens.
            3. Linguagem em português brasileiro, acolhedora e clara.
            4. Se o histórico é escasso, seja honesto e sugira pontos gerais preventivos.
            """;

    private static final String HEALTH_SUMMARY_SCHEMA = """
            {
              "type": "object",
              "properties": {
                "resumo": {"type": "string"},
                "pontos_melhoria": {
                  "type": "array",
                  "items": {
                    "type": "object",
                    "properties": {
                      "titulo": {"type": "string"},
                      "descricao": {"type": "string"},
                      "categoria": {"type": "string"},
                      "prioridade": {"type": "string", "enum": ["alta", "media", "baixa"]}
                    },
                    "required": ["titulo", "descricao", "categoria", "prioridade"]
                  }
                }
              },
              "required": ["resumo", "pontos_melhoria"]
            }
            """;

    public GeminiHealthSummary generateHealthSummary(HealthSummaryContext context) {
        String prompt = buildHealthSummaryPrompt(context);
        String requestBody = buildSimpleRequestBody(
                HEALTH_SUMMARY_INSTRUCTION + "\n\nJSON Schema esperado:\n" + HEALTH_SUMMARY_SCHEMA,
                prompt
        );
        try {
            return callHealthSummary(requestBody);
        } catch (Exception e) {
            log.warn("Primeira tentativa de resumo de saúde falhou, tentando novamente: {}", e.getMessage());
            try {
                Thread.sleep(1500);
                return callHealthSummary(requestBody);
            } catch (Exception retryEx) {
                throw new RuntimeException("Falha ao gerar resumo de saúde: " + retryEx.getMessage(), retryEx);
            }
        }
    }

    private GeminiHealthSummary callHealthSummary(String requestBody) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GEMINI_API_URL + "?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(Duration.ofSeconds(60))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Gemini API erro " + response.statusCode() + ": " + response.body());
        }

        var root = objectMapper.readTree(response.body());
        String text = root.path("candidates").get(0)
                .path("content").path("parts").get(0)
                .path("text").asText();

        var node = objectMapper.readTree(text);
        List<GeminiHealthSummary.Ponto> pontos = new java.util.ArrayList<>();
        for (var p : node.path("pontos_melhoria")) {
            pontos.add(new GeminiHealthSummary.Ponto(
                    p.path("titulo").asText(),
                    p.path("descricao").asText(),
                    p.path("categoria").asText("habito"),
                    p.path("prioridade").asText("media")
            ));
        }
        return new GeminiHealthSummary(node.path("resumo").asText(), pontos);
    }

    private String buildHealthSummaryPrompt(HealthSummaryContext ctx) {
        var p = ctx.patient();
        StringBuilder sb = new StringBuilder();
        sb.append("PACIENTE:\n");
        sb.append("- Nome: ").append(p.nome()).append("\n");
        if (p.dataNascimento() != null) {
            int age = java.time.LocalDate.now().getYear() - p.dataNascimento().getYear();
            sb.append("- Idade: ").append(age).append(" anos\n");
        }
        sb.append("- Sexo: ").append(p.sexo()).append("\n");
        if (p.tipoSanguineo() != null) sb.append("- Tipo sanguíneo: ").append(p.tipoSanguineo()).append("\n");
        if (p.alergiasConhecidas() != null && !p.alergiasConhecidas().isEmpty()) {
            sb.append("- Alergias: ").append(String.join(", ", p.alergiasConhecidas())).append("\n");
        }

        sb.append("\nHISTÓRICO DE AVALIAÇÕES (").append(ctx.assessments().size()).append("):\n");
        int i = 1;
        for (var a : ctx.assessments()) {
            sb.append("\n[Avaliação ").append(i++).append("] ").append(a.data())
                    .append(a.urgente() ? " — URGENTE" : "").append("\n");
            if (a.sintomas() != null && !a.sintomas().isEmpty()) {
                sb.append("  Sintomas: ").append(String.join("; ", a.sintomas())).append("\n");
            }
            if (a.diagnosticos() != null && !a.diagnosticos().isEmpty()) {
                sb.append("  Diagnósticos:\n");
                for (var d : a.diagnosticos()) {
                    sb.append("    - ").append(d.nome())
                            .append(" (gravidade ").append(d.gravidade());
                    if (d.confianca() != null) {
                        sb.append(", confiança ").append(Math.round(d.confianca() * 100)).append("%");
                    }
                    sb.append(")");
                    if (d.justificativa() != null && !d.justificativa().isBlank()) {
                        sb.append(": ").append(d.justificativa());
                    }
                    sb.append("\n");
                }
            }
            if (a.sugestoes() != null && !a.sugestoes().isEmpty()) {
                sb.append("  Sugestões dadas: ").append(String.join("; ", a.sugestoes())).append("\n");
            }
            if (a.resumoGeral() != null && !a.resumoGeral().isBlank()) {
                sb.append("  Resumo: ").append(a.resumoGeral()).append("\n");
            }
        }

        sb.append("\nCom base nesse histórico, produza o JSON com resumo e pontos_melhoria conforme o schema.");
        return sb.toString();
    }

    private String buildSimpleRequestBody(String systemText, String userText) {
        try {
            var body = java.util.Map.of(
                    "contents", List.of(
                            java.util.Map.of("role", "user", "parts", List.of(
                                    java.util.Map.of("text", systemText),
                                    java.util.Map.of("text", userText)
                            ))
                    ),
                    "generationConfig", java.util.Map.of(
                            "temperature", 0.3,
                            "responseMimeType", "application/json"
                    )
            );
            return objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao montar request do Gemini", e);
        }
    }

    public GeminiAnalysisResult analyze(AssessmentContext context) {
        String prompt = buildPrompt(context);
        String requestBody = buildRequestBody(prompt, context.reports());

        try {
            return callGemini(requestBody);
        } catch (Exception e) {
            log.warn("Primeira tentativa ao Gemini falhou, tentando novamente: {}", e.getMessage());
            try {
                Thread.sleep(2000);
                return callGemini(requestBody);
            } catch (Exception retryEx) {
                throw new RuntimeException("Falha ao comunicar com Gemini após retry: " + retryEx.getMessage(), retryEx);
            }
        }
    }

    private GeminiAnalysisResult callGemini(String requestBody) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GEMINI_API_URL + "?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(Duration.ofSeconds(60))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Gemini API erro " + response.statusCode() + ": " + response.body());
        }

        return parseResponse(response.body());
    }

    private String buildPrompt(AssessmentContext ctx) {
        var p = ctx.patient();
        StringBuilder sb = new StringBuilder();
        sb.append("DADOS DO PACIENTE:\n");
        sb.append("- Nome: ").append(p.nome()).append("\n");
        if (p.dataNascimento() != null) {
            int age = java.time.LocalDate.now().getYear() - p.dataNascimento().getYear();
            sb.append("- Idade: ").append(age).append(" anos\n");
        }
        sb.append("- Sexo: ").append(p.sexo()).append("\n");
        if (p.tipoSanguineo() != null) sb.append("- Tipo sanguíneo: ").append(p.tipoSanguineo()).append("\n");
        if (p.alergiasConhecidas() != null && !p.alergiasConhecidas().isEmpty()) {
            sb.append("- Alergias conhecidas: ").append(String.join(", ", p.alergiasConhecidas())).append("\n");
        }
        sb.append("\nSINTOMAS RELATADOS:\n");
        for (var s : ctx.symptoms()) {
            sb.append("- ").append(s.nome()).append(": intensidade ").append(s.intensidade()).append("/10");
            if (s.duracaoDias() != null) sb.append(", duração ").append(s.duracaoDias()).append(" dias");
            if (s.localizacaoCorpo() != null) sb.append(", localização: ").append(s.localizacaoCorpo());
            sb.append("\n");
            if (s.observacoes() != null && !s.observacoes().isBlank()) {
                sb.append("  Observações: ").append(s.observacoes()).append("\n");
            }
        }
        if (!ctx.reports().isEmpty()) {
            sb.append("\nDOCUMENTOS MÉDICOS ANEXADOS: ").append(ctx.reports().size()).append(" arquivo(s)\n");
            sb.append("Analise os documentos em conjunto com os sintomas acima, apontando achados relevantes.\n");
        }
        if (ctx.followupQA() != null && !ctx.followupQA().isEmpty()) {
            sb.append("\nRESPOSTAS A PERGUNTAS ANTERIORES:\n");
            for (var qa : ctx.followupQA()) {
                sb.append("- P: ").append(qa.pergunta()).append("\n");
                sb.append("  R: ").append(qa.resposta() == null || qa.resposta().isBlank() ? "(não respondida)" : qa.resposta()).append("\n");
            }
        }
        sb.append("\nCom base nessas informações, decida se precisa de mais contexto (retorne perguntas) ou se já pode concluir (retorne diagnósticos e sugestões). Siga o JSON schema.");
        return sb.toString();
    }

    private String buildRequestBody(String prompt, List<ReportFileDTO> reports) {
        try {
            var parts = new java.util.ArrayList<Object>();

            parts.add(java.util.Map.of("text", SYSTEM_INSTRUCTION + "\n\nJSON Schema esperado:\n" + JSON_SCHEMA));
            parts.add(java.util.Map.of("text", prompt));

            for (var report : reports) {
                String base64 = Base64.getEncoder().encodeToString(report.content());
                parts.add(java.util.Map.of(
                        "inline_data", java.util.Map.of(
                                "mime_type", report.mimeType(),
                                "data", base64
                        )
                ));
            }

            var body = java.util.Map.of(
                    "contents", List.of(
                            java.util.Map.of("role", "user", "parts", parts)
                    ),
                    "generationConfig", java.util.Map.of(
                            "temperature", 0.2,
                            "responseMimeType", "application/json"
                    )
            );

            return objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao montar request do Gemini", e);
        }
    }

    private GeminiAnalysisResult parseResponse(String responseBody) {
        try {
            var root = objectMapper.readTree(responseBody);
            String text = root
                    .path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

            var node = objectMapper.readTree(text);

            boolean precisaMaisInfo = node.path("precisa_mais_info").asBoolean(false);

            List<GeminiAnalysisResult.GeminiPergunta> perguntas = new java.util.ArrayList<>();
            for (var q : node.path("perguntas")) {
                perguntas.add(new GeminiAnalysisResult.GeminiPergunta(
                        q.path("pergunta").asText(),
                        q.path("categoria").asText("outro"),
                        q.has("contexto") ? q.path("contexto").asText(null) : null
                ));
            }

            List<GeminiAnalysisResult.GeminiDiagnostico> diagnosticos = new java.util.ArrayList<>();
            for (var d : node.path("diagnosticos")) {
                diagnosticos.add(new GeminiAnalysisResult.GeminiDiagnostico(
                        d.path("nome").asText(),
                        d.has("cid") ? d.path("cid").asText(null) : null,
                        d.path("confianca").asDouble(),
                        d.path("gravidade").asText(),
                        d.path("justificativa").asText()
                ));
            }

            List<GeminiAnalysisResult.GeminiSugestao> sugestoes = new java.util.ArrayList<>();
            for (var s : node.path("sugestoes")) {
                sugestoes.add(new GeminiAnalysisResult.GeminiSugestao(
                        s.path("tipo").asText(),
                        s.path("descricao").asText(),
                        s.path("prioridade").asInt(3)
                ));
            }

            return new GeminiAnalysisResult(
                    precisaMaisInfo,
                    perguntas,
                    diagnosticos,
                    sugestoes,
                    node.path("resumo_geral").asText(),
                    node.path("urgente").asBoolean(false)
            );
        } catch (Exception e) {
            throw new RuntimeException("Falha ao parsear resposta do Gemini: " + e.getMessage(), e);
        }
    }
}
