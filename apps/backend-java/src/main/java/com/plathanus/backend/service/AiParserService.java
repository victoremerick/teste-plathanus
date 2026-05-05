package com.plathanus.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class AiParserService {

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

    // Max length to cap user input before regex matching to prevent ReDoS.
    private static final int MAX_TEXTO_LENGTH = 2000;

    // Matches patterns like "10 caixas de leite integral" in Portuguese order texts.
    // Uses possessive quantifiers throughout to prevent backtracking.
    private static final Pattern ITEM_PATTERN = Pattern.compile(
            "(\\d++)\\s++(caixas?+|fardos?+|unidades?+|pacotes?+|litros?+|kg|sacos?+)" +
            "\\s++de\\s++(\\w++(?:\\s++\\w++){0,20})",
            Pattern.CASE_INSENSITIVE
    );
    private static final String SYSTEM_PROMPT = """
            Você é um assistente que extrai informações estruturadas de textos de pedidos comerciais.
            Retorne APENAS um JSON válido (sem markdown, sem explicações) com o seguinte formato:
            {
              "cliente": "nome do cliente ou null se não informado",
              "dataEntrega": "data no formato YYYY-MM-DD ou null se não informada",
              "itens": [
                {"produto": "nome do produto", "quantidade": numero_inteiro, "unidade": "unidade de medida"}
              ]
            }
            Se a data de entrega for "amanhã", calcule com base na data de hoje.
            Se não houver cliente mencionado, use null.
            """;

    @Value("${openai.api.key:}")
    private String openaiApiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public ParsedPedido parse(String texto) {
        if (openaiApiKey != null && !openaiApiKey.isBlank()) {
            try {
                return callOpenAi(texto);
            } catch (Exception e) {
                log.warn("Falha ao chamar OpenAI API, usando fallback. Erro: {}", e.getMessage());
            }
        } else {
            log.info("OPENAI_API_KEY não configurada, usando parser de fallback.");
        }
        return fallbackParse(texto);
    }

    private ParsedPedido callOpenAi(String texto) throws Exception {
        String userMessage = "Extraia as informações do seguinte pedido: " + texto;
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        Map<String, Object> requestMap = Map.of(
                "model", "gpt-3.5-turbo",
                "response_format", Map.of("type", "json_object"),
                "messages", List.of(
                        Map.of("role", "system", "content", SYSTEM_PROMPT + "\nHoje é: " + today),
                        Map.of("role", "user", "content", userMessage)
                )
        );
        String requestBody = objectMapper.writeValueAsString(requestMap);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OPENAI_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + openaiApiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("OpenAI retornou status " + response.statusCode() + ": " + response.body());
        }

        // === IA em uso: log da resposta bruta para debug ===
        log.debug("Resposta bruta da OpenAI: {}", response.body());

        JsonNode root = objectMapper.readTree(response.body());
        // === IA em uso: extração do conteúdo gerado pelo modelo ===
        String content = root.path("choices").get(0).path("message").path("content").asText();
        log.debug("Conteúdo JSON retornado pela IA: {}", content);
        return mapJsonToParsedPedido(objectMapper.readTree(content));
    }

    private ParsedPedido fallbackParse(String texto) {
        // Cap input length to prevent ReDoS on pathological inputs
        String safeTexto = texto.length() > MAX_TEXTO_LENGTH ? texto.substring(0, MAX_TEXTO_LENGTH) : texto;
        String textoLower = safeTexto.toLowerCase();
        List<ParsedItem> itens = new ArrayList<>();

        Matcher matcher = ITEM_PATTERN.matcher(safeTexto);
        while (matcher.find()) {
            int quantidade = Integer.parseInt(matcher.group(1));
            String unidade = matcher.group(2).toLowerCase();
            String produto = matcher.group(3).trim();
            itens.add(new ParsedItem(produto, quantidade, unidade));
        }

        LocalDate dataEntrega = null;
        if (textoLower.contains("amanhã") || textoLower.contains("amanha")) {
            dataEntrega = LocalDate.now().plusDays(1);
        } else if (textoLower.contains("hoje")) {
            dataEntrega = LocalDate.now();
        }

        return new ParsedPedido("desconhecido", dataEntrega, itens);
    }

    private ParsedPedido mapJsonToParsedPedido(JsonNode node) {
        // Se não houver cliente identificado, usa "desconhecido"
        JsonNode clienteNode = node.path("cliente");
        String cliente = (clienteNode.isNull() || clienteNode.isMissingNode())
                ? "desconhecido"
                : clienteNode.asText("desconhecido");
        if (cliente.isBlank() || "null".equalsIgnoreCase(cliente)) {
            cliente = "desconhecido";
        }

        LocalDate dataEntrega = null;
        String dataStr = node.path("dataEntrega").asText(null);
        if (dataStr != null && !dataStr.equals("null")) {
            try {
                dataEntrega = LocalDate.parse(dataStr, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (Exception e) {
                log.warn("Não foi possível parsear a data: {}", dataStr);
            }
        }

        List<ParsedItem> itens = new ArrayList<>();
        JsonNode itensNode = node.path("itens");
        if (itensNode.isArray()) {
            for (JsonNode itemNode : itensNode) {
                String produto = itemNode.path("produto").asText();

                // Se não identificar quantidade → 1
                JsonNode qtdNode = itemNode.path("quantidade");
                Integer quantidade = (qtdNode.isNull() || qtdNode.isMissingNode())
                        ? 1
                        : qtdNode.asInt();

                // Se não identificar unidade → null
                JsonNode unidadeNode = itemNode.path("unidade");
                String rawUnidade = (unidadeNode.isNull() || unidadeNode.isMissingNode())
                        ? null
                        : unidadeNode.asText(null);
                String unidade = (rawUnidade == null || rawUnidade.isBlank() || "null".equalsIgnoreCase(rawUnidade))
                        ? null
                        : rawUnidade;

                itens.add(new ParsedItem(produto, quantidade, unidade));
            }
        }

        return new ParsedPedido(cliente, dataEntrega, itens);
    }

    public record ParsedItem(String produto, Integer quantidade, String unidade) {}

    public record ParsedPedido(String cliente, LocalDate dataEntrega, List<ParsedItem> itens) {}
}
