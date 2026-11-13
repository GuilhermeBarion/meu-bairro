package br.com.urbantech.meubairro.utils;

import br.com.urbantech.meubairro.models.Ocorrencia;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class FiltroUtils {
    private FiltroUtils() { }

    private static String normalizar(String s) {
        if (s == null) return "";
        return Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }

    private static String nz(String s) { return s == null ? "" : s; }

    /** Filtra por texto (título, descrição, endereço, categoria), categoria e status. null = sem filtro. */
    public static List<Ocorrencia> filtrar(List<Ocorrencia> base, String busca, String categoria, String status) {
        String q = normalizar(busca);
        List<Ocorrencia> r = new ArrayList<>();
        if (base == null) return r;
        for (Ocorrencia o : base) {
            if (categoria != null && !categoria.equals(o.getCategoria())) continue;
            if (status != null && !status.equals(o.getStatus())) continue;
            if (!q.isEmpty()) {
                String alvo = normalizar(nz(o.getTitulo()) + " " + nz(o.getDescricao()) + " "
                        + nz(o.getEndereco()) + " " + nz(o.getCategoria()));
                if (!alvo.contains(q)) continue;
            }
            r.add(o);
        }
        return r;
    }
}
