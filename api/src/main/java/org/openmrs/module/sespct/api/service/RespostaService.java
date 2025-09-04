package org.openmrs.module.sespct.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.sespct.api.model.Resposta;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface RespostaService extends OpenmrsService {

    Resposta saveResposta(Resposta resposta);

    @Transactional(readOnly = true)
    Resposta getRespostaById(Integer id);

    @Transactional(readOnly = true)
    Resposta getRespostaByExternalId(String respostaId);

    @Transactional(readOnly = true)
    List<Resposta> getRespostasByPedidoId(String pedidoId);

    void deleteResposta(Resposta resposta);

    Resposta saveFromJson(JsonNode dadosResposta);
}
