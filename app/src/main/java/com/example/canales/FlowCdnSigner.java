package com.example.canales;

import android.net.Uri;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FlowCdnSigner {

    private static final String BEARER_URL = "https://gist.githubusercontent.com/test22222222bdfgwcAdx/7b5382504ea798429353bec55f9f69f5/raw/piratacodigo.json";
    private static final String CDN_GEN_BASE = "https://cdn-token.app.flow.com.ar/cdntoken/v2/generator?path=";
    private static final long TOKEN_TTL_MS = 23L * 60 * 60 * 1000;

    private static String cachedBearer = null;
    private static long cachedAt = 0L;

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .followRedirects(true)
            .build();

    /**
     * Devuelve true si el canal necesita firma CDN antes de reproducir.
     */
    public static boolean needsSigning(Canal canal) {
        if (canal.url == null || !canal.url.toLowerCase().endsWith(".mpd")) return false;
        if (canal.url.contains("cvattv.com.ar")) return true;
        if (canal.referer == null) return false;
        return canal.referer.contains("flow.com.ar") || canal.referer.contains("flow.com.py");
    }

    /**
     * Devuelve el origin correcto según el referer del canal.
     */
    public static String siteOriginFor(Canal canal) {
        if (canal.referer != null && canal.referer.contains("flow.com.py")) {
            return "https://portal.app.flow.com.py";
        }
        return "https://portal.app.flow.com.ar";
    }

    /**
     * Firma la URL del canal. Llámalo en un hilo secundario (AsyncTask, Executor, etc.).
     * Devuelve la URL firmada, o null si falla (usar URL original como fallback).
     */
    public static String signUrl(Canal canal) {
        String result = signOnce(canal, false);
        if (result == null) {
            result = signOnce(canal, true);
        }
        return result;
    }

    private static String signOnce(Canal canal, boolean forceFreshBearer) {
        try {
            String bearer = forceFreshBearer ? refreshBearer() : getBearer();
            if (bearer == null) return null;

            String origin = siteOriginFor(canal);
            String encodedFullUrl = Uri.encode(canal.url, null);
            String cdnGenUrl = CDN_GEN_BASE + encodedFullUrl;

            Request req = new Request.Builder()
                    .url(cdnGenUrl)
                    .header("Authorization", bearer)
                    .header("Accept", "application/json")
                    .header("Origin", origin)
                    .header("Referer", origin + "/")
                    .header("x-flow-origin", origin)
                    .header("User-Agent",
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                                    "AppleWebKit/537.36 (KHTML, like Gecko) " +
                                    "Chrome/138.0.0.0 Safari/537.36")
                    .build();

            try (Response response = client.newCall(req).execute()) {
                if (!response.isSuccessful()) {
                    if (!forceFreshBearer) cachedBearer = null;
                    return null;
                }
                String body = response.body() != null ? response.body().string() : null;
                if (body == null) return null;

                String token = new JSONObject(body.trim()).optString("token", null);
                if (token == null || token.isEmpty()) return null;

                return canal.url + "?cdntoken=" + token;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private static synchronized String getBearer() {
        long now = System.currentTimeMillis();
        if (cachedBearer != null && (now - cachedAt) < TOKEN_TTL_MS) {
            return cachedBearer;
        }
        String bearer = fetchBearer();
        if (bearer != null) {
            cachedBearer = bearer;
            cachedAt = System.currentTimeMillis();
        }
        return bearer;
    }

    private static synchronized String refreshBearer() {
        cachedBearer = null;
        cachedAt = 0L;
        return getBearer();
    }

    private static String fetchBearer() {
        try {
            Request req = new Request.Builder().url(BEARER_URL).build();
            try (Response response = client.newCall(req).execute()) {
                if (!response.isSuccessful() || response.body() == null) return null;
                String body = response.body().string();
                String token = new JSONObject(body).optString("bearerToken", null);
                return (token != null && !token.isEmpty()) ? token : null;
            }
        } catch (Exception e) {
            return null;
        }
    }
}