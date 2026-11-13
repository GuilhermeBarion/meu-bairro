package br.com.urbantech.meubairro.utils;

import java.util.function.Consumer;

/** Callback simples usado pelos repositórios. */
public interface Resultado<T> {
    void onSucesso(T valor);

    void onErro(String mensagem);

    static <T> Resultado<T> de(Consumer<T> sucesso, Consumer<String> erro) {
        return new Resultado<T>() {
            @Override
            public void onSucesso(T valor) { sucesso.accept(valor); }

            @Override
            public void onErro(String mensagem) { erro.accept(mensagem); }
        };
    }
}
