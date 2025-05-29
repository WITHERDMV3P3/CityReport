package com.example.cityreport.BancoDados;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.view.View;

public class BancodeDados extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "CityReport.db";
    final static int DATABASE_VERSION = 1;

    public static String USUARIOS = "usuarios";
    public static String PROBLEMAS = "problemas";
    public static String CATEGORIAS = "categorias";

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
        String CriarTabelaUsuarios = "CREATE TABLE " + USUARIOS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nome TEXT NOT NULL, " +
                "email TEXT NOT NULL, " +
                "senha TEXT NOT NULL)";
        db.execSQL(CriarTabelaUsuarios);
    }

    private void criarTabelaCategorias(SQLiteDatabase db) {
        String CriarTabelaCategorias = "CREATE TABLE " + CATEGORIAS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nome TEXT NOT NULL, " +
                "descricao TEXT)";
        db.execSQL(CriarTabelaCategorias);
        inserirCategoriasPadrao(db);
    }

    private void inserirCategoriasPadrao(SQLiteDatabase db) {
        insertCategoria(db, "Iluminação Pública", "Problemas relacionados à iluminação das ruas.");
        insertCategoria(db, "Buracos e Pavimentação", "Problemas de buracos e condições das vias.");
        insertCategoria(db, "Limpeza Urbana", "Questões de lixo e limpeza nas áreas públicas.");
        insertCategoria(db, "Áreas Verdes", "Problemas em parques e jardins públicos.");
        insertCategoria(db, "Sinalização", "Falta ou danos em placas de trânsito e sinalização.");

    }

    private void insertCategoria(SQLiteDatabase db, String nome, String descricao) {
        String insertSql = "INSERT INTO " + CATEGORIAS + " (nome, descricao) VALUES (?, ?)";
        db.execSQL(insertSql, new Object[]{nome, descricao});
    }

    private void criarTabelaProblemas(SQLiteDatabase db) {
        String CriarTabelaProblemas = "CREATE TABLE " + PROBLEMAS + " (" +
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

    public void inserirCategoria() {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put("nome", "Infraestrutura");
            values.put("descricao", "Problemas com infraestrutura");
            db.insert("categorias", null, values);
        }
    }



}
