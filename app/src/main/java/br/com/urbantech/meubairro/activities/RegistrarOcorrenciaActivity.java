package br.com.urbantech.meubairro.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import br.com.urbantech.meubairro.databinding.ActivityRegistrarOcorrenciaBinding;
import br.com.urbantech.meubairro.models.Ocorrencia;
import br.com.urbantech.meubairro.repositories.OcorrenciaRepository;
import br.com.urbantech.meubairro.utils.Constantes;
import br.com.urbantech.meubairro.utils.ImageUtils;
import br.com.urbantech.meubairro.utils.NetworkUtils;
import br.com.urbantech.meubairro.utils.Resultado;
import br.com.urbantech.meubairro.utils.ViewUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegistrarOcorrenciaActivity extends AppCompatActivity {
    private static final String ESTADO_FOTO = "foto";
    private static final String ESTADO_CAMERA = "camera";

    private ActivityRegistrarOcorrenciaBinding b;
    private final OcorrenciaRepository repo = new OcorrenciaRepository();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private Uri fotoSelecionada;
    private Uri fotoCameraUri;
    private Double latitude;
    private Double longitude;
    private boolean preenchendoPorGps;

    private final ActivityResultLauncher<Uri> camera =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), ok -> {
                if (Boolean.TRUE.equals(ok) && fotoCameraUri != null) definirFoto(fotoCameraUri);
            });

    private final ActivityResultLauncher<String> galeria =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) definirFoto(uri);
            });

    private final ActivityResultLauncher<String[]> permissaoLocalizacao =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    (Map<String, Boolean> r) -> {
                        boolean ok = Boolean.TRUE.equals(r.get(Manifest.permission.ACCESS_FINE_LOCATION))
                                || Boolean.TRUE.equals(r.get(Manifest.permission.ACCESS_COARSE_LOCATION));
                        if (ok) obterLocalizacao();
                        else Toast.makeText(this,
                                "Permissão de localização negada. Informe o endereço manualmente.",
                                Toast.LENGTH_LONG).show();
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityRegistrarOcorrenciaBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        if (savedInstanceState != null) {
            String f = savedInstanceState.getString(ESTADO_FOTO);
            String c = savedInstanceState.getString(ESTADO_CAMERA);
            if (c != null) fotoCameraUri = Uri.parse(c);
            if (f != null) definirFoto(Uri.parse(f));
        }

        b.toolbar.setNavigationOnClickListener(v -> finish());
        b.autoCategoria.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Constantes.CATEGORIAS));

        b.btnCamera.setOnClickListener(v -> tirarFoto());
        b.btnGaleria.setOnClickListener(v -> galeria.launch("image/*"));
        b.btnRemoverFoto.setOnClickListener(v -> definirFoto(null));
        b.btnLocalizacao.setOnClickListener(v -> usarLocalizacao());
        b.btnEnviar.setOnClickListener(v -> enviar());

        // Se o usuário editar o endereço à mão, as coordenadas capturadas deixam de valer.
        b.editEndereco.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) { }
            @Override public void onTextChanged(CharSequence s, int st, int bf, int c) {
                if (!preenchendoPorGps) {
                    latitude = null;
                    longitude = null;
                }
            }
            @Override public void afterTextChanged(Editable s) { }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        if (fotoSelecionada != null) out.putString(ESTADO_FOTO, fotoSelecionada.toString());
        if (fotoCameraUri != null) out.putString(ESTADO_CAMERA, fotoCameraUri.toString());
    }

    @Override
    protected void onDestroy() {
        executor.shutdown();
        super.onDestroy();
    }

    // ---------- Foto ----------
    private void tirarFoto() {
        try {
            File dir = new File(getCacheDir(), "fotos");
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
            File arquivo = File.createTempFile("foto_", ".jpg", dir);
            fotoCameraUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", arquivo);
            camera.launch(fotoCameraUri);
        } catch (IOException | RuntimeException e) {
            Toast.makeText(this, "Não foi possível abrir a câmera. Use a galeria.", Toast.LENGTH_LONG).show();
        }
    }

    private void definirFoto(Uri uri) {
        fotoSelecionada = uri;
        ViewUtils.mostrar(b.imgPreview, uri != null);
        ViewUtils.mostrar(b.btnRemoverFoto, uri != null);
        if (uri != null) Glide.with(this).load(uri).centerCrop().into(b.imgPreview);
    }

    // ---------- Localização ----------
    private void usarLocalizacao() {
        boolean fina = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        boolean grossa = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        if (fina || grossa) {
            obterLocalizacao();
        } else {
            permissaoLocalizacao.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
        }
    }

    @SuppressLint("MissingPermission")
    private void obterLocalizacao() {
        ViewUtils.mostrar(b.progress, true);
        LocationServices.getFusedLocationProviderClient(this)
                .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, new CancellationTokenSource().getToken())
                .addOnSuccessListener(loc -> {
                    if (loc == null) {
                        ViewUtils.mostrar(b.progress, false);
                        Toast.makeText(this, "Não foi possível obter a localização. Verifique se o GPS está ativado.",
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    latitude = loc.getLatitude();
                    longitude = loc.getLongitude();
                    converterEndereco(loc.getLatitude(), loc.getLongitude());
                })
                .addOnFailureListener(e -> {
                    ViewUtils.mostrar(b.progress, false);
                    Toast.makeText(this, "Falha ao obter a localização.", Toast.LENGTH_LONG).show();
                });
    }

    private void converterEndereco(double lat, double lon) {
        executor.execute(() -> {
            String texto = String.format(Locale.US, "%.5f, %.5f", lat, lon);
            try {
                if (Geocoder.isPresent()) {
                    List<Address> r = new Geocoder(this, new Locale("pt", "BR")).getFromLocation(lat, lon, 1);
                    if (r != null && !r.isEmpty() && r.get(0).getAddressLine(0) != null) {
                        texto = r.get(0).getAddressLine(0);
                    }
                }
            } catch (IOException ignorada) {
                // mantém as coordenadas como texto
            }
            final String endereco = texto;
            runOnUiThread(() -> {
                if (isDestroyed()) return;
                ViewUtils.mostrar(b.progress, false);
                preenchendoPorGps = true;
                b.editEndereco.setText(endereco);
                preenchendoPorGps = false;
            });
        });
    }

    // ---------- Envio ----------
    private void enviar() {
        b.tilTitulo.setError(null);
        b.tilCategoria.setError(null);
        b.tilDescricao.setError(null);
        b.tilEndereco.setError(null);

        String titulo = ViewUtils.texto(b.editTitulo);
        String categoria = ViewUtils.texto(b.autoCategoria);
        String descricao = ViewUtils.texto(b.editDescricao);
        String endereco = ViewUtils.texto(b.editEndereco);

        boolean ok = true;
        if (titulo.length() < 3) {
            b.tilTitulo.setError("Informe um título (mín. 3 caracteres)");
            ok = false;
        }
        if (!Arrays.asList(Constantes.CATEGORIAS).contains(categoria)) {
            b.tilCategoria.setError("Escolha uma categoria");
            ok = false;
        }
        if (descricao.length() < 10) {
            b.tilDescricao.setError("Descreva o problema (mín. 10 caracteres)");
            ok = false;
        }
        if (endereco.isEmpty()) {
            b.tilEndereco.setError("Informe o endereço ou use sua localização");
            ok = false;
        }
        if (!ok) return;

        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        if (u == null) {
            Toast.makeText(this, "Sessão expirada. Entre novamente.", Toast.LENGTH_LONG).show();
            return;
        }
        if (!NetworkUtils.temConexao(this)) {
            Toast.makeText(this, "Sem conexão com a internet. Tente novamente quando estiver online.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        Ocorrencia o = new Ocorrencia();
        o.setTitulo(titulo);
        o.setCategoria(categoria);
        o.setDescricao(descricao);
        o.setEndereco(endereco);
        o.setLatitude(latitude);
        o.setLongitude(longitude);
        o.setUsuarioId(u.getUid());
        o.setUsuarioNome(u.getDisplayName() != null ? u.getDisplayName() : "Cidadão");
        o.setStatus(Constantes.STATUS_RECEBIDA);

        carregando(true);
        final Uri foto = fotoSelecionada;
        executor.execute(() -> {
            byte[] bytes = null;
            try {
                if (foto != null) bytes = ImageUtils.comprimir(this, foto);
            } catch (IOException e) {
                runOnUiThread(() -> {
                    carregando(false);
                    Toast.makeText(this, "Não foi possível processar a foto selecionada.", Toast.LENGTH_LONG).show();
                });
                return;
            }
            final byte[] dados = bytes;
            runOnUiThread(() -> repo.criar(o, dados, Resultado.de(
                    id -> {
                        Toast.makeText(this, "Ocorrência enviada com sucesso!", Toast.LENGTH_LONG).show();
                        finish();
                    },
                    msg -> {
                        carregando(false);
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                    })));
        });
    }

    private void carregando(boolean sim) {
        ViewUtils.mostrar(b.progress, sim);
        b.btnEnviar.setEnabled(!sim);
    }
}
