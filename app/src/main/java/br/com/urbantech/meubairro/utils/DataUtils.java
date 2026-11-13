package br.com.urbantech.meubairro.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class DataUtils {
    private DataUtils() { }

    public static String formatar(Date data) {
        if (data == null) return "—";
        return new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("pt", "BR")).format(data);
    }
}
