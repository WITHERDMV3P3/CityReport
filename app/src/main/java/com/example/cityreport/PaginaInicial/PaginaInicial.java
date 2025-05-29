package com.example.cityreport.PaginaInicial;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cityreport.PaginaReporte.PaginaCadastro;
import com.example.cityreport.R;

public class PaginaInicial extends AppCompatActivity {

    Button btnCadastrarProblema;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.pg_inicial);
        btnCadastrarProblema = findViewById(R.id.btnCadastrarProblema);

        btnCadastrarProblema.setOnClickListener(v -> {
            Intent intent = new Intent(PaginaInicial.this, PaginaCadastro.class);
            startActivity(intent);
            finish();
        });

    }
}