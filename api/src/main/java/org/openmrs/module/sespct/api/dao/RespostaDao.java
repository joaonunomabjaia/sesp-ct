package org.openmrs.module.sespct.api.dao;

import org.openmrs.module.sespct.api.model.Resposta;

import java.util.List;

public interface RespostaDao {

    /**
     * Save or update a Resposta
     *
     * @param resposta the Resposta to save
     * @return the saved Resposta
     */
    Resposta saveResposta(Resposta resposta);

    /**
     * Get a Resposta by ID
     *
     * @param id the Resposta ID
     * @return the Resposta or null if not found
     */
    Resposta getRespostaById(Integer id);

    /**
     * Get all Respostas
     *
     * @return list of all Respostas
     */
    List<Resposta> getAllRespostas();

    /**
     * Get all Respostas by Pedido
     *
     * @param pedidoId the ID of the Pedido
     * @return list of Respostas for the given Pedido
     */
    List<Resposta> getRespostasByPedidoId(Integer pedidoId);

    /**
     * Delete a Resposta
     *
     * @param resposta the Resposta to delete
     */
    void deleteResposta(Resposta resposta);

    /**
     * Save or update a Resposta from JSON
     *
     * @param resposta the Resposta object to save
     * @return the saved Resposta
     */
    Resposta saveOrFromJson(Resposta resposta);
}
