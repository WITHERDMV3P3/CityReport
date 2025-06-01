package com.example.cityreport.PaginaInicial;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cityreport.BancoDados.BancodadosDAO;
import com.example.cityreport.EditarProblema.EditarProblema;
import com.example.cityreport.PaginaReporte.PaginaCadastro;
import com.example.cityreport.R;
import com.example.cityreport.PaginaInicial.problema.Problema;

import java.util.List;

public class PaginaInicial extends AppCompatActivity {

    private ListView listViewProblemas;
    private Button btnCadastrarProblema;
    private List<Problema> listaProblemas;
    private ProblemaAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pg_inicial);

        listViewProblemas = findViewById(R.id.listViewProblemas);
        btnCadastrarProblema = findViewById(R.id.btnCadastrarProblema);

        // Obter ID do usuário logado
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        int usuarioId = sharedPreferences.getInt("usuario_id", -1);

        // Carregar problemas
        carregarProblemas(usuarioId);

        btnCadastrarProblema.setOnClickListener(v -> {
            Intent intent = new Intent(PaginaInicial.this, PaginaCadastro.class);
            startActivity(intent);
        });
    }

    private void carregarProblemas(int usuarioId) {
        BancodadosDAO dao = new BancodadosDAO(this);
        listaProblemas = dao.buscarProblemasPorUsuario(usuarioId);

        if (listaProblemas.isEmpty()) {
            listaProblemas.add(new Problema(0, "Nenhum problema", "Nenhum problema reportado ainda",
                    "", "", 0, 0, false));
        }

        adapter = new ProblemaAdapter(this, listaProblemas);
        listViewProblemas.setAdapter(adapter);
    }

    // Classe Adapter
    private class ProblemaAdapter extends ArrayAdapter<Problema> {

        public ProblemaAdapter(Context context, List<Problema> problemas) {
            super(context, R.layout.item_problema, problemas);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_problema, parent, false);
            }

            Problema problema = getItem(position);

            TextView textCategoria = convertView.findViewById(R.id.textCategoria);
            TextView textDescricao = convertView.findViewById(R.id.textDescricao);
            TextView textData = convertView.findViewById(R.id.textData);
            TextView textStatus = convertView.findViewById(R.id.textStatus);
            ImageButton menuButton = convertView.findViewById(R.id.menu_button);

            textCategoria.setText(problema.getCategoria());
            textDescricao.setText(problema.getDescricao());
            textData.setText(problema.getDataHora());
            textStatus.setText(problema.getStatus());

            // Configurar o menu de três pontos
            menuButton.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(getContext(), v);
                popup.getMenuInflater().inflate(R.menu.menu_problema, popup.getMenu());

                popup.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.action_edit) {
                        editarProblema(problema);
                        return true;
                    } else if (item.getItemId() == R.id.action_delete) {
                        confirmarExclusao(problema, position);
                        return true;
                    }
                    return false;
                });

                popup.show();
            });

            return convertView;
        }
    }

    private void editarProblema(Problema problema) {
        // Busca a foto apenas quando for editar
        new Thread(() -> {
            byte[] foto = new BancodadosDAO(PaginaInicial.this).buscarFotoProblema(problema.getId());

            runOnUiThread(() -> {
                Intent intent = new Intent(PaginaInicial.this, EditarProblema.class);
                intent.putExtra("problema", problema);

                // Se encontrou foto, passa junto
                if (foto != null) {
                    intent.putExtra("foto", foto);
                }

                startActivity(intent);
            });
        }).start();
    }

    private void confirmarExclusao(Problema problema, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar exclusão")
                .setMessage("Deseja realmente excluir este problema?")
                .setPositiveButton("Excluir", (dialog, which) -> {
                    excluirProblema(problema, position);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void excluirProblema(Problema problema, int position) {
        BancodadosDAO dao = new BancodadosDAO(this);
        if (dao.excluirProblema(problema.getId())) {
            listaProblemas.remove(position);
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "Problema excluído", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Erro ao excluir", Toast.LENGTH_SHORT).show();
        }
    }
}