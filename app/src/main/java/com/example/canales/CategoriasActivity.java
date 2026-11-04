package com.example.canales;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import android.widget.ImageView;
public class CategoriasActivity extends AppCompatActivity {


    LinearLayout btnDeportes, btnPeliculas, btnEventos, btnEventosPy, btnParaguay;
    ImageView btnActualizar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categorias);

        ImageView imgDeportes = findViewById(R.id.imgDeportes);
        ImageView imgPeliculas = findViewById(R.id.imgPeliculas);
        ImageView imgEventos = findViewById(R.id.imgEventos);
        ImageView imgEventosPy = findViewById(R.id.imgEventosPy);
        ImageView imgParaguay = findViewById(R.id.imgParaguay);
        btnActualizar = findViewById(R.id.btnActualizar);

        btnActualizar.setOnClickListener(v -> {

            android.widget.Toast.makeText(
                    this,
                    "Actualizando canales...",
                    android.widget.Toast.LENGTH_SHORT
            ).show();

            Intent i = new Intent(this, CategoriasActivity.class);
            startActivity(i);
            finish();


        });

        Glide.with(this).load("https://raw.githubusercontent.com/futbolparaguayotv/imagenes/refs/heads/main/imgapk/deportes.png").into(imgDeportes);
        Glide.with(this).load("https://raw.githubusercontent.com/futbolparaguayotv/imagenes/refs/heads/main/imgapk/peliculas.png").into(imgPeliculas);
        Glide.with(this).load("https://raw.githubusercontent.com/futbolparaguayotv/imagenes/refs/heads/main/imgapk/eventospy.png").into(imgEventos);
        Glide.with(this).load("https://raw.githubusercontent.com/futbolparaguayotv/imagenes/refs/heads/main/imgapk/eventospy.png").into(imgEventosPy);
        Glide.with(this).load("https://raw.githubusercontent.com/futbolparaguayotv/imagenes/refs/heads/main/imgapk/paraguay.png").into(imgParaguay);

        btnDeportes = findViewById(R.id.btnDeportes);
        btnPeliculas = findViewById(R.id.btnPeliculas);
        btnEventos = findViewById(R.id.btnEventos);
        btnEventosPy = findViewById(R.id.btnEventosPy);
        btnParaguay = findViewById(R.id.btnParaguay);

        btnDeportes.setOnClickListener(v -> abrirCategoria("Deportes"));
        btnPeliculas.setOnClickListener(v -> abrirCategoria("Peliculas"));
        btnEventos.setOnClickListener(v -> abrirCategoria("Eventos"));
        btnEventosPy.setOnClickListener(v -> abrirCategoria("EventosPy"));
        btnParaguay.setOnClickListener(v -> abrirCategoria("Paraguay"));
    }

    void abrirCategoria(String categoria) {
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("categoria", categoria);
        startActivity(i);
    }
}