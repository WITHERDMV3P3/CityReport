package com.example.cityreport.PaginaReporte;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.example.cityreport.BancoDados.BancodadosDAO;
import com.example.cityreport.PaginaInicial.PaginaInicial;
import com.example.cityreport.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PaginaCadastro extends AppCompatActivity implements OnMapReadyCallback {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PICK_IMAGE = 2;
    private static final int REQUEST_PERMISSIONS = 100;
    private static final int REQUEST_LOCATION_PERMISSION = 200;
    private ImageView imgPerfil;
    private Button btnReportar, btnVoltar;
    private String currentPhotoPath;
    private Uri photoURI;

    private TextInputEditText descricaoText;

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private Spinner spinnerCategorias;
    private BancodadosDAO bancodadosDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pg_cadastroreporte);

        btnVoltar = findViewById(R.id.btnvoltar);
        imgPerfil = findViewById(R.id.imgPerfil);
        spinnerCategorias = findViewById(R.id.spinnerCategorias);
        btnReportar = findViewById(R.id.btnreportar);
        descricaoText = findViewById(R.id.descricao);



        bancodadosDAO = new BancodadosDAO(this);
        loadCategories();

        imgPerfil.setOnClickListener(v -> {
            if (hasImagePermissions()) {
                showImagePickerOptions();
            } else {
                requestImagePermissions();
            }
        });

        btnVoltar.setOnClickListener(v -> {
            Intent intent = new Intent(PaginaCadastro.this, PaginaInicial.class);
            startActivity(intent);
            finish();
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnReportar.setOnClickListener(v -> {
            reportarProblema();
        });
    }

    private void reportarProblema() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        int usuarioId = sharedPreferences.getInt("usuario_id", -1);


        String descricao = descricaoText.getText() != null ? descricaoText.getText().toString().trim() : "";
        String categoriaSelecionada = (String) spinnerCategorias.getSelectedItem();

       if (photoURI == null) {
            Toast.makeText(this, "Por favor tire uma foto.", Toast.LENGTH_SHORT).show();
            return;
        }

        byte[] imagemBytes = convertImageToByteArray(photoURI);
        if (imagemBytes == null) {
            Toast.makeText(this, "Erro ao processar a imagem.", Toast.LENGTH_SHORT).show();
            return;
        }

        int categoriaId = bancodadosDAO.buscarcategoriaID(categoriaSelecionada);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        String dataHoraAtual = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

                        String status = "Pendente";

                        // Insere no banco
                        bancodadosDAO.inserirProblema(categoriaId, usuarioId, descricao, imagemBytes, latitude, longitude, dataHoraAtual, status);
                        Toast.makeText(this, "Problema reportado com sucesso!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Não foi possível obter localização.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao obter localização: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }



    private byte[] convertImageToByteArray(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            return stream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Location getCurrentLocationFromMap() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }
        return fusedLocationClient.getLastLocation().getResult();
    }


    private void loadCategories() {
        List<String> categories = bancodadosDAO.carregarCategorias();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategorias.setAdapter(adapter);
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

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_LOCATION_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSIONS) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                showImagePickerOptions();
            } else {
                Toast.makeText(this, "Permissões necessárias para ARMAZENAMENTO e CAMERA", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if (mMap != null) {
                        mMap.setMyLocationEnabled(true);
                        getCurrentLocation();
                    }
                }
            } else {
                Toast.makeText(this, "Permissão de localização necessária", Toast.LENGTH_SHORT).show();
            }
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE:
                    imgPerfil.setImageURI(photoURI);
                    break;
                case REQUEST_PICK_IMAGE:
                    if (data != null) {
                        Uri selectedImage = data.getData();
                        imgPerfil.setImageURI(selectedImage);
                    }
                    break;
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            getCurrentLocation();
        } else {
            requestLocationPermission();
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.addMarker(new MarkerOptions().position(currentLocation).title("Você está aqui"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                    } else {
                        Toast.makeText(this, "Não foi possível obter a localização atual. Centralizando em São Paulo.", Toast.LENGTH_LONG).show();
                        LatLng sp = new LatLng(-23.55052, -46.633308);
                        mMap.addMarker(new MarkerOptions().position(sp).title("São Paulo"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sp, 12));
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao obter localização: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}