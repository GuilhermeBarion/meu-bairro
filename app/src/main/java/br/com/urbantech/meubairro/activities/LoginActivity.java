package br.com.urbantech.meubairro.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import br.com.urbantech.meubairro.databinding.ActivityLoginBinding;
import br.com.urbantech.meubairro.repositories.AuthRepository;
import br.com.urbantech.meubairro.utils.DialogUtils;
import br.com.urbantech.meubairro.utils.NetworkUtils;
import br.com.urbantech.meubairro.utils.Resultado;
import br.com.urbantech.meubairro.utils.Validador;
import br.com.urbantech.meubairro.utils.ViewUtils;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding b;
    private final AuthRepository auth = new AuthRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        b.btnEntrar.setOnClickListener(v -> entrar());
        b.btnCriarConta.setOnClickListener(v -> startActivity(new Intent(this, CadastroActivity.class)));
        b.btnEsqueciSenha.setOnClickListener(v -> DialogUtils.pedirTexto(
                this, "Recuperar senha", "E-mail cadastrado",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
                ViewUtils.texto(b.editEmail), this::recuperarSenha));
    }

    private void entrar() {
        b.tilEmail.setError(null);
        b.tilSenha.setError(null);
        String email = ViewUtils.texto(b.editEmail);
        String senha = b.editSenha.getText() == null ? "" : b.editSenha.getText().toString();

        boolean ok = true;
        if (!Validador.emailValido(email)) {
            b.tilEmail.setError("Informe um e-mail válido");
            ok = false;
        }
        if (senha.isEmpty()) {
            b.tilSenha.setError("Informe a senha");
            ok = false;
        }
        if (!ok) return;
        if (!NetworkUtils.temConexao(this)) {
            Toast.makeText(this, "Sem conexão com a internet.", Toast.LENGTH_LONG).show();
            return;
        }

        carregando(true);
        auth.login(email, senha, Resultado.de(
                u -> {
                    Intent i = new Intent(this, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                },
                msg -> {
                    carregando(false);
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                }));
    }

    private void recuperarSenha(String email) {
        if (!Validador.emailValido(email)) {
            Toast.makeText(this, "Informe um e-mail válido.", Toast.LENGTH_LONG).show();
            return;
        }
        if (!NetworkUtils.temConexao(this)) {
            Toast.makeText(this, "Sem conexão com a internet.", Toast.LENGTH_LONG).show();
            return;
        }
        auth.recuperarSenha(email, Resultado.<Void>de(
                v -> Toast.makeText(this,
                        "Se o e-mail estiver cadastrado, você receberá um link para redefinir a senha.",
                        Toast.LENGTH_LONG).show(),
                msg -> Toast.makeText(this, msg, Toast.LENGTH_LONG).show()));
    }

    private void carregando(boolean sim) {
        ViewUtils.mostrar(b.progress, sim);
        b.btnEntrar.setEnabled(!sim);
        b.btnCriarConta.setEnabled(!sim);
        b.btnEsqueciSenha.setEnabled(!sim);
    }
}
