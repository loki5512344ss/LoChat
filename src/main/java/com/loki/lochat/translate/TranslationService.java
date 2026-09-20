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
    private final String provider;
    private final int timeoutMs;

    public TranslationService(String endpoint, String apiKey, boolean enabled, Logger logger) {
        this(endpoint, apiKey, enabled, "libretranslate", 3000, logger);
    }

    public TranslationService(String endpoint, String apiKey, boolean enabled, String provider, int timeoutMs,
                              Logger logger) {
        this.endpoint = endpoint != null ? endpoint.replaceAll("/+$", "") : "";
        this.apiKey = apiKey;
        this.enabled = enabled;
        this.provider = provider == null ? "libretranslate" : provider.toLowerCase(Locale.ROOT);
        this.timeoutMs = Math.max(500, timeoutMs);
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

    private boolean isGoogle() {
        return "google".equals(provider);
    }

    public boolean isEnabled() {
        return enabled && (isGoogle() || !endpoint.isBlank());
    }

    /** Cached translation for the given language, or null if it has not been translated yet. */
    public String getCachedTranslation(String text, String language) {
        return getCached(text, localeToLanguageCode(language));
    }

    /**
     * Translates the text into every given language in parallel and waits up to timeoutMs.
     * Call it off the main thread; the results end up in the cache.
     */
    public void prefetch(String text, java.util.Collection<String> languages, long timeoutMs) {
        if (!isEnabled() || text == null || text.isBlank()) {
            return;
        }
        java.util.List<CompletableFuture<String>> futures = new java.util.ArrayList<>();
        for (String language : languages) {
            futures.add(translateAsync(text, language));
        }
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            // timeout or failure: the message is simply sent without a translation
        }
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

    private volatile long gtxBlockedUntil;
    private volatile long chromeBlockedUntil;

    /**
     * The gtx endpoint gets IP-blocked (HTTP 429 / captcha page) very quickly, so the Chrome dictionary
     * endpoint is tried first and gtx is only a fallback; a blocked endpoint is skipped for a minute.
     */
    private String doFetchGoogle(String text, String targetLang) throws Exception {
        long now = System.currentTimeMillis();
        String q = java.net.URLEncoder.encode(text, StandardCharsets.UTF_8);
        String tl = java.net.URLEncoder.encode(targetLang, StandardCharsets.UTF_8);
        Exception last = null;
        if (now >= chromeBlockedUntil) {
            try {
                return parseChromeResponse(get("https://clients5.google.com/translate_a/t?client=dict-chrome-ex&sl=auto&tl="
                        + tl + "&q=" + q));
            } catch (Exception e) {
                chromeBlockedUntil = now + 60_000;
                last = e;
            }
        }
        if (now >= gtxBlockedUntil) {
            try {
                return parseGoogleResponse(get("https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&dt=t&tl="
                        + tl + "&q=" + q));
            } catch (Exception e) {
                gtxBlockedUntil = now + 60_000;
                last = e;
            }
        }
        throw last != null ? last : new RuntimeException("all translation endpoints are on cooldown");
    }

    private String get(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(timeoutMs))
                .header("User-Agent", "Mozilla/5.0")
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Google returned " + response.statusCode());
        }
        return response.body();
    }

    /** Chrome dictionary response: [["translated","detected-language"], ...]. */
    static String parseChromeResponse(String body) {
        com.google.gson.JsonArray root = JsonParser.parseString(body).getAsJsonArray();
        StringBuilder sb = new StringBuilder();
        for (com.google.gson.JsonElement segment : root) {
            if (segment.isJsonArray()) {
                com.google.gson.JsonArray parts = segment.getAsJsonArray();
                if (parts.size() > 0 && !parts.get(0).isJsonNull()) {
                    sb.append(parts.get(0).getAsString());
                }
            } else if (segment.isJsonPrimitive()) {
                sb.append(segment.getAsString());
                break;
            }
        }
        if (sb.length() == 0) {
            throw new RuntimeException("empty translation response");
        }
        return sb.toString();
    }

    /** Google response: [[["translated","original",...], ...], null, "detected-language"]. */
    static String parseGoogleResponse(String body) {
        com.google.gson.JsonArray root = JsonParser.parseString(body).getAsJsonArray();
        StringBuilder sb = new StringBuilder();
        for (com.google.gson.JsonElement segment : root.get(0).getAsJsonArray()) {
            com.google.gson.JsonArray parts = segment.getAsJsonArray();
            if (parts.size() > 0 && !parts.get(0).isJsonNull()) {
                sb.append(parts.get(0).getAsString());
            }
        }
        return sb.toString();
    }

    private String doFetch(String text, String targetLang) throws Exception {
        if (isGoogle()) {
            return doFetchGoogle(text, targetLang);
        }
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
        String normalized = locale.toLowerCase(Locale.ROOT).replace('-', '_');
        if (normalized.startsWith("zh_tw") || normalized.startsWith("zh_hk")) {
            return "zh-TW";
        }
        String lang = normalized.split("_")[0];
        return "zh".equals(lang) ? "zh-CN" : lang;
    }

    public void clearCache() {
        cache.clear();
    }

    private static String abbreviate(String text) {
        return text.length() > 40 ? text.substring(0, 37) + "..." : text;
    }
}
