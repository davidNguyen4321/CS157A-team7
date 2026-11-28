package util;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class GeminiClient {
	private static final String MODEL = "gemini-3.5-flash-lite";
	private static final String URL = "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL + ":generateContent";
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private GeminiClient() {}

    public static JsonNode generateJson(String prompt, JsonNode responseSchema) throws IOException, InterruptedException {
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) throw new IllegalStateException("GEMINI_API_KEY is not set");

        ObjectNode body = MAPPER.createObjectNode();
        body.putArray("contents").addObject().putArray("parts").addObject().put("text", prompt);
        ObjectNode config = body.putObject("generationConfig");
        config.put("responseMimeType", "application/json");
        config.set("responseSchema", responseSchema);

        HttpRequest request = HttpRequest.newBuilder(URI.create(URL))
            .timeout(Duration.ofSeconds(20))
            .header("Content-Type", "application/json")
            .header("x-goog-api-key", apiKey)
            .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(body)))
            .build();

        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            String detail = response.body() == null ? "" : response.body().replaceAll("\\s+", " ");
            throw new IOException("Gemini returned HTTP " + response.statusCode()
                + (detail.length() > 300 ? " " + detail.substring(0, 300) : " " + detail));
        }
        
        String text = MAPPER.readTree(response.body())
            .path("candidates").path(0).path("content").path("parts").path(0).path("text").asText(null);
        if (text == null) throw new IOException("Gemini returned no content");
        return MAPPER.readTree(text);
    }
}