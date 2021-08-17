package com.emdev.matematicamente.Model;

public class ArchivosEscolares {
    String id;
    String nombre;
    String escuela;
    String curso;
    String compartido;
    String fecha;
    String url;

    public ArchivosEscolares() {
    }

    public ArchivosEscolares(String id, String nombre, String escuela, String curso, String compartido, String fecha, String url) {
        this.id = id;
        this.nombre = nombre;
        this.escuela = escuela;
        this.curso = curso;
        this.compartido = compartido;
        this.fecha = fecha;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEscuela() {
        return escuela;
    }

    public void setEscuela(String escuela) {
        this.escuela = escuela;
    }

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    public String getCompartido() {
        return compartido;
    }

    public void setCompartido(String compartido) {
        this.compartido = compartido;
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
