package br.com.urbantech.meubairro.utils;

public final class Constantes {
    private Constantes() { }

    public static final String COL_USUARIOS = "usuarios";
    public static final String COL_OCORRENCIAS = "ocorrencias";
    public static final String SUB_HISTORICO = "historico";

    public static final String TIPO_USUARIO = "usuario";
    public static final String TIPO_ADMIN = "administrador";

    public static final String STATUS_RECEBIDA = "Recebida";
    public static final String STATUS_EM_ANALISE = "Em análise";
    public static final String STATUS_EM_ANDAMENTO = "Em andamento";
    public static final String STATUS_RESOLVIDA = "Resolvida";
    public static final String STATUS_NAO_PROCEDENTE = "Não procedente";

    public static final String[] STATUS = {
            STATUS_RECEBIDA, STATUS_EM_ANALISE, STATUS_EM_ANDAMENTO,
            STATUS_RESOLVIDA, STATUS_NAO_PROCEDENTE
    };

    public static final String[] CATEGORIAS = {
            "Ruas e pavimentação",
            "Calçadas e acessibilidade",
            "Iluminação pública",
            "Limpeza urbana",
            "Praças e espaços públicos",
            "Drenagem urbana"
    };

    public static final String EXTRA_OCORRENCIA_ID = "ocorrencia_id";
}
