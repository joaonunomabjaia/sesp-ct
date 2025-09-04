package org.openmrs.module.sespct.api.dao;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmrs.module.sespct.api.model.Pedido;

import java.util.List;

public interface PedidoDao {
	
	/**
	 * Save or update a Pedido
	 * 
	 * @param pedido the Pedido to save
	 * @return the saved Pedido
	 */
	Pedido savePedido(Pedido pedido);
	
	/**
	 * Get a Pedido by ID
	 * 
	 * @param id the Pedido ID
	 * @return the Pedido or null if not found
	 */
	Pedido getPedidoById(Integer id);
	
	/**
	 * Get a Pedido by external ID
	 * 
	 * @param externalId the external Pedido ID
	 * @return the Pedido or null if not found
	 */
	Pedido getPedidoByExternalId(String externalId);
	
	/**
	 * Get all Pedidos
	 * 
	 * @return list of all Pedidos
	 */
	List<Pedido> getAllPedidos();
	
	/**
	 * Get Pedidos by status
	 * 
	 * @param status the status to filter by
	 * @return list of Pedidos with the specified status
	 */
	List<Pedido> getPedidosByEstado(String estado);
	
	/**
	 * Delete a Pedido
	 * 
	 * @param pedido the Pedido to delete
	 */
	void deletePedido(Pedido pedido);

    Pedido saveOrFromJson(Pedido pedido);
}
