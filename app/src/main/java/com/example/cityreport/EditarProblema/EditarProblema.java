package com.example.cityreport.EditarProblema;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.cityreport.BancoDados.BancodadosDAO;
import com.example.cityreport.PaginaInicial.PaginaInicial;
import com.example.cityreport.PaginaReporte.PaginaCadastro;
import com.example.cityreport.R;
import com.example.cityreport.PaginaInicial.problema.Problema;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class EditarProblema extends AppCompatActivity implements OnMapReadyCallback {

    private TextInputEditText etDescricao;
    private Spinner spinnerCategorias;
    private ImageView imgProblema;
    private GoogleMap mMap;
    private Button btnSalvar, btnCancelar;
    private Problema problema;
    private BancodadosDAO bancodadosDAO;

    private Uri photoURI;

    private String currentPhotoPath;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PICK_IMAGE = 2;
    private static final int REQUEST_PERMISSIONS = 100;
    private static final int REQUEST_LOCATION_PERMISSION = 200;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pg_editar_problema);

        etDescricao = findViewById(R.id.etDescricao);
        spinnerCategorias = findViewById(R.id.spinnerCategorias);
        imgProblema = findViewById(R.id.imgProblema);
        btnSalvar = findViewById(R.id.btnSalvar);
        btnCancelar = findViewById(R.id.btnvoltarinicial);

        bancodadosDAO = new BancodadosDAO(this);

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
                .findFragmentById(R.id.mMap);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        btnSalvar.setOnClickListener(v -> salvarAlteracoes());
        btnCancelar.setOnClickListener(v -> {
            Intent intent = new Intent(EditarProblema.this, PaginaInicial.class);
            startActivity(intent);
            finish();
        });

        imgProblema.setOnClickListener(v -> {
            if (hasImagePermissions()) {
                showImagePickerOptions();
            } else {
                requestImagePermissions();
            }
        });
    }

    private boolean hasImagePermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestImagePermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE},
                REQUEST_PERMISSIONS);
    }

    private void showImagePickerOptions() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Escolher imagem");
        builder.setItems(new CharSequence[]{"Tirar Foto", "Cancelar"}, (dialog, which) -> {
            switch (which) {
                case 0:
                    dispatchTakePictureIntent();
                    break;
                case 1:
                    dialog.dismiss();
                    break;
            }
        });
        builder.show();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) == null) {
            Toast.makeText(this, "App de câmera não encontrado ou não permitido", Toast.LENGTH_SHORT).show();
            return;
        }

        File photoFile;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Toast.makeText(this, "Erro ao criar arquivo: " + ex.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        if (photoFile != null) {
            photoURI = FileProvider.getUriForFile(
                    this,
                    getApplicationContext().getPackageName() + ".fileprovider",
                    photoFile);

            takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
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

    private byte[] convertImageToByteArray(Uri imageUri) {
        try {
            Bitmap original = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

            // Redimensionar imagem para largura máx de 1024px (mantendo proporção)
            Bitmap scaled = Bitmap.createScaledBitmap(original, 1024, (original.getHeight() * 1024) / original.getWidth(), true);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            scaled.compress(Bitmap.CompressFormat.JPEG, 80, stream); // qualidade 80%
            return stream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void salvarAlteracoes() {
        String novaDescricao = etDescricao.getText().toString().trim();
        String novaCategoria = (String) spinnerCategorias.getSelectedItem();

        if (novaDescricao.isEmpty()) {
            Toast.makeText(this, "Informe uma descrição", Toast.LENGTH_SHORT).show();
            return;
        }

        byte[] imagemBytes;

        if (photoURI != null) {
            imagemBytes = convertImageToByteArray(photoURI);
            if (imagemBytes == null) {
                Toast.makeText(this, "Erro ao processar a nova imagem.", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            imagemBytes = bancodadosDAO.buscarFotoProblema(problema.getId());
        }

        boolean sucesso = bancodadosDAO.atualizarProblema(
                problema.getId(),
                bancodadosDAO.buscarcategoriaID(novaCategoria),
                novaDescricao,
                imagemBytes
        );

        if (sucesso) {
            Toast.makeText(this, "Problema atualizado com sucesso!", Toast.LENGTH_SHORT).show();
            finish();
            Intent intent = new Intent(EditarProblema.this, PaginaInicial.class);
            startActivity(intent);
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (photoURI != null) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoURI);

                    // Redimensionar para manter qualidade sem exagerar
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 1024, (bitmap.getHeight() * 1024) / bitmap.getWidth(), true);

                    imgProblema.setImageBitmap(scaledBitmap); // Atualiza a imagem na tela
                } catch (IOException e) {
                    Toast.makeText(this, "Erro ao carregar imagem", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }
    }

}