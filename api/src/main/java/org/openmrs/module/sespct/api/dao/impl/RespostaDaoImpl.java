package org.openmrs.module.sespct.api.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.openmrs.api.db.hibernate.DbSession;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.sespct.api.dao.RespostaDao;
import org.openmrs.module.sespct.api.model.Resposta;

import java.util.List;

public class RespostaDaoImpl implements RespostaDao {

    protected final Log log = LogFactory.getLog(this.getClass());

    private DbSessionFactory dbSessionFactory;

    public void setDbSessionFactory(DbSessionFactory dbSessionFactory) {
        this.dbSessionFactory = dbSessionFactory;
    }

    public DbSessionFactory getDbSessionFactory() {
        return dbSessionFactory;
    }

    private DbSession getCurrentSession() {
        return dbSessionFactory.getCurrentSession();
    }

    @Override
    public Resposta saveResposta(Resposta resposta) {
        this.getCurrentSession().saveOrUpdate(resposta);
        return resposta;
    }

    @Override
    public Resposta getRespostaById(Integer id) {
        return (Resposta) this.getCurrentSession().get(Resposta.class, id);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Resposta> getAllRespostas() {
        final String hql = "FROM Resposta WHERE voided = 0 ORDER BY dateCreated DESC";
        final Query query = this.getCurrentSession().createQuery(hql);
        return query.list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Resposta> getRespostasByPedidoId(Integer pedidoId) {
        final String hql = "FROM Resposta WHERE pedido.id = :pedidoId AND voided = 0 ORDER BY dateCreated DESC";
        final Query query = this.getCurrentSession().createQuery(hql)
                .setParameter("pedidoId", pedidoId);
        return query.list();
    }

    @Override
    public void deleteResposta(Resposta resposta) {
        // Não deletar fisicamente, apenas marcar como voided
        resposta.setVoided(true);
        this.getCurrentSession().saveOrUpdate(resposta);
    }

    @Override
    public Resposta saveOrFromJson(Resposta resposta) {
        try {
            return saveResposta(resposta);
        } catch (Exception e) {
            log.error("Erro ao salvar Resposta a partir do JsonNode", e);
        }
        return null;
    }
}
