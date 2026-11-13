package br.com.urbantech.meubairro.utils;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.StorageException;

/** Traduz exceções do Firebase em mensagens claras para o usuário. */
public final class ErroUtils {
    private ErroUtils() { }

    public static String traduzir(Exception e) {
        if (e instanceof FirebaseNetworkException) {
            return "Sem conexão com a internet. Verifique sua rede e tente novamente.";
        }
        if (e instanceof FirebaseTooManyRequestsException) {
            return "Muitas tentativas. Aguarde um instante e tente novamente.";
        }
        if (e instanceof FirebaseAuthException) {
            String code = ((FirebaseAuthException) e).getErrorCode();
            switch (code) {
                case "ERROR_INVALID_EMAIL":
                    return "E-mail inválido.";
                case "ERROR_WRONG_PASSWORD":
                case "ERROR_INVALID_CREDENTIAL":
                case "ERROR_USER_NOT_FOUND":
                    return "E-mail ou senha incorretos.";
                case "ERROR_EMAIL_ALREADY_IN_USE":
                    return "Já existe uma conta com este e-mail.";
                case "ERROR_WEAK_PASSWORD":
                    return "Senha fraca. Use ao menos 6 caracteres.";
                case "ERROR_USER_DISABLED":
                    return "Esta conta foi desativada.";
                case "ERROR_REQUIRES_RECENT_LOGIN":
                    return "Por segurança, entre novamente e repita a operação.";
                default:
                    return "Não foi possível concluir a autenticação.";
            }
        }
        if (e instanceof FirebaseFirestoreException) {
            switch (((FirebaseFirestoreException) e).getCode()) {
                case PERMISSION_DENIED:
                    return "Você não tem permissão para realizar esta ação.";
                case UNAVAILABLE:
                    return "Serviço indisponível ou sem conexão. Tente novamente.";
                default:
                    return "Erro ao acessar os dados. Tente novamente.";
            }
        }
        if (e instanceof StorageException) {
            return "Falha ao enviar a foto. Tente novamente ou envie sem foto.";
        }
        return "Ocorreu um erro inesperado. Tente novamente.";
    }
}
