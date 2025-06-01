package com.example.cityreport.Mapa;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.fragment.app.FragmentActivity;

import com.example.cityreport.BancoDados.BancodadosDAO;
import com.example.cityreport.PaginaInicial.problema.Problema;
import com.example.cityreport.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class Mapa extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private BancodadosDAO dao;
    private Button btnVoltar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapa);
        btnVoltar = findViewById(R.id.btnvoltar);
        btnVoltar.setOnClickListener(v -> finish());



        dao = new BancodadosDAO(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        int usuarioId = sharedPreferences.getInt("usuario_id", -1);

        mMap = googleMap;

        List<Problema> problemas = dao.buscarProblemasPorUsuarioMapa(usuarioId);

        for (Problema problema : problemas) {
            LatLng local = new LatLng(problema.getLatitude(), problema.getLongitude());
            mMap.addMarker(new MarkerOptions()
                    .position(local)
                    .title(problema.getCategoria())
                    .snippet(problema.getDescricao() + " - " + problema.getStatus()));
        }

        if (!problemas.isEmpty()) {
            LatLng primeiro = new LatLng(problemas.get(0).getLatitude(), problemas.get(0).getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(primeiro, 15));
        }
    }
}
