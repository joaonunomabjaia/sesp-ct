package org.openmrs.module.sespct.api.impl;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.sespct.api.service.RespostaService;
import org.openmrs.module.sespct.api.dao.RespostaDao;
import org.openmrs.module.sespct.api.model.Resposta;
import org.openmrs.module.sespct.config.CTConfig;
import org.openmrs.module.sespct.ct.CtClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class  RespostaServiceImpl extends BaseOpenmrsService implements RespostaService {

    private static final Log log = LogFactory.getLog(RespostaServiceImpl.class);

    @Autowired
    private RespostaDao respostaDao;

    @Autowired
    private CtClient ctClient;

    @Autowired
    private CTConfig cfg;

    public void setRespostaDao(RespostaDao respostaDao) {
        this.respostaDao = respostaDao;
    }

    public void initializeModule() {
        log.info("Initializing Resposta Module...");
        List<Resposta> existing = respostaDao.getAllRespostas();
        if (existing.isEmpty()) {
            createDummyData();
        }
        else {
            log.info("Found " + existing.size() + " existing respostas. Skipping dummy data creation.");
        }
    }

    @Override
    public Resposta saveResposta(Resposta resposta) {
        return respostaDao.saveResposta(resposta);
    }

    @Override
    @Transactional(readOnly = true)
    public Resposta getRespostaById(Integer id) {
        return respostaDao.getRespostaById(id);
    }

    @Override
    public Resposta getRespostaByExternalId(String respostaId) {
//        return respostaDao.getRespostaByExternalId(respostaId);
        return null;
    }

    @Override
    public List<Resposta> getRespostasByPedidoId(String pedidoId) {
        try {
            Integer id = Integer.parseInt(pedidoId);
            return respostaDao.getRespostasByPedidoId(id);
        } catch (NumberFormatException e) {
            log.warn("PedidoId inválido: " + pedidoId, e);
            return List.of();
        }
    }

    @Transactional(readOnly = true)
    public List<Resposta> getAllRespostas() {
        return respostaDao.getAllRespostas();
    }

    @Override
    public void deleteResposta(Resposta resposta) {
        respostaDao.deleteResposta(resposta);
    }

    @Override
    public void createDummyData() {
        try {
            log.info("Creating dummy Resposta data...");
            for (int i = 1; i <= 10; i++) {
                Resposta resp = new Resposta();
                resp.setUuid(UUID.randomUUID().toString());
                resp.setDateCreated(new Date());
                resp.setCreator(Context.getAuthenticatedUser());

                resp.setPedidoId(String.valueOf(1000 + i));
                resp.setRespostaId(String.valueOf(2000 + i));
                resp.setProcessadoPor("Comite Provincial " + (i % 2 == 0 ? "Nampula" : "Tete"));
                resp.setVersao("1.0");
                resp.setTimestamp(new Date());
                resp.setUltimaSincronizacao(new Date());

                // Notificações
                resp.setDataNotificacao(new Date());
                resp.setEmailEnviado(i % 2 == 0);
                resp.setSmsEnviado(i % 3 == 0);
                resp.setWebhookEntregue(true);

                // Resposta Comitê
                resp.setAutorizante("Dr. Exemplo " + i);
                resp.setComentario("Comentário dummy " + i);
                resp.setContacto("+25884" + (1234560 + i));
                resp.setDataAprovacao(new Date());
                resp.setDataResposta(new Date());
                resp.setEmail("exemplo" + i + "@hpn.gov.mz");
                resp.setEsquemaAprovado("ABC+3TC+DTG");
                resp.setLinhaTerapeutica(i % 2 == 0 ? "1 Linha Alternativa" : "2 Linha");
                resp.setNivelAutorizacao("provincial");
                resp.setResposta(i % 2 == 0 ? "Aprovado" : "Pendente");

                respostaDao.saveResposta(resp);
            }
            log.info("Dummy Resposta data created successfully");
        } catch (Exception e) {
            log.error("Error creating dummy Resposta data", e);
        }
    }


    private Resposta mapJsonToResposta(JsonNode node) {
        Resposta resp = new Resposta();
        resp.setUuid(UUID.randomUUID().toString());
        resp.setDateCreated(new Date());
        resp.setCreator(Context.getAuthenticatedUser());

        // --- Metadados ---
        JsonNode meta = node.path("metadados");
        resp.setPedidoId(meta.path("pedidoId").asInt(0));
        resp.setRespostaId(meta.path("respostaId").asText(null));
        resp.setProcessadoPor(meta.path("processadoPor").asText(null));
        resp.setVersao(meta.path("versao").asText(null));

        String ts = meta.path("timestamp").asText(null);
        if (ts != null) {
            try {
                resp.setTimestamp(javax.xml.bind.DatatypeConverter.parseDateTime(ts).getTime());
            } catch (Exception e) {
                log.warn("Erro ao parsear timestamp: " + ts, e);
            }
        }

        String ultimaSync = meta.path("ultimaSincronizacao").asText(null);
        if (ultimaSync != null) {
            try {
                resp.setUltimaSincronizacao(javax.xml.bind.DatatypeConverter.parseDateTime(ultimaSync).getTime());
            } catch (Exception e) {
                log.warn("Erro ao parsear ultimaSincronizacao: " + ultimaSync, e);
            }
        }

        // --- Notificações ---
        JsonNode notif = node.path("notificacoes");
        if (!notif.isMissingNode()) {
            String dtNotif = notif.path("dataNotificacao").asText(null);
            if (dtNotif != null) {
                try {
                    resp.setDataNotificacao(javax.xml.bind.DatatypeConverter.parseDateTime(dtNotif).getTime());
                } catch (Exception e) {
                    log.warn("Erro ao parsear dataNotificacao: " + dtNotif, e);
                }
            }
            resp.setEmailEnviado(notif.path("emailEnviado").asBoolean(false));
            resp.setSmsEnviado(notif.path("smsEnviado").asBoolean(false));
            resp.setWebhookEntregue(notif.path("webhookEntregue").asBoolean(false));
        }

        // --- Resposta do Comitê ---
        JsonNode rc = node.path("respostaComite");
        if (!rc.isMissingNode()) {
            resp.setAutorizante(rc.path("autorizante").asText(null));
            resp.setComentario(rc.path("comentario").asText(null));
            resp.setContacto(rc.path("contacto").asText(null));
            resp.setEmail(rc.path("email").asText(null));
            resp.setEsquemaAprovado(rc.path("esquemaAprovado").asText(null));
            resp.setLinhaTerapeutica(rc.path("linhaTerapeutica").asText(null));
            resp.setNivelAutorizacao(rc.path("nivelAutorizacao").asText(null));
            resp.setEstado(rc.path("resposta").asText("Pendente"));

            String dtAprov = rc.path("dataAprovacao").asText(null);
            if (dtAprov != null) {
                try {
                    resp.setDataAprovacao(javax.xml.bind.DatatypeConverter.parseDateTime(dtAprov).getTime());
                } catch (Exception e) {
                    log.warn("Erro ao parsear dataAprovacao: " + dtAprov, e);
                }
            }

            String dtResp = rc.path("dataResposta").asText(null);
            if (dtResp != null) {
                try {
                    resp.setDataResposta(javax.xml.bind.DatatypeConverter.parseDateTime(dtResp).getTime());
                } catch (Exception e) {
                    log.warn("Erro ao parsear dataResposta: " + dtResp, e);
                }
            }
        }

        return resp;
    }


    @Override
    public Resposta saveFromJson(JsonNode node) {
        if (node == null || node.isEmpty()) return null;
        return respostaDao.saveOrFromJson(mapJsonToResposta(node));
    }

    @Override
    public void fetchAndCreateFromCtAsync(String pedidoId) {
        try {
            JsonNode body = ctClient.getRespostasDoPedido(pedidoId);
            if (body != null && body.has("data")) {
                for (JsonNode item : body.path("data")) {
                    JsonNode dados = item.path("dadosResposta");
                    if (!dados.isMissingNode()) {
                        Resposta resp = mapJsonToResposta(dados);
                        respostaDao.saveOrFromJson(resp);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Erro ao buscar Resposta do CT", e);
        }
    }
}
