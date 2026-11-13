package br.com.urbantech.meubairro.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import br.com.urbantech.meubairro.R;

public final class StatusUtils {
    private StatusUtils() { }

    public static int cor(Context ctx, String status) {
        int res;
        if (Constantes.STATUS_EM_ANALISE.equals(status)) res = R.color.status_em_analise;
        else if (Constantes.STATUS_EM_ANDAMENTO.equals(status)) res = R.color.status_em_andamento;
        else if (Constantes.STATUS_RESOLVIDA.equals(status)) res = R.color.status_resolvida;
        else if (Constantes.STATUS_NAO_PROCEDENTE.equals(status)) res = R.color.status_nao_procedente;
        else res = R.color.status_recebida;
        return ContextCompat.getColor(ctx, res);
    }

    /** Aplica texto e cor do status em um TextView que usa o estilo Chip.Status. */
    public static void aplicar(TextView tv, String status) {
        tv.setText(status == null ? "" : status);
        ViewCompat.setBackgroundTintList(tv, ColorStateList.valueOf(cor(tv.getContext(), status)));
    }
}
