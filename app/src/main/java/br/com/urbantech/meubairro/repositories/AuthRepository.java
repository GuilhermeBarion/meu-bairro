package br.com.urbantech.meubairro.repositories;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import br.com.urbantech.meubairro.utils.ErroUtils;
import br.com.urbantech.meubairro.utils.Resultado;

public class AuthRepository {
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final UsuarioRepository usuarios = new UsuarioRepository();
    private final OcorrenciaRepository ocorrencias = new OcorrenciaRepository();

    public FirebaseUser usuarioAtual() {
        return auth.getCurrentUser();
    }

    public void login(String email, String senha, Resultado<FirebaseUser> cb) {
        auth.signInWithEmailAndPassword(email, senha)
                .addOnSuccessListener(r -> cb.onSucesso(r.getUser()))
                .addOnFailureListener(e -> cb.onErro(ErroUtils.traduzir(e)));
    }

    /** Cria a conta no Authentication e o perfil no Firestore (senha nunca é salva em texto: o Firebase guarda o hash). */
    public void cadastrar(String nome, String email, String senha, Resultado<FirebaseUser> cb) {
        auth.createUserWithEmailAndPassword(email, senha)
                .addOnSuccessListener(r -> {
                    FirebaseUser u = r.getUser();
                    if (u == null) {
                        cb.onErro("Não foi possível criar a conta.");
                        return;
                    }
                    u.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(nome).build())
                            .addOnCompleteListener(t1 ->
                                    // Se a gravação do perfil falhar, a MainActivity tenta recriá-lo ao abrir.
                                    usuarios.criar(u.getUid(), nome, email, Resultado.de(
                                            x -> cb.onSucesso(u), msg -> cb.onSucesso(u))));
                })
                .addOnFailureListener(e -> cb.onErro(ErroUtils.traduzir(e)));
    }

    public void recuperarSenha(String email, Resultado<Void> cb) {
        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(v -> cb.onSucesso(null))
                .addOnFailureListener(e -> cb.onErro(ErroUtils.traduzir(e)));
    }

    public void sair() {
        auth.signOut();
    }

    /** Reautentica com a senha, apaga ocorrências e perfil, e por fim remove a conta do Authentication. */
    public void excluirConta(String senha, Resultado<Void> cb) {
        FirebaseUser u = auth.getCurrentUser();
        if (u == null || u.getEmail() == null) {
            cb.onErro("Sessão expirada. Entre novamente.");
            return;
        }
        AuthCredential cred = EmailAuthProvider.getCredential(u.getEmail(), senha);
        u.reauthenticate(cred)
                .<Void>continueWithTask(t -> {
                    if (!t.isSuccessful()) throw t.getException();
                    return ocorrencias.excluirDoUsuario(u.getUid());
                })
                .<Void>continueWithTask(t -> {
                    if (!t.isSuccessful()) throw t.getException();
                    return usuarios.excluir(u.getUid());
                })
                .<Void>continueWithTask(t -> {
                    if (!t.isSuccessful()) throw t.getException();
                    return u.delete();
                })
                .addOnSuccessListener(v -> cb.onSucesso(null))
                .addOnFailureListener(e -> cb.onErro(ErroUtils.traduzir(e)));
    }
}
