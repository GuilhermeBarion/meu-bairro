package br.com.urbantech.meubairro.utils;

import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import br.com.urbantech.meubairro.databinding.LayoutDialogInputBinding;

import java.util.function.Consumer;

public final class DialogUtils {
    private DialogUtils() { }

    /** Diálogo com um campo de texto obrigatório. Só fecha quando o campo está preenchido. */
    public static void pedirTexto(Context ctx, String titulo, String hint, int inputType,
                                  String inicial, Consumer<String> aoConfirmar) {
        LayoutDialogInputBinding b = LayoutDialogInputBinding.inflate(LayoutInflater.from(ctx));
        b.tilInput.setHint(hint);
        b.editInput.setInputType(inputType);
        if ((inputType & InputType.TYPE_MASK_VARIATION) == InputType.TYPE_TEXT_VARIATION_PASSWORD) {
            b.tilInput.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
        }
        if (inicial != null) b.editInput.setText(inicial);

        AlertDialog d = new MaterialAlertDialogBuilder(ctx)
                .setTitle(titulo)
                .setView(b.getRoot())
                .setPositiveButton("Confirmar", null)
                .setNegativeButton("Cancelar", null)
                .create();
        d.setOnShowListener(x -> d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String t = ViewUtils.texto(b.editInput);
            if (t.isEmpty()) {
                b.tilInput.setError("Campo obrigatório");
                return;
            }
            d.dismiss();
            aoConfirmar.accept(t);
        }));
        d.show();
    }
}
