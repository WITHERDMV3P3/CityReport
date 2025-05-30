package com.example.cityreport.BancoDados;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;
public class BancodadosDAO {
    final private Context context;

    public BancodadosDAO(Context context) {
        this.context = context;
    }

    public void inserirDadosUsuario(EncDados dados) {
        try (SQLiteDatabase db = new BancodeDados(context).getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put("nome", dados.getNome());
            values.put("email", dados.getEmail());
            values.put("senha", dados.getSenha());
            db.insert("usuarios", null, values);
        }
    }

    public boolean verificarUsuario(String email, String senha) {
        SQLiteDatabase db = new BancodeDados(context).getWritableDatabase();
        String query = "SELECT * FROM usuarios WHERE email = ? AND senha = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email, senha});
        boolean loginValido = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return loginValido;
    }

    public List<String> buscarProblemas(int usuarioId) {
        List<String> problemas = new ArrayList<>();
        SQLiteDatabase db = new BancodeDados(context).getReadableDatabase();

        String query = "SELECT c.nome, p.descricao, p.data_hora, p.status " +
                "FROM problemas p " +
                "JOIN categorias c ON p.categoria_id = c.id " +
                "WHERE p.usuario_id = ? " +
                "ORDER BY p.data_hora DESC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(usuarioId)});

        if (cursor.moveToFirst()) {
            do {
                String categoria = cursor.getString(0);
                String descricao = cursor.getString(1);
                String dataHora = cursor.getString(2);
                String status = cursor.getString(3);

                // Formata os dados em uma string única
                String problemaFormatado = String.format(
                        "Categoria: %s\nDescrição: %s\nData: %s\nStatus: %s",
                        categoria, descricao, dataHora, status
                );

                problemas.add(problemaFormatado);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return problemas;
    }

    public List<String> carregarCategorias() {
        List<String> categorias = new ArrayList<>();
        try(SQLiteDatabase db = new BancodeDados(context).getReadableDatabase()) {
            String query = "SELECT nome FROM categorias";
            Cursor cursor = db.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                do {
                    categorias.add(cursor.getString(0)); // Adiciona o nome da categoria à lista
                } while (cursor.moveToNext());
            }
        }
        return categorias;
    }

    public void inserirProblema(int categoriaId, int usuarioId, String descricao, byte[] foto, double latitude, double longitude, String dataHora, String status) {
        try (SQLiteDatabase db = new BancodeDados(context).getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put("categoria_id", categoriaId);
            values.put("usuario_id", usuarioId);
            values.put("descricao", descricao);
            values.put("foto", foto);
            values.put("latitude", latitude);
            values.put("longitude", longitude);
            values.put("data_hora", dataHora);
            values.put("status", status);
            db.insert("problemas", null, values);
        }
    }

    public int buscarcategoriaID(String categoryName) {
        int categoryId = -1;
        SQLiteDatabase db = new BancodeDados(context).getReadableDatabase();
        String query = "SELECT id FROM categorias WHERE nome = ?";
        Cursor cursor = db.rawQuery(query, new String[]{categoryName});
        if (cursor.moveToFirst()) {
            categoryId = cursor.getInt(0);
        }
        cursor.close();
        return categoryId;
    }

    public int obterIdUsuario(String email, String senha) {
        SQLiteDatabase db = new BancodeDados(context).getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM usuarios WHERE email = ? AND senha = ?", new String[]{email, senha});
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(0);
            cursor.close();
            return id;
        }
        cursor.close();
        return -1;
    }
}

