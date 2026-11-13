package br.com.urbantech.meubairro.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import br.com.urbantech.meubairro.R;
import br.com.urbantech.meubairro.activities.AdminActivity;
import br.com.urbantech.meubairro.activities.LoginActivity;
import br.com.urbantech.meubairro.databinding.FragmentPerfilBinding;
import br.com.urbantech.meubairro.models.Usuario;
import br.com.urbantech.meubairro.repositories.AuthRepository;
import br.com.urbantech.meubairro.repositories.UsuarioRepository;
import br.com.urbantech.meubairro.utils.DialogUtils;
import br.com.urbantech.meubairro.utils.NetworkUtils;
import br.com.urbantech.meubairro.utils.Resultado;
import br.com.urbantech.meubairro.utils.ViewUtils;
import br.com.urbantech.meubairro.viewmodels.UsuarioViewModel;

public class PerfilFragment extends Fragment {
    private FragmentPerfilBinding b;
    private UsuarioViewModel uvm;
    private final AuthRepository auth = new AuthRepository();
    private final UsuarioRepository usuarios = new UsuarioRepository();
    private Usuario usuario;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle s) {
        b = FragmentPerfilBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        uvm = new ViewModelProvider(requireActivity()).get(UsuarioViewModel.class);
        uvm.getUsuario().observe(getViewLifecycleOwner(), u -> {
            usuario = u;
            if (b == null || u == null) return;
            b.txtNome.setText(u.getNome());
            b.txtEmail.setText(u.getEmail());
            b.txtTipo.setText(u.isAdmin() ? "Administrador" : "Cidadão");
            ViewUtils.mostrar(b.btnAdmin, u.isAdmin());
        });

        b.btnEditar.setOnClickListener(v -> editarNome());
        b.btnAdmin.setOnClickListener(v -> startActivity(new Intent(requireContext(), AdminActivity.class)));
        b.btnSobre.setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.sobre)
                .setMessage(R.string.sobre_texto)
                .setPositiveButton("OK", null)
                .show());
        b.btnSair.setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Sair da conta")
                .setMessage("Deseja realmente sair?")
                .setPositiveButton("Sair", (d, w) -> {
                    auth.sair();
                    irParaLogin();
                })
                .setNegativeButton("Cancelar", null)
                .show());
        b.btnExcluir.setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Excluir conta")
                .setMessage("Sua conta, seu perfil e todas as ocorrências que você registrou serão apagados "
                        + "permanentemente. Esta ação não pode ser desfeita.")
                .setPositiveButton("Continuar", (d, w) -> DialogUtils.pedirTexto(
                        requireContext(), "Confirme sua senha", "Senha",
                        InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD,
                        null, this::excluirConta))
                .setNegativeButton("Cancelar", null)
                .show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        b = null;
    }

    private void editarNome() {
        if (usuario == null) return;
        DialogUtils.pedirTexto(requireContext(), "Editar nome", "Nome",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS, usuario.getNome(), novo -> {
                    if (novo.length() < 2) {
                        Toast.makeText(requireContext(), "Informe um nome válido.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (!NetworkUtils.temConexao(requireContext())) {
                        Toast.makeText(requireContext(), "Sem conexão com a internet.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    usuarios.atualizarNome(usuario.getId(), novo, Resultado.<Void>de(v -> {
                        FirebaseUser fu = auth.usuarioAtual();
                        if (fu != null) {
                            fu.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(novo).build());
                        }
                        uvm.carregar();
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Perfil atualizado.", Toast.LENGTH_SHORT).show();
                        }
                    }, msg -> {
                        if (getContext() != null) Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                    }));
                });
    }

    private void excluirConta(String senha) {
        if (!NetworkUtils.temConexao(requireContext())) {
            Toast.makeText(requireContext(), "Sem conexão com a internet.", Toast.LENGTH_LONG).show();
            return;
        }
        ViewUtils.mostrar(b.progress, true);
        auth.excluirConta(senha, Resultado.<Void>de(v -> {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Conta excluída.", Toast.LENGTH_LONG).show();
                irParaLogin();
            }
        }, msg -> {
            if (b != null) ViewUtils.mostrar(b.progress, false);
            if (getContext() != null) Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
        }));
    }

    private void irParaLogin() {
        Intent i = new Intent(requireContext(), LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }
}
