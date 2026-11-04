package com.example.canales;

import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.PlaybackException;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.ui.PlayerView;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.view.Gravity;

import java.util.HashMap;
import java.util.Map;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.content.Intent;
import android.widget.ProgressBar;
import android.widget.LinearLayout;
import android.view.View;

public class PlayerActivity extends AppCompatActivity {

    ExoPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        getWindow().getDecorView().setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                        | android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        FrameLayout root = new FrameLayout(this);
        LinearLayout loadingLayout = new LinearLayout(this);
        loadingLayout.setOrientation(LinearLayout.VERTICAL);
        loadingLayout.setGravity(Gravity.CENTER);
        loadingLayout.setBackgroundColor(0x88000000);

        ProgressBar progressBar = new ProgressBar(this);
        progressBar.getIndeterminateDrawable().setTint(0xFFE50914);
        loadingLayout.addView(progressBar);

        root.addView(loadingLayout, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        loadingLayout.bringToFront();
        PlayerView playerView = new PlayerView(this);
        root.addView(playerView);

        TextView btnBack = new TextView(this);
        btnBack.setText("←");
        btnBack.setTextSize(28);
        btnBack.setTextColor(0xFFFFFFFF);
        btnBack.setPadding(30, 30, 30, 30);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.TOP | Gravity.START;

        root.addView(btnBack, params);
        setContentView(root);
        loadingLayout.bringToFront();

        btnBack.setOnClickListener(v -> finish());

        playerView.setUseController(true);
        playerView.setControllerAutoShow(true);
        playerView.setControllerHideOnTouch(true);
        playerView.setControllerShowTimeoutMs(2500);

        String url     = getIntent().getStringExtra("url");
        String tipo    = getIntent().getStringExtra("tipo");
        String key     = getIntent().getStringExtra("key");
        String licencia  = getIntent().getStringExtra("licencia");
        String referer   = getIntent().getStringExtra("referer");
        String origin    = getIntent().getStringExtra("origin");
        String userAgent = getIntent().getStringExtra("userAgent");

        if (url == null || url.trim().isEmpty()) {
            Toast.makeText(this, "URL vacía", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (url.contains(".php") || url.contains("canales.php")) {
            extraerM3u8ConWebView(url, referer, origin, userAgent);
            return;
        }

        // ── Variables finales para usar dentro del hilo ──
        final String urlFinal     = url.trim();
        final String tipoFinal    = tipo;
        final String keyFinal     = key;
        final String licenciaFinal  = licencia;
        final String refererFinal   = referer;
        final String originFinal    = origin;
        final String userAgentFinal = userAgent;

        // ── Construir Canal temporal para FlowCdnSigner ──
        Canal canal = new Canal(
                "",
                urlFinal,
                "",
                tipoFinal != null ? tipoFinal : "",
                keyFinal != null ? keyFinal : "",
                licenciaFinal != null ? licenciaFinal : "",
                refererFinal != null ? refererFinal : "",
                originFinal != null ? originFinal : "",
                userAgentFinal != null ? userAgentFinal : "",
                ""
        );

        // ── Firma CDN en hilo secundario si hace falta ──
        new Thread(() -> {
            String urlParaReproducir = urlFinal;

            if (FlowCdnSigner.needsSigning(canal)) {
                String signed = FlowCdnSigner.signUrl(canal);
                if (signed != null) {
                    urlParaReproducir = signed;
                }
            }

            final String urlDefinitiva = urlParaReproducir;

            runOnUiThread(() -> iniciarReproduccion(
                    urlDefinitiva,
                    tipoFinal,
                    keyFinal,
                    licenciaFinal,
                    refererFinal,
                    originFinal,
                    userAgentFinal,
                    playerView,
                    loadingLayout
            ));
        }).start();
    }

    private void iniciarReproduccion(
            String url,
            String tipo,
            String key,
            String licencia,
            String referer,
            String origin,
            String userAgent,
            PlayerView playerView,
            LinearLayout loadingLayout
    ) {
        Map<String, String> headers = new HashMap<>();

        if (referer != null && !referer.isEmpty())     headers.put("Referer", referer);
        if (origin != null && !origin.isEmpty())       headers.put("Origin", origin);
        if (userAgent != null && !userAgent.isEmpty()) headers.put("User-Agent", userAgent);
        else                                           headers.put("User-Agent", "Mozilla/5.0");

        DefaultHttpDataSource.Factory dataSourceFactory =
                new DefaultHttpDataSource.Factory()
                        .setUserAgent(userAgent != null && !userAgent.isEmpty() ? userAgent : "Mozilla/5.0")
                        .setDefaultRequestProperties(headers)
                        .setAllowCrossProtocolRedirects(true);

        player = new ExoPlayer.Builder(this)
                .setMediaSourceFactory(new DefaultMediaSourceFactory(dataSourceFactory))
                .build();

        playerView.setPlayer(player);

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_READY) {
                    loadingLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                Toast.makeText(PlayerActivity.this, "Canal no disponible", Toast.LENGTH_LONG).show();
            }
        });

        try {
            MediaItem.Builder mediaBuilder = new MediaItem.Builder()
                    .setUri(Uri.parse(url));

            if (tipo != null && tipo.equalsIgnoreCase("CLEARKEY")) {
                if (licencia != null && !licencia.isEmpty()) {
                    mediaBuilder.setDrmConfiguration(
                            new MediaItem.DrmConfiguration.Builder(C.CLEARKEY_UUID)
                                    .setLicenseUri(licencia.trim())
                                    .build()
                    );
                } else if (key != null && !key.isEmpty()) {
                    String[] parts = key.split(":");
                    if (parts.length == 2) {
                        String json =
                                "{\"keys\":[{\"kty\":\"oct\",\"k\":\"" + parts[1] +
                                        "\",\"kid\":\"" + parts[0] +
                                        "\"}],\"type\":\"temporary\"}";
                        String lic = "data:application/json;base64," +
                                Base64.encodeToString(json.getBytes(), Base64.NO_WRAP);
                        mediaBuilder.setDrmConfiguration(
                                new MediaItem.DrmConfiguration.Builder(C.CLEARKEY_UUID)
                                        .setLicenseUri(lic)
                                        .build()
                        );
                    }
                }
            }

            player.setMediaItem(mediaBuilder.build());
            player.prepare();
            player.play();

            playerView.showController();
            playerView.postDelayed(playerView::hideController, 3500);

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (player != null) player.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
    }

    private void extraerM3u8ConWebView(String paginaUrl, String referer, String origin, String userAgent) {
        WebView webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String encontrada = request.getUrl().toString();
                if (encontrada.contains(".m3u8")) {
                    runOnUiThread(() -> {
                        webView.destroy();
                        Intent intent = new Intent(PlayerActivity.this, PlayerActivity.class);
                        intent.putExtra("url", encontrada);
                        intent.putExtra("tipo", "M3U8");
                        intent.putExtra("referer", referer);
                        intent.putExtra("origin", origin);
                        intent.putExtra("userAgent", userAgent);
                        startActivity(intent);
                        finish();
                    });
                }
                return super.shouldInterceptRequest(view, request);
            }
        });

        webView.loadUrl(paginaUrl);
    }
}