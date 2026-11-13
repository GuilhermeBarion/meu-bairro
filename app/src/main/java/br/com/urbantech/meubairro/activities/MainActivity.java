package br.com.urbantech.meubairro.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;

import br.com.urbantech.meubairro.R;
import br.com.urbantech.meubairro.databinding.ActivityMainBinding;
import br.com.urbantech.meubairro.fragments.InicioFragment;
import br.com.urbantech.meubairro.fragments.ListaOcorrenciasFragment;
import br.com.urbantech.meubairro.fragments.PerfilFragment;
import br.com.urbantech.meubairro.viewmodels.UsuarioViewModel;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding b;
    private boolean trocandoPorCodigo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            irParaLogin();
            return;
        }
        b = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        UsuarioViewModel uvm = new ViewModelProvider(this).get(UsuarioViewModel.class);
        uvm.getErro().observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        });
        if (savedInstanceState == null) uvm.carregar();

        b.bottomNav.setOnItemSelectedListener(item -> {
            if (trocandoPorCodigo) return true;
            int id = item.getItemId();
            Fragment f;
            if (id == R.id.nav_inicio) f = new InicioFragment();
            else if (id == R.id.nav_ocorrencias) f = ListaOcorrenciasFragment.novo(false, null, null);
            else if (id == R.id.nav_minhas) f = ListaOcorrenciasFragment.novo(true, null, null);
            else f = new PerfilFragment();
            mostrar(f, item.getTitle());
            return true;
        });

        if (savedInstanceState == null) {
            mostrar(new InicioFragment(), getString(R.string.app_name));
        }
    }

    private void mostrar(Fragment f, CharSequence titulo) {
        b.toolbar.setTitle(titulo);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, f)
                .commit();
    }

    /** Abre a aba "Ocorrências" já com busca/categoria aplicadas (usado pela tela inicial). */
    public void abrirOcorrencias(String busca, String categoria) {
        trocandoPorCodigo = true;
        b.bottomNav.setSelectedItemId(R.id.nav_ocorrencias);
        trocandoPorCodigo = false;
        mostrar(ListaOcorrenciasFragment.novo(false, busca, categoria), getString(R.string.nav_ocorrencias));
    }

    private void irParaLogin() {
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
