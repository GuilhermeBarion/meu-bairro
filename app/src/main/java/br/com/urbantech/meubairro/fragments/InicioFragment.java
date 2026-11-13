package br.com.urbantech.meubairro.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.chip.Chip;

import br.com.urbantech.meubairro.R;
import br.com.urbantech.meubairro.activities.DetalhesOcorrenciaActivity;
import br.com.urbantech.meubairro.activities.MainActivity;
import br.com.urbantech.meubairro.activities.RegistrarOcorrenciaActivity;
import br.com.urbantech.meubairro.adapters.OcorrenciaAdapter;
import br.com.urbantech.meubairro.databinding.FragmentInicioBinding;
import br.com.urbantech.meubairro.models.Ocorrencia;
import br.com.urbantech.meubairro.utils.Constantes;
import br.com.urbantech.meubairro.utils.ViewUtils;
import br.com.urbantech.meubairro.viewmodels.OcorrenciaViewModel;
import br.com.urbantech.meubairro.viewmodels.UsuarioViewModel;

import java.util.ArrayList;
import java.util.List;

public class InicioFragment extends Fragment {
    private static final int RECENTES = 3;

    private FragmentInicioBinding b;
    private OcorrenciaViewModel vm;
    private OcorrenciaAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle s) {
        b = FragmentInicioBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        vm = new ViewModelProvider(this).get(OcorrenciaViewModel.class);
        UsuarioViewModel uvm = new ViewModelProvider(requireActivity()).get(UsuarioViewModel.class);

        adapter = new OcorrenciaAdapter(o -> {
            Intent i = new Intent(requireContext(), DetalhesOcorrenciaActivity.class);
            i.putExtra(Constantes.EXTRA_OCORRENCIA_ID, o.getId());
            startActivity(i);
        });
        b.recyclerRecentes.setLayoutManager(new LinearLayoutManager(requireContext()));
        b.recyclerRecentes.setAdapter(adapter);
        b.recyclerRecentes.setNestedScrollingEnabled(false);

        uvm.getUsuario().observe(getViewLifecycleOwner(), u -> {
            if (u != null) b.txtSaudacao.setText(getString(R.string.saudacao, primeiroNome(u.getNome())));
        });

        for (String categoria : Constantes.CATEGORIAS) {
            Chip chip = new Chip(requireContext());
            chip.setText(categoria);
            chip.setOnClickListener(v -> abrirListaCom(null, categoria));
            b.chipsCategorias.addView(chip);
        }

        b.btnRegistrar.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), RegistrarOcorrenciaActivity.class)));
        b.btnVerTodas.setOnClickListener(v -> abrirListaCom(null, null));
        b.tilBusca.setEndIconOnClickListener(v -> buscar());
        b.editBusca.setOnEditorActionListener((v, actionId, e) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                buscar();
                return true;
            }
            return false;
        });
        b.btnTentar.setOnClickListener(v -> vm.carregar(false));

        vm.getLista().observe(getViewLifecycleOwner(), l -> renderizar());
        vm.getCarregando().observe(getViewLifecycleOwner(), c -> renderizar());
        vm.getErro().observe(getViewLifecycleOwner(), e -> renderizar());
    }

    @Override
    public void onResume() {
        super.onResume();
        vm.carregar(false); // atualiza ao voltar do cadastro de ocorrência
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        b = null;
    }

    private void buscar() {
        String q = ViewUtils.texto(b.editBusca);
        abrirListaCom(q.isEmpty() ? null : q, null);
    }

    private void abrirListaCom(String busca, String categoria) {
        ((MainActivity) requireActivity()).abrirOcorrencias(busca, categoria);
    }

    private String primeiroNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) return "cidadão";
        return nome.trim().split("\\s+")[0];
    }

    private void renderizar() {
        if (b == null) return;
        List<Ocorrencia> l = vm.getLista().getValue();
        boolean carregando = Boolean.TRUE.equals(vm.getCarregando().getValue());
        String erro = vm.getErro().getValue();
        boolean semDados = l == null || l.isEmpty();

        ViewUtils.mostrar(b.progress, carregando && semDados);
        ViewUtils.mostrar(b.layoutErro, erro != null && semDados);
        if (erro != null) b.txtErro.setText(erro);
        ViewUtils.mostrar(b.txtVazio, semDados && !carregando && erro == null);

        if (l == null) return;
        int pendentes = 0, andamento = 0, resolvidas = 0;
        for (Ocorrencia o : l) {
            String st = o.getStatus();
            if (Constantes.STATUS_RECEBIDA.equals(st) || Constantes.STATUS_EM_ANALISE.equals(st)) pendentes++;
            else if (Constantes.STATUS_EM_ANDAMENTO.equals(st)) andamento++;
            else if (Constantes.STATUS_RESOLVIDA.equals(st)) resolvidas++;
        }
        b.txtTotal.setText(String.valueOf(l.size()));
        b.txtPendentes.setText(String.valueOf(pendentes));
        b.txtAndamento.setText(String.valueOf(andamento));
        b.txtResolvidas.setText(String.valueOf(resolvidas));
        adapter.submitList(new ArrayList<>(l.subList(0, Math.min(RECENTES, l.size()))));
    }
}
