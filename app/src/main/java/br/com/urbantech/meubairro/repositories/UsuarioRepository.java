package br.com.urbantech.meubairro.repositories;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import br.com.urbantech.meubairro.models.Usuario;
import br.com.urbantech.meubairro.utils.Constantes;
import br.com.urbantech.meubairro.utils.ErroUtils;
import br.com.urbantech.meubairro.utils.Resultado;

public class UsuarioRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private DocumentReference ref(String uid) {
        return db.collection(Constantes.COL_USUARIOS).document(uid);
    }

    /** Cria o perfil. O tipo é sempre "usuario"; administradores são definidos manualmente no console. */
    public void criar(String uid, String nome, String email, Resultado<Usuario> cb) {
        Usuario u = new Usuario(uid, nome, email);
        ref(uid).set(u)
                .addOnSuccessListener(v -> cb.onSucesso(u))
                .addOnFailureListener(e -> cb.onErro(ErroUtils.traduzir(e)));
    }

    /** Carrega o perfil; se o documento não existir (ex.: falha no cadastro), cria um novo. */
    public void carregar(FirebaseUser fu, Resultado<Usuario> cb) {
        ref(fu.getUid()).get()
                .addOnSuccessListener(snap -> {
                    Usuario u = snap.exists() ? snap.toObject(Usuario.class) : null;
                    if (u != null) {
                        cb.onSucesso(u);
                        return;
                    }
                    String email = fu.getEmail() == null ? "" : fu.getEmail();
                    String nome = fu.getDisplayName();
                    if (nome == null || nome.trim().isEmpty()) {
                        nome = email.contains("@") ? email.substring(0, email.indexOf('@')) : "Cidadão";
                    }
                    criar(fu.getUid(), nome, email, cb);
                })
                .addOnFailureListener(e -> cb.onErro(ErroUtils.traduzir(e)));
    }

    public void atualizarNome(String uid, String nome, Resultado<Void> cb) {
        ref(uid).update("nome", nome)
                .addOnSuccessListener(v -> cb.onSucesso(null))
                .addOnFailureListener(e -> cb.onErro(ErroUtils.traduzir(e)));
    }

    public Task<Void> excluir(String uid) {
        return ref(uid).delete();
    }
}
