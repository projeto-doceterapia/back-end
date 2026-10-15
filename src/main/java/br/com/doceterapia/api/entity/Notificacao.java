package br.com.doceterapia.api.entity;

import br.com.doceterapia.api.enums.StatusEnvioNotificacao;
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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "notificacao",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_notificacao_chave_unicidade",
        columnNames = "chave_unicidade"
    )
)
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

    @Column(length = 150)
    private String chaveUnicidade;

    private LocalDateTime janelaInicio;

    private LocalDateTime janelaFim;

    @Column(length = 80)
    private String templateKey;

    @Column(columnDefinition = "TEXT")
    private String parametros;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private StatusEnvioNotificacao statusEnvio = StatusEnvioNotificacao.NAO_ENVIAR;

    private LocalDateTime enviadoEm;

    @Column(length = 120)
    private String canalMessageId;

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

    public String getChaveUnicidade() {
        return chaveUnicidade;
    }

    public void setChaveUnicidade(String chaveUnicidade) {
        this.chaveUnicidade = chaveUnicidade;
    }

    public LocalDateTime getJanelaInicio() {
        return janelaInicio;
    }

    public void setJanelaInicio(LocalDateTime janelaInicio) {
        this.janelaInicio = janelaInicio;
    }

    public LocalDateTime getJanelaFim() {
        return janelaFim;
    }

    public void setJanelaFim(LocalDateTime janelaFim) {
        this.janelaFim = janelaFim;
    }

    public String getTemplateKey() {
        return templateKey;
    }

    public void setTemplateKey(String templateKey) {
        this.templateKey = templateKey;
    }

    public String getParametros() {
        return parametros;
    }

    public void setParametros(String parametros) {
        this.parametros = parametros;
    }

    public StatusEnvioNotificacao getStatusEnvio() {
        return statusEnvio;
    }

    public void setStatusEnvio(StatusEnvioNotificacao statusEnvio) {
        this.statusEnvio = statusEnvio;
    }

    public LocalDateTime getEnviadoEm() {
        return enviadoEm;
    }

    public void setEnviadoEm(LocalDateTime enviadoEm) {
        this.enviadoEm = enviadoEm;
    }

    public String getCanalMessageId() {
        return canalMessageId;
    }

    public void setCanalMessageId(String canalMessageId) {
        this.canalMessageId = canalMessageId;
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
