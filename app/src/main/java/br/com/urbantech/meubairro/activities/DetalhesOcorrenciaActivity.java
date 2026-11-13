package br.com.urbantech.meubairro.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import br.com.urbantech.meubairro.databinding.ActivityDetalhesOcorrenciaBinding;
import br.com.urbantech.meubairro.databinding.ItemHistoricoBinding;
import br.com.urbantech.meubairro.models.HistoricoStatus;
import br.com.urbantech.meubairro.models.Ocorrencia;
import br.com.urbantech.meubairro.models.Usuario;
import br.com.urbantech.meubairro.repositories.OcorrenciaRepository;
import br.com.urbantech.meubairro.repositories.UsuarioRepository;
import br.com.urbantech.meubairro.utils.Constantes;
import br.com.urbantech.meubairro.utils.DataUtils;
import br.com.urbantech.meubairro.utils.Resultado;
import br.com.urbantech.meubairro.utils.StatusDialog;
import br.com.urbantech.meubairro.utils.StatusUtils;
import br.com.urbantech.meubairro.utils.ViewUtils;

import java.util.List;
import java.util.Locale;

public class DetalhesOcorrenciaActivity extends AppCompatActivity {
    private ActivityDetalhesOcorrenciaBinding b;
    private final OcorrenciaRepository repo = new OcorrenciaRepository();
    private final UsuarioRepository usuarios = new UsuarioRepository();
    private String ocorrenciaId;
    private Ocorrencia atual;
    private Usuario usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityDetalhesOcorrenciaBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        b.toolbar.setNavigationOnClickListener(v -> finish());

        ocorrenciaId = getIntent().getStringExtra(Constantes.EXTRA_OCORRENCIA_ID);
        if (ocorrenciaId == null) {
            Toast.makeText(this, "Ocorrência inválida.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        b.btnTentar.setOnClickListener(v -> carregar());
        b.btnAtualizarStatus.setOnClickListener(v ->
                StatusDialog.mostrar(this, atual, usuario, repo, this::carregar));

        FirebaseUser fu = FirebaseAuth.getInstance().getCurrentUser();
        if (fu != null) {
            usuarios.carregar(fu, Resultado.de(u -> {
                usuario = u;
                ViewUtils.mostrar(b.btnAtualizarStatus, u.isAdmin() && atual != null);
            }, msg -> { /* sem perfil: apenas não exibe controles de administrador */ }));
        }
        carregar();
    }

    private void carregar() {
        ViewUtils.mostrar(b.progress, true);
        ViewUtils.mostrar(b.layoutErro, false);
        repo.buscarPorId(ocorrenciaId, Resultado.de(o -> {
            atual = o;
            exibir(o);
            carregarHistorico();
        }, msg -> {
            ViewUtils.mostrar(b.progress, false);
            ViewUtils.mostrar(b.conteudo, false);
            b.txtErro.setText(msg);
            ViewUtils.mostrar(b.layoutErro, true);
        }));
    }

    private void exibir(Ocorrencia o) {
        ViewUtils.mostrar(b.progress, false);
        ViewUtils.mostrar(b.conteudo, true);

        b.txtTitulo.setText(o.getTitulo());
        b.txtCategoria.setText(o.getCategoria());
        StatusUtils.aplicar(b.txtStatus, o.getStatus());
        ViewUtils.mostrar(b.txtNaoProcedente, Constantes.STATUS_NAO_PROCEDENTE.equals(o.getStatus()));
        b.txtDescricao.setText(o.getDescricao());
        b.txtEndereco.setText("Endereço: " + o.getEndereco());
        b.txtData.setText("Registrada em: " + DataUtils.formatar(o.getDataCriacao()));
        b.txtAtualizacao.setText("Última atualização: " + DataUtils.formatar(o.getDataAtualizacao()));
        b.txtAutor.setText("Registrada por: " + (o.getUsuarioNome() == null ? "—" : o.getUsuarioNome()));

        boolean temFoto = o.getImagemUrl() != null && !o.getImagemUrl().isEmpty();
        ViewUtils.mostrar(b.imgFoto, temFoto);
        if (temFoto) Glide.with(this).load(o.getImagemUrl()).centerCrop().into(b.imgFoto);

        boolean temCoord = o.getLatitude() != null && o.getLongitude() != null;
        ViewUtils.mostrar(b.btnMapa, temCoord);
        if (temCoord) b.btnMapa.setOnClickListener(v -> abrirMapa(o));

        ViewUtils.mostrar(b.btnAtualizarStatus, usuario != null && usuario.isAdmin());
    }

    private void abrirMapa(Ocorrencia o) {
        String geo = String.format(Locale.US, "geo:%f,%f?q=%f,%f", o.getLatitude(), o.getLongitude(),
                o.getLatitude(), o.getLongitude());
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(geo)));
        } catch (Exception e) {
            Toast.makeText(this, "Nenhum aplicativo de mapas disponível.", Toast.LENGTH_LONG).show();
        }
    }

    private void carregarHistorico() {
        repo.historico(ocorrenciaId, Resultado.de(this::exibirHistorico, msg -> {
            b.containerHistorico.removeAllViews();
            Toast.makeText(this, "Não foi possível carregar o histórico.", Toast.LENGTH_SHORT).show();
        }));
    }

    private void exibirHistorico(List<HistoricoStatus> lista) {
        b.containerHistorico.removeAllViews();
        for (HistoricoStatus h : lista) {
            ItemHistoricoBinding ib = ItemHistoricoBinding.inflate(
                    LayoutInflater.from(this), b.containerHistorico, false);
            StatusUtils.aplicar(ib.txtHistStatus, h.getStatus());
            String autor = h.getAutorNome() == null ? "" : " · " + h.getAutorNome();
            String obs = h.getObservacao() == null ? "" : "\n" + h.getObservacao();
            ib.txtHistInfo.setText(DataUtils.formatar(h.getData()) + autor + obs);
            b.containerHistorico.addView(ib.getRoot());
        }
    }
}
