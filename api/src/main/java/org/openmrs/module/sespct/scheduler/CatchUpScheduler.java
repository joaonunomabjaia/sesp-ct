package org.openmrs.module.sespct.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmrs.module.sespct.api.service.PedidoService;
import org.openmrs.module.sespct.config.CTConfig;
import org.openmrs.module.sespct.ct.CtClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CatchUpScheduler {
	
	@Autowired
	private CtClient ct;
	
	@Autowired
	private CTConfig cfg;
	
	@Autowired
	private PedidoService pedidoService;
	
	// a cada 10 minutos
	@Scheduled(cron = "0 */10 * * * *")
	public void pullSince() {
		try {
			JsonNode list = ct.getPedidosSince(cfg.getSinceIso(), cfg.getDefaultFacility());
			if (list != null && list.isArray()) {
				for (JsonNode elem : list) {
					String id = elem.path("requestId").asText(null);
					if (id != null) {
						pedidoService.fetchAndCreateFromCtAsync(id, cfg.getDefaultFacility());
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
