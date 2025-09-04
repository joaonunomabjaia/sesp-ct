package org.openmrs.module.sespct.ct;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.openmrs.module.sespct.api.service.RespostaService;
import org.openmrs.module.sespct.api.model.Resposta;
import org.openmrs.module.sespct.api.service.PedidoService;
import org.openmrs.module.sespct.api.model.Pedido;
import org.openmrs.module.sespct.api.service.RespostaService;
import org.openmrs.module.sespct.config.CTConfig;
import org.openmrs.module.sespct.oauth.OAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.http.HttpHeaders;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Cliente HTTP do CT (Comité Terapêutico). Compatível com Spring 4.x / Java 8. Endpoints CT usados:
 * - GET /api/v1/pedido-troca-linhas/{id}?facilityCode={f} - POST
 * /api/v1/pedido-troca-linhas/pesquisa - GET /api/v1/pedido-troca-linhas-respostas/{pedidoId} -
 * POST /api/v1/pedido-troca-linhas/offset-pagination - POST
 * /api/v1/pedido-troca-linhas/cursor-pagination - GET /api/v1/notificacoes
 */
@Service
public class CtClientImpl implements CtClient {
	
	private static final String E_PEDIDO_BY_ID = "/api/v1/pedido-troca-linhas/{id}?facilityCode={f}";
	
	private static final String E_PEDIDOS_PESQUISA = "/api/v1/pedido-troca-linhas/pesquisa";
	
	private static final String E_RESPOSTAS_BY_PEDIDO = "/api/v1/pedido-troca-linhas-respostas/{id}";
	
	private static final String E_OFFSET_PAGINATION = "/api/v1/pedido-troca-linhas/offset-pagination";
	
	private static final String E_CURSOR_PAGINATION = "/api/v1/pedido-troca-linhas/cursor-pagination";
	
	private static final String E_NOTIFICACOES = "/api/v1/notificacoes";
	
	private final RestTemplate rest;
	
	private final OAuthService oauth;
	
	private final CTConfig cfg;
	
	private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private PedidoService pedidoService;
    @Autowired
    private RespostaService  respostaService;

	@Autowired
	public CtClientImpl(RestTemplate rest, OAuthService oauth, CTConfig cfg) {
		this.rest = rest;
		this.oauth = oauth;
		this.cfg = cfg;
	}
	
	/* ===================== Métodos do contrato ===================== */

    @Override
    public Pedido getPedidoById(String id, String facility) {
        HttpHeaders h = authJsonHeaders();
        String f = (facility != null && !facility.isEmpty()) ? facility : cfg.getDefaultFacility();

        ResponseEntity<JsonNode> r = rest.exchange(cfg.getCtBaseUrl() + E_PEDIDO_BY_ID,
                HttpMethod.GET, new HttpEntity<Void>(h), JsonNode.class, id, f);

        JsonNode dp = r.getBody().path("dadosPedido");
        if (dp != null && !dp.isEmpty()) {
            return pedidoService.saveFromJson(dp); // ← transforma e salva via service
        }
        return null;
    }


    /**
	 * Pesquisa “desde” (since ISO-8601) para uma unidade. Implementado como POST para /pesquisa,
	 * pois o CT expõe pesquisa via POST. O shape do JSON pode ser ajustado conforme o contrato
	 * oficial do CT.
	 */
	@Override
	public JsonNode getPedidosSince(String since, String facility) {
		HttpHeaders h = authJsonHeaders();
		
		String f = (facility != null && facility.length() > 0) ? facility : cfg.getDefaultFacility();
		ObjectNode body = mapper.createObjectNode();
		body.put("facilityCode", f);
		if (since != null && since.length() > 0) {
			body.put("since", since); // confirme o nome exato do campo aceito pelo CT
		}
		
		ResponseEntity<JsonNode> r = rest.postForEntity(cfg.getCtBaseUrl() + E_PEDIDOS_PESQUISA,
		    new HttpEntity<String>(body.toString(), h), JsonNode.class);
		return r.getBody();
	}
	
	/* ===================== Utilidades adicionais ===================== */
	
	/** GET /api/v1/pedido-troca-linhas-respostas/{pedidoId} */

    @Override
    public List<Resposta> getRespostasDoPedido(String pedidoId) {
        HttpHeaders h = authJsonHeaders();
        ResponseEntity<JsonNode> r = rest.exchange(
                cfg.getCtBaseUrl() + E_RESPOSTAS_BY_PEDIDO,
                HttpMethod.GET,
                new HttpEntity<Void>(h),
                JsonNode.class,
                pedidoId
        );

        JsonNode body = r.getBody();
        List<Resposta> respostas = new ArrayList<>();

        if (body != null && body.has("data")) {
            for (JsonNode node : body.path("data")) {
                JsonNode dadosResposta = node.path("dadosResposta");
                if (!dadosResposta.isMissingNode()) {
                    Resposta resp = respostaService.saveFromJson(dadosResposta);
                    respostas.add(resp);
                }
            }
        }

        return respostas;
    }


    /**
	 * POST /api/v1/pedido-troca-linhas/offset-pagination
	 * 
	 * @param page página (0-based ou 1-based conforme API do CT)
	 * @param size tamanho da página
	 * @param facility unidade
	 * @param extra campos adicionais de filtro se necessário (pode ser null)
	 */
	public JsonNode searchOffset(int page, int size, String facility, ObjectNode extra) {
		HttpHeaders h = authJsonHeaders();
		
		ObjectNode body = mapper.createObjectNode();
		body.put("facilityCode", (facility != null && facility.length() > 0) ? facility : cfg.getDefaultFacility());
		body.put("page", page);
		body.put("size", size);
		if (extra != null) {
			body.setAll(extra);
		}
		
		ResponseEntity<JsonNode> r = rest.postForEntity(cfg.getCtBaseUrl() + E_OFFSET_PAGINATION, new HttpEntity<String>(
		        body.toString(), h), JsonNode.class);
		return r.getBody();
	}
	
	/**
	 * POST /api/v1/pedido-troca-linhas/cursor-pagination
	 * 
	 * @param cursor token de cursor (null para primeira page)
	 * @param size tamanho do lote
	 */
	public JsonNode searchCursor(String cursor, int size, String facility, ObjectNode extra) {
		HttpHeaders h = authJsonHeaders();
		
		ObjectNode body = mapper.createObjectNode();
		body.put("facilityCode", (facility != null && facility.length() > 0) ? facility : cfg.getDefaultFacility());
		body.put("size", size);
		if (cursor != null && cursor.length() > 0) {
			body.put("cursor", cursor);
		}
		if (extra != null) {
			body.setAll(extra);
		}
		
		ResponseEntity<JsonNode> r = rest.postForEntity(cfg.getCtBaseUrl() + E_CURSOR_PAGINATION, new HttpEntity<String>(
		        body.toString(), h), JsonNode.class);
		return r.getBody();
	}
	
	/** GET /api/v1/notificacoes — fallback para obter eventos caso webhook esteja offline. */
	public JsonNode pollNotificacoes() {
		HttpHeaders h = authJsonHeaders();
		ResponseEntity<JsonNode> r = rest.exchange(cfg.getCtBaseUrl() + E_NOTIFICACOES, HttpMethod.GET,
		    new HttpEntity<Void>(h), JsonNode.class);
		return r.getBody();
	}
	
	/* ===================== Helpers ===================== */
	
	private HttpHeaders authJsonHeaders() {
		HttpHeaders h = new HttpHeaders();
		h.set("Authorization", "Bearer " + oauth.getToken()); // compatível com Spring 4.x
		h.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		h.setContentType(MediaType.APPLICATION_JSON);
		return h;
	}
}
