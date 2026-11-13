package br.com.urbantech.meubairro.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import br.com.urbantech.meubairro.databinding.ActivityCadastroBinding;
import br.com.urbantech.meubairro.repositories.AuthRepository;
import br.com.urbantech.meubairro.utils.NetworkUtils;
import br.com.urbantech.meubairro.utils.Resultado;
import br.com.urbantech.meubairro.utils.Validador;
import br.com.urbantech.meubairro.utils.ViewUtils;

public class CadastroActivity extends AppCompatActivity {
    private ActivityCadastroBinding b;
    private final AuthRepository auth = new AuthRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityCadastroBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        b.toolbar.setNavigationOnClickListener(v -> finish());
        b.btnJaTenhoConta.setOnClickListener(v -> finish());
        b.btnCadastrar.setOnClickListener(v -> cadastrar());
    }

    private void cadastrar() {
        b.tilNome.setError(null);
        b.tilEmail.setError(null);
        b.tilSenha.setError(null);
        b.tilConfirma.setError(null);

        String nome = ViewUtils.texto(b.editNome);
        String email = ViewUtils.texto(b.editEmail);
        String senha = b.editSenha.getText() == null ? "" : b.editSenha.getText().toString();
        String confirma = b.editConfirma.getText() == null ? "" : b.editConfirma.getText().toString();

        boolean ok = true;
        if (nome.length() < 2) {
            b.tilNome.setError("Informe seu nome");
            ok = false;
        }
        if (!Validador.emailValido(email)) {
            b.tilEmail.setError("Informe um e-mail válido");
            ok = false;
        }
        if (!Validador.senhaValida(senha)) {
            b.tilSenha.setError("A senha deve ter ao menos " + Validador.SENHA_MIN + " caracteres");
            ok = false;
        }
        if (!senha.equals(confirma)) {
            b.tilConfirma.setError("As senhas não conferem");
            ok = false;
        }
        if (!ok) return;
        if (!NetworkUtils.temConexao(this)) {
            Toast.makeText(this, "Sem conexão com a internet.", Toast.LENGTH_LONG).show();
            return;
        }

        carregando(true);
        auth.cadastrar(nome, email, senha, Resultado.de(
                u -> {
                    Toast.makeText(this, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(this, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                },
                msg -> {
                    carregando(false);
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                }));
    }

    private void carregando(boolean sim) {
        ViewUtils.mostrar(b.progress, sim);
        b.btnCadastrar.setEnabled(!sim);
        b.btnJaTenhoConta.setEnabled(!sim);
    }
}
