package com.example.cityreport.PaginaInicial.problema;

import java.io.Serializable;

public class Problema implements Serializable {
    private int id;
    private String categoria;
    private String descricao;
    private String dataHora;
    private String status;
    private boolean temFoto;
    private double latitude;
    private double longitude;

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

    public Problema(int id, String categoria, String descricao, String dataHora, String status) {
        this(id, categoria, descricao, dataHora, status, 0.0, 0.0, false);
    }

    public int getId() { return id; }
    public String getCategoria() { return categoria; }
    public String getDescricao() { return descricao; }
    public String getDataHora() { return dataHora; }
    public String getStatus() { return status; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    public void setId(int id) { this.id = id; }
    public void setStatus(String status) { this.status = status; }
}