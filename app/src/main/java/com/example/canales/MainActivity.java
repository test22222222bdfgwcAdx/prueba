package com.example.canales;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import android.view.View;
import android.widget.TextView;
public class MainActivity extends AppCompatActivity {

    RecyclerView recycler;
    ArrayList<Canal> lista = new ArrayList<>();
    ArrayList<Canal> listaCompleta = new ArrayList<>();
    EditText buscador;
    TextView txtCategoria, btnBuscar;
    String categoriaSeleccionada;

    String URL_TXT = "https://gist.githubusercontent.com/test22222222bdfgwcAdx/de56884bd4be26a984e5daf56f4057da/raw/canales.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recycler = findViewById(R.id.recycler);
        buscador = findViewById(R.id.buscador);
        txtCategoria = findViewById(R.id.txtCategoria);
        btnBuscar = findViewById(R.id.btnBuscar);



        btnBuscar.setOnClickListener(v -> {
            if (buscador.getVisibility() == View.GONE) {
                buscador.setVisibility(View.VISIBLE);
                buscador.setFocusableInTouchMode(true);
                buscador.requestFocus();
            } else {
                buscador.setVisibility(View.GONE);
                buscador.setText("");
                recycler.requestFocus();
            }
        });
        buscador.clearFocus();
        recycler.requestFocus();
        recycler.setLayoutManager(new GridLayoutManager(this, 3));
        categoriaSeleccionada = getIntent().getStringExtra("categoria");

        if (categoriaSeleccionada == null) {
            categoriaSeleccionada = "Todos";
        }
        txtCategoria.setText(categoriaSeleccionada);

        cargarCanales();
        buscador.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarCanales(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    void cargarCanales() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(URL_TXT)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String data = response.body().string();

                try {
                    JSONArray array = new JSONArray(data);
                    lista.clear();

                    for (int i = 0; i < array.length(); i++) {

                        JSONObject obj = array.getJSONObject(i);

                        String nombre = obj.optString("name", "");
                        String categoria = obj.optString("categoria", "Otros");
                        String url = obj.optString("url", "");
                        String imagen = obj.optString("icono", "");
                        String tipo = obj.optString("type", "NONE");
                        String key = obj.optString("key", "");
                        String licencia = obj.optString("drm_license_uri", "");


                        JSONObject headers = obj.optJSONObject("headers");

                        String referer = "";
                        String origin = "";
                        String userAgent = "Mozilla/5.0";

                        if (headers != null) {
                            referer = headers.optString("Referer", "");
                            origin = headers.optString("Origin", "");
                            userAgent = headers.optString("User-Agent", "Mozilla/5.0");
                        }

                        if (categoriaSeleccionada.equalsIgnoreCase("Todos")
                                || categoria.equalsIgnoreCase(categoriaSeleccionada)) {

                            lista.add(new Canal(
                                    nombre,
                                    url,
                                    imagen,
                                    tipo,
                                    key,
                                    licencia,
                                    referer,
                                    origin,
                                    userAgent,
                                    categoria
                            ));
                        }
                    }

                    runOnUiThread(() -> {
                        listaCompleta.clear();
                        listaCompleta.addAll(lista);
                        recycler.setAdapter(new CanalAdapter(MainActivity.this, lista));
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    void filtrarCanales(String texto) {
        ArrayList<Canal> filtrada = new ArrayList<>();

        for (Canal canal : listaCompleta) {
            if (canal.nombre != null &&
                    canal.nombre.toLowerCase().contains(texto.toLowerCase())) {
                filtrada.add(canal);
            }
        }

        recycler.setAdapter(new CanalAdapter(MainActivity.this, filtrada));
    }
    @Override
    public void onBackPressed() {
        finish();
    }
}