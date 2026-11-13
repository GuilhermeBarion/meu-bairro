package br.com.urbantech.meubairro.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;

import br.com.urbantech.meubairro.utils.Constantes;

import java.util.Date;

/** Documento da coleção "usuarios" (id = uid do Firebase Authentication). */
public class Usuario {
    @DocumentId
    private String id;
    private String nome;
    private String email;
    private String foto; // opcional
    private String tipo = Constantes.TIPO_USUARIO;
    @ServerTimestamp
    private Date dataCadastro;

    public Usuario() { }

    public Usuario(String id, String nome, String email) {
        this.id = id;
        this.nome = nome;
        this.email = email;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public Date getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(Date dataCadastro) { this.dataCadastro = dataCadastro; }

    @Exclude
    public boolean isAdmin() {
        return Constantes.TIPO_ADMIN.equals(tipo);
    }
}
