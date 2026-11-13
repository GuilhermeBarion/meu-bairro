package br.com.urbantech.meubairro.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import br.com.urbantech.meubairro.models.Ocorrencia;
import br.com.urbantech.meubairro.repositories.OcorrenciaRepository;
import br.com.urbantech.meubairro.utils.Resultado;

import java.util.List;

/** Mantém a lista de ocorrências (todas ou só as do usuário), o estado de carregamento e o erro. */
public class OcorrenciaViewModel extends ViewModel {
    private final OcorrenciaRepository repo = new OcorrenciaRepository();
    private final MutableLiveData<List<Ocorrencia>> lista = new MutableLiveData<>();
    private final MutableLiveData<Boolean> carregando = new MutableLiveData<>(false);
    private final MutableLiveData<String> erro = new MutableLiveData<>();

    public LiveData<List<Ocorrencia>> getLista() { return lista; }
    public LiveData<Boolean> getCarregando() { return carregando; }
    public LiveData<String> getErro() { return erro; }

    public void carregar(boolean somenteDoUsuario) {
        carregando.setValue(true);
        erro.setValue(null);
        Resultado<List<Ocorrencia>> cb = Resultado.de(
                l -> {
                    lista.setValue(l);
                    carregando.setValue(false);
                },
                msg -> {
                    erro.setValue(msg);
                    carregando.setValue(false);
                });
        if (somenteDoUsuario) {
            FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
            if (u == null) {
                cb.onErro("Sessão expirada. Entre novamente.");
                return;
            }
            repo.listarDoUsuario(u.getUid(), cb);
        } else {
            repo.listarTodas(cb);
        }
    }
}
