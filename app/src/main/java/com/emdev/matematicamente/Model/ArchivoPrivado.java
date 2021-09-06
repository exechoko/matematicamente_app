package com.emdev.matematicamente.Model;

public class ArchivoPrivado {
    String id;
    String idArchivo;
    String nombre;
    String fecha;
    String url;

    public ArchivoPrivado() {
    }

    public ArchivoPrivado(String id, String idArchivo, String nombre, String fecha, String url) {
        this.id = id;
        this.idArchivo = idArchivo;
        this.nombre = nombre;
        this.fecha = fecha;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdArchivo() {
        return idArchivo;
    }

    public void setIdArchivo(String idArchivo) {
        this.idArchivo = idArchivo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
