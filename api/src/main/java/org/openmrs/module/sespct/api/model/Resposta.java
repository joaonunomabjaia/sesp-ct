package org.openmrs.module.sespct.api.model;

import org.openmrs.BaseOpenmrsData;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "sespct_resposta")
public class Resposta extends BaseOpenmrsData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String respostaId;
    private String pedidoId;
    private String processadoPor;

    private Date timestamp;
    private Date ultimaSincronizacao;
    private String versao;

    // Notificações
    private Date dataNotificacao;
    private Boolean emailEnviado;
    private Boolean smsEnviado;
    private Boolean webhookEntregue;

    // Resposta do Comitê
    private String autorizante;
    @Column(length = 2000)
    private String comentario;
    private String contacto;
    private Date dataAprovacao;
    private Date dataResposta;
    private String email;
    private String esquemaAprovado;
    private String linhaTerapeutica;
    private String nivelAutorizacao;
    private String resposta;

    // Ligação com Pedido (muitos Resposta → 1 Pedido)
    @ManyToOne
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;

    // --- Required Overrides ---
    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    // --- Getters e Setters ---

    public String getRespostaId() {
        return respostaId;
    }

    public void setRespostaId(String respostaId) {
        this.respostaId = respostaId;
    }

    public String getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(String pedidoId) {
        this.pedidoId = pedidoId;
    }

    public String getProcessadoPor() {
        return processadoPor;
    }

    public void setProcessadoPor(String processadoPor) {
        this.processadoPor = processadoPor;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Date getUltimaSincronizacao() {
        return ultimaSincronizacao;
    }

    public void setUltimaSincronizacao(Date ultimaSincronizacao) {
        this.ultimaSincronizacao = ultimaSincronizacao;
    }

    public String getVersao() {
        return versao;
    }

    public void setVersao(String versao) {
        this.versao = versao;
    }

    public Date getDataNotificacao() {
        return dataNotificacao;
    }

    public void setDataNotificacao(Date dataNotificacao) {
        this.dataNotificacao = dataNotificacao;
    }

    public Boolean getEmailEnviado() {
        return emailEnviado;
    }

    public void setEmailEnviado(Boolean emailEnviado) {
        this.emailEnviado = emailEnviado;
    }

    public Boolean getSmsEnviado() {
        return smsEnviado;
    }

    public void setSmsEnviado(Boolean smsEnviado) {
        this.smsEnviado = smsEnviado;
    }

    public Boolean getWebhookEntregue() {
        return webhookEntregue;
    }

    public void setWebhookEntregue(Boolean webhookEntregue) {
        this.webhookEntregue = webhookEntregue;
    }

    public String getAutorizante() {
        return autorizante;
    }

    public void setAutorizante(String autorizante) {
        this.autorizante = autorizante;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public String getContacto() {
        return contacto;
    }

    public void setContacto(String contacto) {
        this.contacto = contacto;
    }

    public Date getDataAprovacao() {
        return dataAprovacao;
    }

    public void setDataAprovacao(Date dataAprovacao) {
        this.dataAprovacao = dataAprovacao;
    }

    public Date getDataResposta() {
        return dataResposta;
    }

    public void setDataResposta(Date dataResposta) {
        this.dataResposta = dataResposta;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEsquemaAprovado() {
        return esquemaAprovado;
    }

    public void setEsquemaAprovado(String esquemaAprovado) {
        this.esquemaAprovado = esquemaAprovado;
    }

    public String getLinhaTerapeutica() {
        return linhaTerapeutica;
    }

    public void setLinhaTerapeutica(String linhaTerapeutica) {
        this.linhaTerapeutica = linhaTerapeutica;
    }

    public String getNivelAutorizacao() {
        return nivelAutorizacao;
    }

    public void setNivelAutorizacao(String nivelAutorizacao) {
        this.nivelAutorizacao = nivelAutorizacao;
    }

    public String getResposta() {
        return resposta;
    }

    public void setResposta(String resposta) {
        this.resposta = resposta;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }
}
