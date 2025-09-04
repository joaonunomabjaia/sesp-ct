package org.openmrs.module.sespct.api.dao.impl;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.openmrs.api.db.hibernate.DbSession;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.sespct.api.dao.PedidoDao;
import org.openmrs.module.sespct.api.model.Pedido;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class PedidoDaoImpl implements PedidoDao {
	
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
	public Pedido savePedido(Pedido pedido) {
		this.getCurrentSession().saveOrUpdate(pedido);
		return pedido;
	}
	
	@Override
	public Pedido getPedidoById(Integer id) {
		return (Pedido) this.getCurrentSession().get(Pedido.class, id);
	}
	
	@Override
	public Pedido getPedidoByExternalId(String externalId) {
		final String hql = "FROM Pedido WHERE pedidoExternalId = :externalId AND voided = 0";
		final Query query = this.getCurrentSession().createQuery(hql).setParameter("externalId", externalId);
		
		@SuppressWarnings("unchecked")
		List<Pedido> results = query.list();
		return results.isEmpty() ? null : results.get(0);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<Pedido> getAllPedidos() {
		final String hql = "FROM Pedido WHERE voided = 0 ORDER BY dateCreated DESC";
		final Query query = this.getCurrentSession().createQuery(hql);
		return query.list();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<Pedido> getPedidosByEstado(String estado) {
		final String hql = "FROM Pedido WHERE estado = :estado AND voided = 0 ORDER BY dateCreated DESC";
		final Query query = this.getCurrentSession().createQuery(hql).setParameter("estado", estado);
		return query.list();
	}
	
	@Override
	public void deletePedido(Pedido pedido) {
		// Don't actually delete, just void it (OpenMRS pattern)
		pedido.setVoided(true);
		this.getCurrentSession().saveOrUpdate(pedido);
	}

    @Override
    public Pedido saveOrFromJson(Pedido pedido) {
        try {            // Persistir
            return savePedido(pedido);
        } catch (Exception e) {
            log.error("Erro ao salvar Pedido a partir do JsonNode", e);
        }
        return null;
    }

}
