    package com.example.cityreport;

    import android.Manifest;
    import android.app.NotificationChannel;
    import android.app.NotificationManager;
    import android.content.Intent;
    import android.content.pm.PackageManager;
    import android.media.AudioAttributes;
    import android.media.RingtoneManager;
    import android.os.Build;
    import android.os.Bundle;
    import android.view.View;
    import android.widget.Button;
    import android.widget.Toast;
    import androidx.activity.EdgeToEdge;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.core.app.ActivityCompat;
    import androidx.core.content.ContextCompat;
    import com.example.cityreport.BancoDados.BancodadosDAO;
    import com.example.cityreport.BancoDados.BancodeDados;
    import com.example.cityreport.CadastroUsuario.CadastroUsuario;
    import com.example.cityreport.PaginaInicial.PaginaInicial;
    import com.google.android.material.textfield.TextInputEditText;

    public class MainActivity extends AppCompatActivity {

        private static final int REQUEST_CODE_PERMISSIONS = 100;
        private static final String[] REQUIRED_PERMISSIONS = {
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION


        };

        TextInputEditText email, senha;
        Button cadastrar, btnLogin;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            EdgeToEdge.enable(this);
            setContentView(R.layout.pg_login);

            criarCanalDeNotificacao();

            new BancodeDados(getApplicationContext()).getReadableDatabase();

            email = findViewById(R.id.Email);
            senha = findViewById(R.id.Senha);
            cadastrar = findViewById(R.id.btnCadastrarProblema);
            btnLogin = findViewById(R.id.btnLogin);

            if (!allPermissionsGranted()) {
                ActivityCompat.requestPermissions(
                        this,
                        REQUIRED_PERMISSIONS,
                        REQUEST_CODE_PERMISSIONS
                );
            }

            cadastrar.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, CadastroUsuario.class));
                finish();
            });

            btnLogin.setOnClickListener(v -> {
                String emailUsuario = email.getText().toString().trim();
                String senhaUsuario = senha.getText().toString().trim();

                if (emailUsuario.isEmpty() || senhaUsuario.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                    return;
                }

                BancodadosDAO dao = new BancodadosDAO(getApplicationContext());

                int idUsuario = dao.obterIdUsuario(emailUsuario, senhaUsuario);

                if (idUsuario != -1) {
                    getSharedPreferences("user_prefs", MODE_PRIVATE)
                            .edit()
                            .putInt("usuario_id", idUsuario)
                            .apply();

                    startActivity(new Intent(MainActivity.this, PaginaInicial.class));
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Usuário ou senha incorretos", Toast.LENGTH_SHORT).show();
                }
            });

        }

        private boolean allPermissionsGranted() {
            for (String permission : REQUIRED_PERMISSIONS) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (requestCode == REQUEST_CODE_PERMISSIONS) {
                if (!allPermissionsGranted()) {
                    Toast.makeText(this,
                            "Permissões necessárias para utilização do app",
                            Toast.LENGTH_LONG).show();
                }
            }
        }

        private void criarCanalDeNotificacao() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String channelId = "city_report";
                CharSequence nome = "CityReport";
                String descricao = "Notificações do aplicativo CityReport";
                int importancia = NotificationManager.IMPORTANCE_DEFAULT;

                NotificationChannel canal = new NotificationChannel(channelId, nome, importancia);
                canal.setDescription(descricao);
                canal.enableVibration(true);
                canal.setVibrationPattern(new long[]{100, 200, 300});
                canal.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                        new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build());

                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(canal);
                }
            }
        }

    }