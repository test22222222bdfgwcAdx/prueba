package com.example.canales;

public class Canal {

    public String nombre;
    public String url;
    public String imagen;
    public String tipo;
    public String key;
    public String licencia;
    public String referer;
    public String origin;
    public String userAgent;
    public String categoria;

    public Canal(
            String nombre,
            String url,
            String imagen,
            String tipo,
            String key,
            String licencia,
            String referer,
            String origin,
            String userAgent,
            String categoria
    ) {

        this.nombre = nombre;
        this.url = url;
        this.imagen = imagen;
        this.tipo = tipo;
        this.key = key;
        this.licencia = licencia;
        this.referer = referer;
        this.origin = origin;
        this.userAgent = userAgent;
        this.categoria = categoria;
    }
}