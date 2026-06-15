package br.com.doceterapia.api.entity;

import br.com.doceterapia.api.enums.TipoNotificacao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

@Entity
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idNotificacao;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private TipoNotificacao tipo;

    @Column(length = 150, nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String mensagem;

    @Column(nullable = false)
    private LocalDateTime dataCriacao;

    @Column(nullable = false)
    private Boolean lida = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fk_pedido")
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fk_insumo")
    private Insumo insumo;

    public Integer getIdNotificacao() {
        return idNotificacao;
    }

    public void setIdNotificacao(Integer idNotificacao) {
        this.idNotificacao = idNotificacao;
    }

    public TipoNotificacao getTipo() {
        return tipo;
    }

    public void setTipo(TipoNotificacao tipo) {
        this.tipo = tipo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public Boolean getLida() {
        return lida;
    }

    public void setLida(Boolean lida) {
        this.lida = lida;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public Insumo getInsumo() {
        return insumo;
    }

    public void setInsumo(Insumo insumo) {
        this.insumo = insumo;
    }
}
