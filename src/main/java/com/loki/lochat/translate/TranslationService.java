package com.loki.lochat.translate;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;

public class TranslationService {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final int CACHE_MAX_SIZE = 2000;

    private final Logger logger;
    private final HttpClient httpClient;
    private final Gson gson;
    private final Map<String, Map<String, String>> cache;
    private final ExecutorService executor;
    private final String endpoint;
    private final String apiKey;
    private final boolean enabled;

    public TranslationService(String endpoint, String apiKey, boolean enabled, Logger logger) {
        this.endpoint = endpoint != null ? endpoint.replaceAll("/+$", "") : "";
        this.apiKey = apiKey;
        this.enabled = enabled;
        this.logger = logger;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .build();
        this.gson = new Gson();
        this.cache = Collections.synchronizedMap(new LinkedHashMap<String, Map<String, String>>() {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Map<String, String>> eldest) {
                return size() > CACHE_MAX_SIZE;
            }
        });
        this.executor = Executors.newFixedThreadPool(2, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "lochat-translation");
                t.setDaemon(true);
                return t;
            }
        });
    }

    public boolean isEnabled() {
        return enabled && !endpoint.isBlank();
    }

    public CompletableFuture<String> translateAsync(String text, String targetLanguage) {
        if (!isEnabled() || text == null || text.isBlank()) {
            return CompletableFuture.completedFuture(text);
        }

        String langCode = localeToLanguageCode(targetLanguage);

        String cached = getCached(text, langCode);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }

        return CompletableFuture.supplyAsync(() -> fetchTranslation(text, langCode), executor);
    }

    private String fetchTranslation(String text, String targetLang) {
        try {
            String translation = doFetch(text, targetLang);
            putCached(text, targetLang, translation);
            return translation;
        } catch (Exception e) {
            logger.warning("Translation failed for \"" + abbreviate(text) + "\" to " + targetLang + ": " + e.getMessage());
            return text;
        }
    }

    private String doFetch(String text, String targetLang) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("q", text);
        body.addProperty("source", "auto");
        body.addProperty("target", targetLang);
        body.addProperty("format", "text");
        if (apiKey != null && !apiKey.isBlank()) {
            body.addProperty("api_key", apiKey);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint + "/translate"))
                .timeout(TIMEOUT)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("API returned " + response.statusCode() + ": " + response.body());
        }

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        return json.get("translatedText").getAsString();
    }

    private String getCached(String text, String langCode) {
        synchronized (cache) {
            Map<String, String> langMap = cache.get(text);
            if (langMap != null) {
                return langMap.get(langCode);
            }
        }
        return null;
    }

    private void putCached(String text, String langCode, String translation) {
        synchronized (cache) {
            Map<String, String> langMap = cache.get(text);
            if (langMap == null) {
                langMap = new LinkedHashMap<>();
                cache.put(text, langMap);
            }
            langMap.put(langCode, translation);
        }
    }

    public static String localeToLanguageCode(String locale) {
        if (locale == null || locale.isBlank()) {
            return "en";
        }
        String lang = locale.split("_")[0].toLowerCase(Locale.ROOT);
        return switch (lang) {
            case "zh" -> "zh-CN";
            case "zh-tw", "zh-hk" -> "zh-TW";
            default -> lang;
        };
    }

    public void clearCache() {
        cache.clear();
    }

    private static String abbreviate(String text) {
        return text.length() > 40 ? text.substring(0, 37) + "..." : text;
    }
}
