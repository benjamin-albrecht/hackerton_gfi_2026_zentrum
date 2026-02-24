package com.gfi.zentrum.config;

/**
 * ThreadLocal holder for per-request Anthropic API overrides.
 * Allows the frontend to pass API key and base URL via HTTP headers.
 */
public final class AnthropicOverrideHolder {

    private static final ThreadLocal<String> API_KEY = new ThreadLocal<>();
    private static final ThreadLocal<String> BASE_URL = new ThreadLocal<>();

    private AnthropicOverrideHolder() {
    }

    public static void set(String apiKey, String baseUrl) {
        if (apiKey != null && !apiKey.isBlank()) {
            API_KEY.set(apiKey);
        }
        if (baseUrl != null && !baseUrl.isBlank()) {
            BASE_URL.set(baseUrl);
        }
    }

    public static String getApiKey() {
        return API_KEY.get();
    }

    public static String getBaseUrl() {
        return BASE_URL.get();
    }

    public static void clear() {
        API_KEY.remove();
        BASE_URL.remove();
    }
}
