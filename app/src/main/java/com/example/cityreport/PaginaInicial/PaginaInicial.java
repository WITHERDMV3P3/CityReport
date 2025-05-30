package com.example.cityreport.PaginaInicial;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cityreport.BancoDados.BancodadosDAO;
import com.example.cityreport.PaginaReporte.PaginaCadastro;
import com.example.cityreport.R;

import java.util.List;

public class PaginaInicial extends AppCompatActivity {

    private Button btnCadastrarProblema;
    private ListView listViewProblemas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.pg_inicial);

        // Inicializa os componentes
        btnCadastrarProblema = findViewById(R.id.btnCadastrarProblema);
        listViewProblemas = findViewById(R.id.listViewProblemas);

        // Obtém o ID do usuário logado
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        int usuarioId = sharedPreferences.getInt("usuario_id", -1);

        // Carrega os problemas do usuário
        carregarProblemas(usuarioId);

        // Configura o botão de cadastro
        btnCadastrarProblema.setOnClickListener(v -> {
            Intent intent = new Intent(PaginaInicial.this, PaginaCadastro.class);
            startActivity(intent);
            finish();
        });

        // Configura o clique nos itens da lista
        listViewProblemas.setOnItemClickListener((parent, view, position, id) -> {
            String itemSelecionado = (String) parent.getItemAtPosition(position);
            Toast.makeText(this, "Problema selecionado:\n" + itemSelecionado, Toast.LENGTH_SHORT).show();
        });
    }

    private void carregarProblemas(int usuarioId) {
        BancodadosDAO bancodadosDAO = new BancodadosDAO(this);
        List<String> problemas = bancodadosDAO.buscarProblemas(usuarioId);

        if (problemas.isEmpty()) {
            problemas.add("Nenhum problema reportado ainda.");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                problemas
        );

        listViewProblemas.setAdapter(adapter);
    }
}