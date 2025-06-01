package com.example.cityreport.PaginaInicial.problema;

import java.io.Serializable;

public class Problema implements Serializable {
    private int id;
    private String categoria;
    private String descricao;
    private String dataHora;
    private String status;
    private boolean temFoto;  // Renomeado para ser mais claro
    private double latitude;
    private double longitude;

    // Construtor completo
    public Problema(int id, String categoria, String descricao, String dataHora,
                    String status, double latitude, double longitude, boolean temFoto) {
        this.id = id;
        this.categoria = categoria;
        this.descricao = descricao;
        this.dataHora = dataHora;
        this.status = status;
        this.temFoto = temFoto;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Construtor sem foto e coordenadas
    public Problema(int id, String categoria, String descricao, String dataHora, String status) {
        this(id, categoria, descricao, dataHora, status, 0.0, 0.0, false);
    }

    // Getters
    public int getId() { return id; }
    public String getCategoria() { return categoria; }
    public String getDescricao() { return descricao; }
    public String getDataHora() { return dataHora; }
    public String getStatus() { return status; }
    public boolean temFoto() { return temFoto; }  // Nome mais intuitivo
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public void setDataHora(String dataHora) { this.dataHora = dataHora; }
    public void setStatus(String status) { this.status = status; }
    public void setTemFoto(boolean temFoto) { this.temFoto = temFoto; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}