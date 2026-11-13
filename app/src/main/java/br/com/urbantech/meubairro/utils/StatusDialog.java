package br.com.urbantech.meubairro.utils;

import android.content.Context;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import br.com.urbantech.meubairro.models.Ocorrencia;
import br.com.urbantech.meubairro.models.Usuario;
import br.com.urbantech.meubairro.repositories.OcorrenciaRepository;

/** Diálogo usado pelo administrador para alterar o status de uma ocorrência. */
public final class StatusDialog {
    private StatusDialog() { }

    public static void mostrar(Context ctx, Ocorrencia o, Usuario admin,
                               OcorrenciaRepository repo, Runnable aoConcluir) {
        if (admin == null || !admin.isAdmin()) {
            Toast.makeText(ctx, "Apenas administradores podem alterar o status.", Toast.LENGTH_LONG).show();
            return;
        }
        int atual = 0;
        for (int i = 0; i < Constantes.STATUS.length; i++) {
            if (Constantes.STATUS[i].equals(o.getStatus())) atual = i;
        }
        final int[] sel = {atual};
        new MaterialAlertDialogBuilder(ctx)
                .setTitle("Atualizar status")
                .setSingleChoiceItems(Constantes.STATUS, atual, (d, which) -> sel[0] = which)
                .setPositiveButton("Salvar", (d, w) -> {
                    String novo = Constantes.STATUS[sel[0]];
                    if (novo.equals(o.getStatus())) {
                        Toast.makeText(ctx, "O status não foi alterado.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    repo.atualizarStatus(o.getId(), novo, "Status atualizado para \"" + novo + "\".", admin,
                            Resultado.<Void>de(v -> {
                                Toast.makeText(ctx, "Status atualizado.", Toast.LENGTH_SHORT).show();
                                if (aoConcluir != null) aoConcluir.run();
                            }, msg -> Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show()));
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
