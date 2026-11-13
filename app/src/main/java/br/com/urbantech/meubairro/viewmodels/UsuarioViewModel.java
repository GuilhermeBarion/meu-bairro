package br.com.urbantech.meubairro.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import br.com.urbantech.meubairro.models.Usuario;
import br.com.urbantech.meubairro.repositories.UsuarioRepository;
import br.com.urbantech.meubairro.utils.Resultado;

/** Perfil do usuário logado, compartilhado entre as telas da MainActivity. */
public class UsuarioViewModel extends ViewModel {
    private final UsuarioRepository repo = new UsuarioRepository();
    private final MutableLiveData<Usuario> usuario = new MutableLiveData<>();
    private final MutableLiveData<String> erro = new MutableLiveData<>();

    public LiveData<Usuario> getUsuario() { return usuario; }
    public LiveData<String> getErro() { return erro; }

    public void carregar() {
        FirebaseUser fu = FirebaseAuth.getInstance().getCurrentUser();
        if (fu == null) return;
        repo.carregar(fu, Resultado.de(usuario::setValue, erro::setValue));
    }
}
