    package com.example.cityreport.PaginaInicial;

    import android.Manifest;
    import android.annotation.SuppressLint;
    import android.app.AlertDialog;
    import android.app.NotificationManager;
    import android.content.Context;
    import android.content.Intent;
    import android.content.SharedPreferences;
    import android.content.pm.PackageManager;
    import android.os.Build;
    import android.os.Bundle;
    import android.provider.Settings;
    import android.util.Log;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.ArrayAdapter;
    import android.widget.Button;
    import android.widget.ImageButton;
    import android.widget.ListView;
    import android.widget.PopupMenu;
    import android.widget.Switch;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.annotation.NonNull;
    import androidx.annotation.Nullable;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.core.app.ActivityCompat;
    import androidx.core.app.NotificationCompat;
    import androidx.core.app.NotificationManagerCompat;

    import com.example.cityreport.BancoDados.BancodadosDAO;
    import com.example.cityreport.EditarProblema.EditarProblema;
    import com.example.cityreport.Mapa.Mapa;
    import com.example.cityreport.PaginaReporte.PaginaCadastro;
    import com.example.cityreport.R;
    import com.example.cityreport.PaginaInicial.problema.Problema;

    import java.text.ParseException;
    import java.text.SimpleDateFormat;
    import java.util.Date;
    import java.util.List;
    import java.util.Locale;

    public class PaginaInicial extends AppCompatActivity {

        private ListView listViewProblemas;
        private Button btnCadastrarProblema, btnnotificacoes, btnmapa;
        private List<Problema> listaProblemas;
        private ProblemaAdapter adapter;

        @SuppressLint("MissingInflatedId")
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.pg_inicial);

            listViewProblemas = findViewById(R.id.listViewProblemas);
            btnCadastrarProblema = findViewById(R.id.btnCadastrarProblema);
            btnmapa = findViewById(R.id.btnmapa);

            btnnotificacoes = findViewById(R.id.btnnotificações);

            btnnotificacoes.setOnClickListener((new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                                        .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName())
                                        .putExtra(Settings.EXTRA_CHANNEL_ID, "city_report");
                                startActivity(intent);
                            }else {
                                Toast.makeText(PaginaInicial.this, "Versão do Android não suportada", Toast.LENGTH_SHORT).show();
                            }
                        }
                        }));

            SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
            int usuarioId = sharedPreferences.getInt("usuario_id", -1);

            carregarProblemas(usuarioId);

            btnCadastrarProblema.setOnClickListener(v -> {
                Intent intent = new Intent(PaginaInicial.this, PaginaCadastro.class);
                startActivity(intent);
            });
            btnmapa.setOnClickListener(v -> {
                Intent intent = new Intent(PaginaInicial.this, Mapa.class);
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


                convertView.setOnClickListener(v -> mostrarDialogoStatus(problema, position));

                TextView textCategoria = convertView.findViewById(R.id.textCategoria);
                TextView textDescricao = convertView.findViewById(R.id.textDescricao);
                TextView textData = convertView.findViewById(R.id.textData);
                TextView textStatus = convertView.findViewById(R.id.textStatus);
                ImageButton menuButton = convertView.findViewById(R.id.menu_button);

                textCategoria.setText(problema.getCategoria());
                textDescricao.setText(problema.getDescricao());
                try {
                    SimpleDateFormat formatoOriginal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

                    SimpleDateFormat novoFormato = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());

                    Date data = formatoOriginal.parse(problema.getDataHora());
                    String dataFormatada = novoFormato.format(data);

                    textData.setText(dataFormatada);
                } catch (ParseException e) {
                    textData.setText(problema.getDataHora());
                }

                textStatus.setText(problema.getStatus());

                switch (problema.getStatus()) {
                    case "Reportado":
                        textStatus.setTextColor(getContext().getResources().getColor(R.color.status_reportado));
                        break;
                    case "Em Análise":
                        textStatus.setTextColor(getContext().getResources().getColor(R.color.status_em_analise));
                        break;
                    case "Finalizado":
                        textStatus.setTextColor(getContext().getResources().getColor(R.color.status_finalizado));
                        break;
                }

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
            new Thread(() -> {
                byte[] foto = new BancodadosDAO(PaginaInicial.this).buscarFotoProblema(problema.getId());

                runOnUiThread(() -> {
                    Intent intent = new Intent(PaginaInicial.this, EditarProblema.class);
                    intent.putExtra("problema", problema);

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

        private void mostrarDialogoStatus(Problema problema, int position) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Alterar status");

            String[] statusOpcoes = {"Reportado", "Em Análise", "Finalizado"};

            builder.setItems(statusOpcoes, (dialog, which) -> {
                String novoStatus = statusOpcoes[which];
                problema.setStatus(novoStatus);

                boolean sucesso = new BancodadosDAO(this).atualizarStatusProblema(problema.getId(), novoStatus);

                if (sucesso) {
                    listaProblemas.set(position, problema);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Status atualizado para " + novoStatus, Toast.LENGTH_SHORT).show();
                    notificarStatusAtualizado(novoStatus);
                } else {
                    Toast.makeText(this, "Erro ao atualizar status", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Cancelar", null);
            builder.show();
        }

        private void notificarStatusAtualizado(String novoStatus) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "city_report")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("Status atualizado")
                    .setContentText("Novo status: " + novoStatus)
                    .setPriority(NotificationManager.IMPORTANCE_DEFAULT)
                    .setAutoCancel(true);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            notificationManager.notify(2, builder.build());
        }
    }