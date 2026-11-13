package br.com.urbantech.meubairro.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import br.com.urbantech.meubairro.R;
import br.com.urbantech.meubairro.databinding.ItemOcorrenciaBinding;
import br.com.urbantech.meubairro.models.Ocorrencia;
import br.com.urbantech.meubairro.utils.DataUtils;
import br.com.urbantech.meubairro.utils.StatusUtils;
import br.com.urbantech.meubairro.utils.ViewUtils;

import java.util.Objects;

public class OcorrenciaAdapter extends ListAdapter<Ocorrencia, OcorrenciaAdapter.VH> {

    public interface Listener {
        void onClick(Ocorrencia o);

        default void onAlterarStatus(Ocorrencia o) { }
    }

    private static final DiffUtil.ItemCallback<Ocorrencia> DIFF = new DiffUtil.ItemCallback<Ocorrencia>() {
        @Override
        public boolean areItemsTheSame(@NonNull Ocorrencia a, @NonNull Ocorrencia b) {
            return Objects.equals(a.getId(), b.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Ocorrencia a, @NonNull Ocorrencia b) {
            return Objects.equals(a.getStatus(), b.getStatus())
                    && Objects.equals(a.getTitulo(), b.getTitulo())
                    && Objects.equals(a.getImagemUrl(), b.getImagemUrl())
                    && Objects.equals(a.getDataAtualizacao(), b.getDataAtualizacao());
        }
    };

    private final Listener listener;
    private boolean modoAdmin;

    public OcorrenciaAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
    }

    /** No modo administrador cada item exibe o botão "Alterar status". */
    public void setModoAdmin(boolean modoAdmin) {
        this.modoAdmin = modoAdmin;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemOcorrenciaBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Ocorrencia o = getItem(position);
        ItemOcorrenciaBinding b = h.b;
        b.txtTitulo.setText(o.getTitulo());
        b.txtCategoria.setText(o.getCategoria());
        b.txtEndereco.setText(o.getEndereco());
        b.txtData.setText(DataUtils.formatar(o.getDataCriacao()));
        StatusUtils.aplicar(b.txtStatus, o.getStatus());

        if (o.getImagemUrl() != null && !o.getImagemUrl().isEmpty()) {
            Glide.with(b.imgThumb).load(o.getImagemUrl()).centerCrop()
                    .placeholder(R.drawable.ic_image).into(b.imgThumb);
        } else {
            Glide.with(b.imgThumb).clear(b.imgThumb);
            b.imgThumb.setImageResource(R.drawable.ic_image);
        }

        ViewUtils.mostrar(b.btnAlterarStatus, modoAdmin);
        b.btnAlterarStatus.setOnClickListener(v -> listener.onAlterarStatus(o));
        b.getRoot().setOnClickListener(v -> listener.onClick(o));
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemOcorrenciaBinding b;

        VH(ItemOcorrenciaBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}
