package com.example.cityreport.EditarProblema;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cityreport.BancoDados.BancodadosDAO;
import com.example.cityreport.R;
import com.example.cityreport.PaginaInicial.problema.Problema;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class EditarProblema extends AppCompatActivity implements OnMapReadyCallback {

    private TextInputEditText etDescricao;
    private Spinner spinnerCategorias;
    private ImageView imgProblema;
    private GoogleMap mMap;
    private Button btnAtualizarFoto, btnSalvar, btnCancelar;

    private Problema problema;
    private BancodadosDAO bancodadosDAO;
    private byte[] fotoAtualizada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pg_editar_problema);

        // Inicializa componentes
        etDescricao = findViewById(R.id.etDescricao);
        spinnerCategorias = findViewById(R.id.spinnerCategorias);
        imgProblema = findViewById(R.id.imgProblema);
        btnAtualizarFoto = findViewById(R.id.btnAtualizarFoto);
        btnSalvar = findViewById(R.id.btnSalvar);
        btnCancelar = findViewById(R.id.btnCancelar);

        // Inicializa o DAO
        bancodadosDAO = new BancodadosDAO(this);

        // Obtém o problema passado pela tela anterior
        problema = (Problema) getIntent().getSerializableExtra("problema");
        if (problema == null) {
            Toast.makeText(this, "Erro ao carregar problema", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Configura os dados do problema na tela
        carregarDadosProblema();

        // Configura o mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Configura listeners
        btnAtualizarFoto.setOnClickListener(v -> atualizarFoto());
        btnSalvar.setOnClickListener(v -> salvarAlteracoes());
        btnCancelar.setOnClickListener(v -> finish());
    }

    private void carregarDadosProblema() {
        etDescricao.setText(problema.getDescricao());

        // Carrega categorias no spinner
        List<String> categorias = bancodadosDAO.carregarCategorias();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categorias);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategorias.setAdapter(adapter);

        // Seleciona a categoria atual
        int posicaoCategoria = categorias.indexOf(problema.getCategoria());
        if (posicaoCategoria >= 0) {
            spinnerCategorias.setSelection(posicaoCategoria);
        }

        // Carrega a foto (se existir) em uma thread separada
        new Thread(() -> {
            byte[] foto = bancodadosDAO.buscarFotoProblema(problema.getId());
            if (foto != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(foto, 0, foto.length);
                runOnUiThread(() -> imgProblema.setImageBitmap(bitmap));
            }
        }).start();
    }

    private void atualizarFoto() {
        // Implemente a lógica para atualizar a foto aqui
        Toast.makeText(this, "Funcionalidade de atualizar foto", Toast.LENGTH_SHORT).show();
    }

    private void salvarAlteracoes() {
        String novaDescricao = etDescricao.getText().toString().trim();
        String novaCategoria = (String) spinnerCategorias.getSelectedItem();

        if (novaDescricao.isEmpty()) {
            Toast.makeText(this, "Informe uma descrição", Toast.LENGTH_SHORT).show();
            return;
        }

        // Atualiza no banco de dados sem a foto (evita SQLiteBlobTooBigException)
        boolean sucesso = bancodadosDAO.atualizarProblema(
                problema.getId(),
                bancodadosDAO.buscarcategoriaID(novaCategoria),
                novaDescricao,
                null // Não atualiza a foto para evitar o erro
        );

        if (sucesso) {
            Toast.makeText(this, "Problema atualizado com sucesso!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Erro ao atualizar problema", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Adiciona marcador na localização do problema
        LatLng localizacao = new LatLng(problema.getLatitude(), problema.getLongitude());
        mMap.addMarker(new MarkerOptions().position(localizacao).title("Local do problema"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(localizacao, 15));
    }
}