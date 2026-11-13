package br.com.urbantech.meubairro.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import br.com.urbantech.meubairro.R;
import br.com.urbantech.meubairro.adapters.OcorrenciaAdapter;
import br.com.urbantech.meubairro.databinding.ActivityAdminBinding;
import br.com.urbantech.meubairro.models.Ocorrencia;
import br.com.urbantech.meubairro.models.Usuario;
import br.com.urbantech.meubairro.repositories.OcorrenciaRepository;
import br.com.urbantech.meubairro.repositories.UsuarioRepository;
import br.com.urbantech.meubairro.utils.Constantes;
import br.com.urbantech.meubairro.utils.FiltroUtils;
import br.com.urbantech.meubairro.utils.Resultado;
import br.com.urbantech.meubairro.utils.StatusDialog;
import br.com.urbantech.meubairro.utils.ViewUtils;
import br.com.urbantech.meubairro.viewmodels.OcorrenciaViewModel;

import java.util.ArrayList;
import java.util.List;

/** Painel administrativo. O acesso é conferido no perfil (tipo) e reforçado pelas regras do Firestore. */
public class AdminActivity extends AppCompatActivity {
    private ActivityAdminBinding b;
    private OcorrenciaViewModel vm;
    private OcorrenciaAdapter adapter;
    private final OcorrenciaRepository repo = new OcorrenciaRepository();
    private Usuario admin;
    private String statusFiltro; // null = todos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityAdminBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        b.toolbar.setNavigationOnClickListener(v -> finish());

        FirebaseUser fu = FirebaseAuth.getInstance().getCurrentUser();
        if (fu == null) {
            finish();
            return;
        }
        new UsuarioRepository().carregar(fu, Resultado.de(u -> {
            if (!u.isAdmin()) {
                Toast.makeText(this, "Acesso restrito a administradores.", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            admin = u;
            iniciar();
        }, msg -> {
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            finish();
        }));
    }

    private void iniciar() {
        vm = new ViewModelProvider(this).get(OcorrenciaViewModel.class);

        adapter = new OcorrenciaAdapter(new OcorrenciaAdapter.Listener() {
            @Override
            public void onClick(Ocorrencia o) {
                Intent i = new Intent(AdminActivity.this, DetalhesOcorrenciaActivity.class);
                i.putExtra(Constantes.EXTRA_OCORRENCIA_ID, o.getId());
                startActivity(i);
            }

            @Override
            public void onAlterarStatus(Ocorrencia o) {
                StatusDialog.mostrar(AdminActivity.this, o, admin, repo, () -> vm.carregar(false));
            }
        });
        adapter.setModoAdmin(true);
        b.recycler.setLayoutManager(new LinearLayoutManager(this));
        b.recycler.setAdapter(adapter);
        b.swipe.setOnChildScrollUpCallback((p, c) -> b.recycler.canScrollVertically(-1));
        b.swipe.setOnRefreshListener(() -> vm.carregar(false));
        b.btnTentar.setOnClickListener(v -> vm.carregar(false));

        List<String> opcoes = new ArrayList<>();
        opcoes.add(getString(R.string.filtro_todos_status));
        for (String s : Constantes.STATUS) opcoes.add(s);
        b.autoStatus.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, opcoes));
        b.autoStatus.setText(opcoes.get(0), false);
        b.autoStatus.setOnItemClickListener((p, v, pos, id) -> {
            statusFiltro = pos == 0 ? null : Constantes.STATUS[pos - 1];
            renderizar();
        });

        vm.getLista().observe(this, l -> renderizar());
        vm.getCarregando().observe(this, c -> renderizar());
        vm.getErro().observe(this, e -> renderizar());
        vm.carregar(false);
    }

    private void renderizar() {
        List<Ocorrencia> todas = vm.getLista().getValue();
        boolean carregando = Boolean.TRUE.equals(vm.getCarregando().getValue());
        String erro = vm.getErro().getValue();
        boolean semDados = todas == null || todas.isEmpty();

        b.swipe.setRefreshing(carregando && !semDados);
        ViewUtils.mostrar(b.progress, carregando && semDados);
        ViewUtils.mostrar(b.layoutErro, erro != null && semDados);
        if (erro != null) b.txtErro.setText(erro);
        if (erro != null && !semDados) Toast.makeText(this, erro, Toast.LENGTH_LONG).show();

        if (todas == null) return;
        b.txtResumo.setText(resumo(todas));
        List<Ocorrencia> filtradas = FiltroUtils.filtrar(todas, null, null, statusFiltro);
        adapter.submitList(filtradas);
        boolean vazio = filtradas.isEmpty() && !carregando && erro == null;
        ViewUtils.mostrar(b.txtVazio, vazio);
        if (vazio) b.txtVazio.setText(semDados ? "Nenhuma ocorrência registrada." : "Nenhuma ocorrência com este status.");
    }

    private String resumo(List<Ocorrencia> todas) {
        StringBuilder sb = new StringBuilder("Total: " + todas.size());
        for (String s : Constantes.STATUS) {
            int n = 0;
            for (Ocorrencia o : todas) if (s.equals(o.getStatus())) n++;
            sb.append("  ·  ").append(s).append(": ").append(n);
        }
        return sb.toString();
    }
}
