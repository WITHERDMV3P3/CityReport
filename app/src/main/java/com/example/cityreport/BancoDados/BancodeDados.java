package com.example.cityreport.BancoDados;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class BancodeDados extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "CityReportBD.db";
    final static int DATABASE_VERSION = 1;

    public static String USUARIOS = "usuarios";
    public static String PROBLEMAS = "problemas";
    public static final String CATEGORIAS = "categorias";

    public BancodeDados(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        criarTabelaUsuarios(db);
        criarTabelaCategorias(db);
        criarTabelaProblemas(db);
    }

    private void criarTabelaUsuarios(SQLiteDatabase db) {
        String CriarTabelaUsuarios = "CREATE TABLE IF NOT EXISTS  " + USUARIOS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nome TEXT NOT NULL, " +
                "email TEXT NOT NULL, " +
                "senha TEXT NOT NULL)";
        db.execSQL(CriarTabelaUsuarios);
    }

    private void criarTabelaCategorias(SQLiteDatabase db) {
        String criarTabela = "CREATE TABLE IF NOT EXISTS " + CATEGORIAS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nome TEXT NOT NULL UNIQUE, " +
                "descricao TEXT)";
        db.execSQL(criarTabela);

        inserirCategoria(db, "Iluminação Pública", "Problemas relacionados à iluminação das ruas.");
        inserirCategoria(db, "Buracos e Pavimentação", "Problemas de buracos e condições das vias.");
        inserirCategoria(db, "Limpeza Urbana", "Questões de lixo e limpeza nas áreas públicas.");
        inserirCategoria(db, "Áreas Verdes", "Problemas em parques e jardins públicos.");
        inserirCategoria(db, "Sinalização", "Falta ou danos em placas de trânsito e sinalização.");
    }

    private void inserirCategoria(SQLiteDatabase db, String nome, String descricao) {
        String query = "INSERT INTO categorias (nome, descricao) VALUES (?, ?)";
        db.execSQL(query, new Object[]{nome, descricao});
    }


    private void criarTabelaProblemas(SQLiteDatabase db) {
        String CriarTabelaProblemas = "CREATE TABLE IF NOT EXISTS  " + PROBLEMAS + "(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "categoria_id INTEGER NOT NULL, " +
                "usuario_id INTEGER NOT NULL, " +
                "descricao TEXT NOT NULL, " +
                "foto BLOB, " +
                "latitude REAL, " +
                "longitude REAL, " +
                "data_hora TEXT, " +
                "status TEXT, " +
                "FOREIGN KEY (categoria_id) REFERENCES " + CATEGORIAS + "(id), " +
                "FOREIGN KEY (usuario_id) REFERENCES " + USUARIOS + "(id))";
        db.execSQL(CriarTabelaProblemas);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


}

