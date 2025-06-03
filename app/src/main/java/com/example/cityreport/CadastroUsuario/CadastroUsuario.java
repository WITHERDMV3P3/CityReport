package com.example.cityreport.CadastroUsuario;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cityreport.BancoDados.BancodadosDAO;
import com.example.cityreport.BancoDados.BancodeDados;
import com.example.cityreport.BancoDados.EncDados;
import com.example.cityreport.MainActivity;
import com.example.cityreport.R;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;

public class CadastroUsuario extends AppCompatActivity {

    TextInputEditText nome;
    TextInputEditText email;
    TextInputEditText senha;
    Button cadastrar;
    Button login;
    EncDados dados = new EncDados();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.pg_cadastrousuario);
        nome = findViewById(R.id.Nome);
        email = findViewById(R.id.cdEmail);
        senha = findViewById(R.id.cdSenha);
        cadastrar = findViewById(R.id.btnCadastrarProblema);
        login = findViewById(R.id.Login);

        cadastrar.setOnClickListener(new View.OnClickListener() {
            BancodadosDAO dao = new BancodadosDAO(getApplicationContext());
            @Override
            public void onClick(View v) {

                String nomeusuario = nome.getText().toString();
                String emailusuario = email.getText().toString();
                String senhausuario = senha.getText().toString();

                dados.setNome(nomeusuario);
                dados.setEmail(emailusuario);
                dados.setSenha(senhausuario);


                dao.inserirDadosUsuario(dados);
                Toast.makeText(CadastroUsuario.this, "Usuário cadastrado com sucesso!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CadastroUsuario.this, MainActivity.class);
                    startActivity(intent);
                    finish();
            }
        });



        login.setOnClickListener(v -> {
            Intent intent = new Intent(CadastroUsuario.this, MainActivity.class);
            startActivity(intent);
            finish();
        });


    }
}
