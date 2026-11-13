package br.com.urbantech.meubairro.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import br.com.urbantech.meubairro.R;

public class SplashActivity extends AppCompatActivity {
    private static final long DURACAO_MS = 1500;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        handler.postDelayed(this::avancar, DURACAO_MS);
    }

    private void avancar() {
        boolean logado = FirebaseAuth.getInstance().getCurrentUser() != null;
        startActivity(new Intent(this, logado ? MainActivity.class : LoginActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
