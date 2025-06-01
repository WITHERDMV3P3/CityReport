package com.example.cityreport.BancoDados;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.cityreport.PaginaInicial.problema.Problema;

import java.util.ArrayList;
import java.util.List;

public class BancodadosDAO {
    final private Context context;

    public BancodadosDAO(Context context) {
        this.context = context;
    }

    public List<Problema> buscarProblemasPorUsuario(int usuarioId) {
        List<Problema> problemas = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = new BancodeDados(context).getReadableDatabase();

            String query = "SELECT p.id, c.nome, p.descricao, p.data_hora, p.status, " +
                    "p.latitude, p.longitude, p.foto IS NOT NULL as tem_foto " +
                    "FROM problemas p " +
                    "JOIN categorias c ON p.categoria_id = c.id " +
                    "WHERE p.usuario_id = ? " +
                    "ORDER BY p.data_hora DESC";

            cursor = db.rawQuery(query, new String[]{String.valueOf(usuarioId)});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Problema problema = new Problema(
                            cursor.getInt(0),    // id
                            cursor.getString(1), // categoria
                            cursor.getString(2), // descricao
                            cursor.getString(3), // data_hora
                            cursor.getString(4), // status
                            cursor.getDouble(5), // latitude
                            cursor.getDouble(6), // longitude
                            cursor.getInt(7) == 1 // tem_foto
                    );
                    problemas.add(problema);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("BancodadosDAO", "Erro ao buscar problemas: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
        return problemas;
    }

    public byte[] buscarFotoProblema(int problemaId) {
        SQLiteDatabase db = new BancodeDados(context).getReadableDatabase();
        byte[] foto = null;

        String query = "SELECT foto FROM problemas WHERE id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(problemaId)});

        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            foto = cursor.getBlob(0);
        }

        cursor.close();
        db.close();
        return foto;
    }

    public boolean atualizarProblema(int problemaId, int categoriaId, String descricao, byte[] foto) {
        SQLiteDatabase db = new BancodeDados(context).getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("categoria_id", categoriaId);
        values.put("descricao", descricao);

        if (foto != null) {
            values.put("foto", foto);
        }

        try {
            int rowsAffected = db.update(
                    "problemas",
                    values,
                    "id = ?",
                    new String[]{String.valueOf(problemaId)}
            );
            return rowsAffected > 0;
        } finally {
            db.close();
        }
    }

    // Outros métodos existentes...
    public List<String> carregarCategorias() {
        List<String> categorias = new ArrayList<>();
        try(SQLiteDatabase db = new BancodeDados(context).getReadableDatabase()) {
            String query = "SELECT nome FROM categorias";
            Cursor cursor = db.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                do {
                    categorias.add(cursor.getString(0));
                } while (cursor.moveToNext());
            }
        }
        return categorias;
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

    public boolean excluirProblema(int problemaId) {
        SQLiteDatabase db = new BancodeDados(context).getWritableDatabase();
        try {
            int rowsAffected = db.delete("problemas", "id = ?", new String[]{String.valueOf(problemaId)});
            return rowsAffected > 0;
        } finally {
            db.close();
        }
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