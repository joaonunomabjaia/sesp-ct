package org.openmrs.module.sespct.ct;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmrs.module.sespct.api.model.Resposta;

import java.util.List;

public interface CtClient {
	
	JsonNode getPedidoById(String requestId, String facilityCode);
	
	JsonNode getPedidosSince(String sinceIso, String facilityCode);

    JsonNode getRespostasDoPedido(String pedidoId);
}
