package br.com.urbantech.meubairro.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/** Registro de atualização de status (subcoleção "ocorrencias/{id}/historico"). */
public class HistoricoStatus {
    private String status;
    private String autorId;
    private String autorNome;
    private String observacao;
    @ServerTimestamp
    private Date data;

    public HistoricoStatus() { }

    public HistoricoStatus(String status, String autorId, String autorNome, String observacao) {
        this.status = status;
        this.autorId = autorId;
        this.autorNome = autorNome;
        this.observacao = observacao;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAutorId() { return autorId; }
    public void setAutorId(String autorId) { this.autorId = autorId; }
    public String getAutorNome() { return autorNome; }
    public void setAutorNome(String autorNome) { this.autorNome = autorNome; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public Date getData() { return data; }
    public void setData(Date data) { this.data = data; }
}
