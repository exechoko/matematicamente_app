package com.emdev.matematicamente.Model;

public class Usuario {
    String id;
    String nombre;
    String clave;
    String imageURL;
    String correo;
    String admin;

    public Usuario() {
    }

    public Usuario(String id, String nombre, String clave, String correo) {
        this.id = id;
        this.nombre = nombre;
        this.clave = clave;
        this.correo = correo;
        this.imageURL = "default";
        this.admin = "false";
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

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }
}
