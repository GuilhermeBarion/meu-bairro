package br.com.urbantech.meubairro.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import br.com.urbantech.meubairro.R;
import br.com.urbantech.meubairro.activities.DetalhesOcorrenciaActivity;
import br.com.urbantech.meubairro.adapters.OcorrenciaAdapter;
import br.com.urbantech.meubairro.databinding.FragmentListaOcorrenciasBinding;
import br.com.urbantech.meubairro.models.Ocorrencia;
import br.com.urbantech.meubairro.utils.Constantes;
import br.com.urbantech.meubairro.utils.FiltroUtils;
import br.com.urbantech.meubairro.utils.ViewUtils;
import br.com.urbantech.meubairro.viewmodels.OcorrenciaViewModel;

import java.util.ArrayList;
import java.util.List;

/** Lista com pesquisa e filtros. Serve às abas "Ocorrências" (todas) e "Minhas ocorrências". */
public class ListaOcorrenciasFragment extends Fragment {
    private static final String ARG_MINHAS = "minhas";
    private static final String ARG_BUSCA = "busca";
    private static final String ARG_CATEGORIA = "categoria";

    public static ListaOcorrenciasFragment novo(boolean minhas, @Nullable String busca, @Nullable String categoria) {
        ListaOcorrenciasFragment f = new ListaOcorrenciasFragment();
        Bundle a = new Bundle();
        a.putBoolean(ARG_MINHAS, minhas);
        a.putString(ARG_BUSCA, busca);
        a.putString(ARG_CATEGORIA, categoria);
        f.setArguments(a);
        return f;
    }

    private FragmentListaOcorrenciasBinding b;
    private OcorrenciaViewModel vm;
    private OcorrenciaAdapter adapter;
    private boolean minhas;
    private String categoriaFiltro; // null = todas
    private String statusFiltro;    // null = todos

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle s) {
        b = FragmentListaOcorrenciasBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Bundle args = requireArguments();
        minhas = args.getBoolean(ARG_MINHAS);
        vm = new ViewModelProvider(this).get(OcorrenciaViewModel.class);

        adapter = new OcorrenciaAdapter(o -> {
            Intent i = new Intent(requireContext(), DetalhesOcorrenciaActivity.class);
            i.putExtra(Constantes.EXTRA_OCORRENCIA_ID, o.getId());
            startActivity(i);
        });
        b.recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        b.recycler.setAdapter(adapter);
        b.swipe.setOnChildScrollUpCallback((p, c) -> b.recycler.canScrollVertically(-1));
        b.swipe.setOnRefreshListener(() -> vm.carregar(minhas));
        b.btnTentar.setOnClickListener(v -> vm.carregar(minhas));

        configurarFiltros(args.getString(ARG_BUSCA), args.getString(ARG_CATEGORIA));

        vm.getLista().observe(getViewLifecycleOwner(), l -> renderizar());
        vm.getCarregando().observe(getViewLifecycleOwner(), c -> renderizar());
        vm.getErro().observe(getViewLifecycleOwner(), e -> renderizar());
    }

    private void configurarFiltros(@Nullable String busca, @Nullable String categoria) {
        List<String> cats = new ArrayList<>();
        cats.add(getString(R.string.filtro_todas_categorias));
        for (String c : Constantes.CATEGORIAS) cats.add(c);
        List<String> sts = new ArrayList<>();
        sts.add(getString(R.string.filtro_todos_status));
        for (String s : Constantes.STATUS) sts.add(s);

        b.autoCategoria.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, cats));
        b.autoStatus.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, sts));

        categoriaFiltro = categoria;
        b.autoCategoria.setText(categoria != null ? categoria : cats.get(0), false);
        b.autoStatus.setText(sts.get(0), false);
        if (busca != null) b.editBusca.setText(busca);

        b.autoCategoria.setOnItemClickListener((p, v, pos, id) -> {
            categoriaFiltro = pos == 0 ? null : Constantes.CATEGORIAS[pos - 1];
            renderizar();
        });
        b.autoStatus.setOnItemClickListener((p, v, pos, id) -> {
            statusFiltro = pos == 0 ? null : Constantes.STATUS[pos - 1];
            renderizar();
        });
        b.editBusca.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) { }
            @Override public void onTextChanged(CharSequence s, int st, int bf, int c) { renderizar(); }
            @Override public void afterTextChanged(Editable s) { }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        vm.carregar(minhas); // o status pode ter sido alterado por um administrador
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        b = null;
    }

    private void renderizar() {
        if (b == null) return;
        List<Ocorrencia> todas = vm.getLista().getValue();
        boolean carregando = Boolean.TRUE.equals(vm.getCarregando().getValue());
        String erro = vm.getErro().getValue();
        boolean semDados = todas == null || todas.isEmpty();

        b.swipe.setRefreshing(carregando && !semDados);
        ViewUtils.mostrar(b.progress, carregando && semDados);
        ViewUtils.mostrar(b.layoutErro, erro != null && semDados);
        if (erro != null) b.txtErro.setText(erro);
        if (erro != null && !semDados && getContext() != null) {
            Toast.makeText(getContext(), erro, Toast.LENGTH_LONG).show();
        }

        if (todas == null) return;
        List<Ocorrencia> filtradas = FiltroUtils.filtrar(
                todas, ViewUtils.texto(b.editBusca), categoriaFiltro, statusFiltro);
        adapter.submitList(filtradas);

        boolean vazio = filtradas.isEmpty() && !carregando && erro == null;
        ViewUtils.mostrar(b.txtVazio, vazio);
        if (vazio) {
            if (semDados) {
                b.txtVazio.setText(minhas
                        ? "Você ainda não registrou nenhuma ocorrência."
                        : "Nenhuma ocorrência registrada ainda.");
            } else {
                b.txtVazio.setText("Nenhuma ocorrência encontrada com esses filtros.");
            }
        }
    }
}
