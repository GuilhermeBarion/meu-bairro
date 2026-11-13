package br.com.urbantech.meubairro.repositories;

import android.net.Uri;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import br.com.urbantech.meubairro.models.HistoricoStatus;
import br.com.urbantech.meubairro.models.Ocorrencia;
import br.com.urbantech.meubairro.models.Usuario;
import br.com.urbantech.meubairro.utils.Constantes;
import br.com.urbantech.meubairro.utils.ErroUtils;
import br.com.urbantech.meubairro.utils.Resultado;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OcorrenciaRepository {
    private static final int LIMITE_LISTA = 200;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    private DocumentReference ref(String id) {
        return db.collection(Constantes.COL_OCORRENCIAS).document(id);
    }

    /** Registra a ocorrência (e a primeira entrada do histórico). A foto é opcional. */
    public void criar(Ocorrencia o, byte[] foto, Resultado<String> cb) {
        DocumentReference ref = db.collection(Constantes.COL_OCORRENCIAS).document();

        Task<Uri> upload;
        if (foto != null) {
            StorageReference sr = storage.getReference()
                    .child("ocorrencias/" + o.getUsuarioId() + "/" + ref.getId() + ".jpg");
            StorageMetadata md = new StorageMetadata.Builder().setContentType("image/jpeg").build();
            upload = sr.putBytes(foto, md).<Uri>continueWithTask(t -> {
                if (!t.isSuccessful()) throw t.getException();
                return sr.getDownloadUrl();
            });
        } else {
            upload = Tasks.<Uri>forResult(null);
        }

        upload.<Void>continueWithTask(t -> {
            if (!t.isSuccessful()) throw t.getException();
            Uri url = t.getResult();
            if (url != null) o.setImagemUrl(url.toString());
            WriteBatch b = db.batch();
            b.set(ref, o);
            b.set(ref.collection(Constantes.SUB_HISTORICO).document(),
                    new HistoricoStatus(Constantes.STATUS_RECEBIDA, o.getUsuarioId(),
                            o.getUsuarioNome(), "Ocorrência registrada."));
            return b.commit();
        }).addOnSuccessListener(v -> cb.onSucesso(ref.getId()))
          .addOnFailureListener(e -> cb.onErro(ErroUtils.traduzir(e)));
    }

    /** Todas as ocorrências, da mais recente para a mais antiga. */
    public void listarTodas(Resultado<List<Ocorrencia>> cb) {
        db.collection(Constantes.COL_OCORRENCIAS)
                .orderBy("dataCriacao", Query.Direction.DESCENDING)
                .limit(LIMITE_LISTA)
                .get()
                .addOnSuccessListener(qs -> cb.onSucesso(qs.toObjects(Ocorrencia.class)))
                .addOnFailureListener(e -> cb.onErro(ErroUtils.traduzir(e)));
    }

    /** Ocorrências do usuário (ordenadas no cliente, evitando índice composto). */
    public void listarDoUsuario(String uid, Resultado<List<Ocorrencia>> cb) {
        db.collection(Constantes.COL_OCORRENCIAS)
                .whereEqualTo("usuarioId", uid)
                .get()
                .addOnSuccessListener(qs -> {
                    List<Ocorrencia> lista = new ArrayList<>(qs.toObjects(Ocorrencia.class));
                    Collections.sort(lista, (a, b) -> {
                        Date da = a.getDataCriacao(), dbb = b.getDataCriacao();
                        if (da == null && dbb == null) return 0;
                        if (da == null) return -1; // recém-criada (ainda sem data do servidor)
                        if (dbb == null) return 1;
                        return dbb.compareTo(da);
                    });
                    cb.onSucesso(lista);
                })
                .addOnFailureListener(e -> cb.onErro(ErroUtils.traduzir(e)));
    }

    public void buscarPorId(String id, Resultado<Ocorrencia> cb) {
        ref(id).get()
                .addOnSuccessListener(s -> {
                    Ocorrencia o = s.exists() ? s.toObject(Ocorrencia.class) : null;
                    if (o == null) cb.onErro("Ocorrência não encontrada.");
                    else cb.onSucesso(o);
                })
                .addOnFailureListener(e -> cb.onErro(ErroUtils.traduzir(e)));
    }

    public void historico(String id, Resultado<List<HistoricoStatus>> cb) {
        ref(id).collection(Constantes.SUB_HISTORICO)
                .orderBy("data", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(qs -> cb.onSucesso(qs.toObjects(HistoricoStatus.class)))
                .addOnFailureListener(e -> cb.onErro(ErroUtils.traduzir(e)));
    }

    /** Somente administradores (garantido pelas regras do Firestore). */
    public void atualizarStatus(String id, String novoStatus, String observacao, Usuario admin, Resultado<Void> cb) {
        DocumentReference ref = ref(id);
        Map<String, Object> up = new HashMap<>();
        up.put("status", novoStatus);
        up.put("dataAtualizacao", FieldValue.serverTimestamp());

        WriteBatch b = db.batch();
        b.update(ref, up);
        b.set(ref.collection(Constantes.SUB_HISTORICO).document(),
                new HistoricoStatus(novoStatus, admin.getId(), admin.getNome(), observacao));
        b.commit()
                .addOnSuccessListener(v -> cb.onSucesso(null))
                .addOnFailureListener(e -> cb.onErro(ErroUtils.traduzir(e)));
    }

    /** Apaga as ocorrências do usuário (com histórico e foto). Usado na exclusão de conta. */
    public Task<Void> excluirDoUsuario(String uid) {
        return db.collection(Constantes.COL_OCORRENCIAS).whereEqualTo("usuarioId", uid).get()
                .<Void>continueWithTask(t -> {
                    if (!t.isSuccessful()) throw t.getException();
                    final List<DocumentSnapshot> docs = t.getResult().getDocuments();
                    if (docs.isEmpty()) return Tasks.<Void>forResult(null);

                    final List<Task<QuerySnapshot>> historicos = new ArrayList<>();
                    for (DocumentSnapshot d : docs) {
                        historicos.add(d.getReference().collection(Constantes.SUB_HISTORICO).get());
                    }
                    return Tasks.whenAll(historicos).<Void>continueWithTask(t2 -> {
                        if (!t2.isSuccessful()) throw t2.getException();
                        WriteBatch b = db.batch();
                        for (int i = 0; i < docs.size(); i++) {
                            for (DocumentSnapshot h : historicos.get(i).getResult().getDocuments()) {
                                b.delete(h.getReference());
                            }
                            b.delete(docs.get(i).getReference());
                            apagarFoto(docs.get(i).getString("imagemUrl"));
                        }
                        return b.commit();
                    });
                });
    }

    private void apagarFoto(String url) {
        if (url == null || url.isEmpty()) return;
        try {
            storage.getReferenceFromUrl(url).delete();
        } catch (Exception ignorada) {
            // melhor esforço
        }
    }
}
