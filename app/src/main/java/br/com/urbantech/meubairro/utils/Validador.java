package br.com.urbantech.meubairro.utils;

import android.util.Patterns;

public final class Validador {
    private Validador() { }

    public static final int SENHA_MIN = 6;

    public static boolean emailValido(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean senhaValida(String senha) {
        return senha != null && senha.length() >= SENHA_MIN;
    }
}
