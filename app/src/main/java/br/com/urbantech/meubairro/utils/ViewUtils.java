package br.com.urbantech.meubairro.utils;

import android.view.View;
import android.widget.TextView;

public final class ViewUtils {
    private ViewUtils() { }

    /** Texto de um campo, sem espaços nas pontas; nunca nulo. */
    public static String texto(TextView tv) {
        CharSequence cs = tv.getText();
        return cs == null ? "" : cs.toString().trim();
    }

    public static void mostrar(View v, boolean visivel) {
        v.setVisibility(visivel ? View.VISIBLE : View.GONE);
    }
}
